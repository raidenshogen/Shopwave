package org.shopwave.orderservice.service.imp;
import org.shopwave.orderservice.openfeign.client.CartResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shopwave.orderservice.Dto.request.CancelOrderRequest;
import org.shopwave.orderservice.Dto.request.CreateOrderRequest;
import org.shopwave.orderservice.Dto.response.*;
import org.shopwave.orderservice.entities.*;
import org.shopwave.orderservice.entities.OrderStatus;
import org.shopwave.orderservice.kafka.OrderEventProducer;
import org.shopwave.orderservice.kafka.OrderEvent;
import org.shopwave.orderservice.exceptions.InsufficientStockException;
import org.shopwave.orderservice.exceptions.OrderNotFoundException;
import org.shopwave.orderservice.openfeign.client.CartServiceClient;
import org.shopwave.orderservice.openfeign.client.ProductDetails;
import org.shopwave.orderservice.openfeign.client.ProductServiceClient;
import org.shopwave.orderservice.repositories.OrderItemRepository;
import org.shopwave.orderservice.repositories.OrderRepository;
import org.shopwave.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.shopwave.orderservice.openfeign.client.CartResponse.CartItemResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final OrderEventProducer orderEventProducer;
    @Value("${order.shipping.fee:9.99}")
    private BigDecimal shippingFee;

    @Value("${order.shipping.tax-rate:0.08}")
    private BigDecimal taxRate;

    // ==========================================
    // CREATE ORDER
    // ==========================================

    @Override
    @Transactional
    public OrderResponse createOrder(UUID customerId,
                                     CreateOrderRequest request,
                                     String authToken) {

        log.info("Creating order for customer: {}", customerId);

        // 1. Get cart from Cart Service
        log.debug("Fetching cart from cart service");
        CartResponse cart = cartServiceClient.getCart(authToken);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order with empty cart");
        }

        // 2. Validate stock for each item
        validateCartItems(cart.getItems());

        // 3. Build Order entity (NO paymentMethod - field doesn't exist in entity)
        Order order = Order.builder()
                .customerId(customerId)
                .subtotal(cart.getSubtotal())
                .discount(cart.getDiscountAmount() != null ?
                        cart.getDiscountAmount() : BigDecimal.ZERO)
                .shippingFee(shippingFee)
                .couponCode(cart.getCouponCode())
                .notes(request.getNotes())
                .build();

        // 4. Calculate tax and total
        BigDecimal taxableAmount = order.getSubtotal().subtract(order.getDiscount());
        BigDecimal calculatedTax = taxableAmount.multiply(taxRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal calculatedTotal = taxableAmount.add(shippingFee).add(calculatedTax)
                .setScale(2, RoundingMode.HALF_UP);

        order.setTax(calculatedTax);
        order.setTotal(calculatedTotal);

        // 5. Set shipping address (placeholder JSON)
        String addressJson = "{\"addressId\":\"" + request.getAddressId() + "\"}";
        order.setShippingAddress(addressJson);

        // 6. Add initial status history
        addStatusHistory(order, OrderStatus.PENDING, "Order created");

        // 7. Save order first (need ID before items)
        Order savedOrder = orderRepository.save(order);

        // 8. Save order items
        List<OrderItem> savedItems = saveOrderItems(savedOrder, cart);
        savedOrder.setItems(savedItems); // Set back for response mapping

        // 9. Clear cart after successful creation
        try {
            cartServiceClient.clearCart(authToken);
            log.info("Cart cleared successfully");
        } catch (Exception e) {
            log.warn("Failed to clear cart: {}", e.getMessage());
        }

        // 10. Publish Kafka event
        publishOrderCreated(savedOrder);

        log.info("Order created: {}", savedOrder.getOrderNumber());
        return mapToResponse(savedOrder);
    }

    // ==========================================
    // GET ORDERS
    // ==========================================

    @Override
    public OrderResponse getOrderById(UUID customerId, UUID orderId) {
        return mapToResponse(findOrderForCustomer(customerId, orderId));
    }

    @Override
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order number: " + orderNumber));
        return mapToResponse(order);
    }

    @Override
    public Page<OrderResponse> getUserOrders(UUID customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::mapToResponse);
    }

    // ==========================================
    // CANCEL ORDER
    // ==========================================

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID customerId,
                                     UUID orderId,
                                     CancelOrderRequest request) {

        Order order = findOrderForCustomer(customerId, orderId);

        // Business rule: Can't cancel shipped/delivered/cancelled orders
        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        // Update status
        order.setStatus(OrderStatus.CANCELLED);

        String reason = (request != null && request.getReason() != null) ?
                request.getReason() : "Customer requested cancellation";

        addStatusHistory(order, OrderStatus.CANCELLED, reason);

        Order saved = orderRepository.save(order);
        publishOrderCancelled(saved);

        log.info("Order cancelled: {}", order.getOrderNumber());
        return mapToResponse(saved);
    }

    // ==========================================
    // UPDATE STATUS (Admin/Seller)
    // ==========================================

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId,
                                           String statusStr,
                                           String comment,
                                           UUID adminId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Convert string to enum safely
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + statusStr +
                    ". Valid values: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED");
        }

        order.setStatus(newStatus);
        addStatusHistory(order, newStatus, comment, adminId);

        Order saved = orderRepository.save(order);

        // Publish appropriate event based on new status
        switch (newStatus) {
            case CONFIRMED -> publishOrderConfirmed(saved);
            case SHIPPED -> publishOrderShipped(saved);
            case DELIVERED -> publishOrderDelivered(saved);
            default -> log.debug("No Kafka event for status: {}", newStatus);
        }

        log.info("Order {} → {}", saved.getOrderNumber(), newStatus);
        return mapToResponse(saved);
    }

    // ==========================================
    // PRIVATE HELPERS - Validation
    // ==========================================

    /**
     * Validate stock availability via Product Service
     * FIXED: Uses List<CartItemResponse> explicitly
     */
    private void validateCartItems(List<CartResponse.CartItemResponse> items) { // FIX: Explicit type
        for (CartResponse.CartItemResponse item : items) {
            try {
                ProductDetails product = productServiceClient.getProduct(
                        item.getProductId(), null);

                if (!product.isInStock()) {
                    throw new InsufficientStockException(
                            "Out of stock: " + product.getName());
                }

                // Check quantity against available stock
                int requestedQty = item.getQuantity(); // FIX: Direct getter access
                int availableStock = product.getStock();

                if (requestedQty > availableStock) {
                    throw new InsufficientStockException(
                            String.format("Insufficient '%s': want %d, have %d",
                                    product.getName(), requestedQty, availableStock));
                }

            } catch (InsufficientStockException e) {
                throw e; // Re-throw our business exception
            } catch (Exception e) {
                log.warn("Stock validation failed for product {}: {}",
                        item.getProductId(), e.getMessage());
                // Fail open: continue even if Product Service is down
            }
        }
    }

    // ==========================================
    // PRIVATE HELPERS - Persistence
    // ==========================================

    /**
     * Save order items to database
     * FIXED: Properly maps from CartItemResponse to OrderItem entity
     */
    private List<OrderItem> saveOrderItems(Order order, CartResponse cart) {
        List<OrderItem> items = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()       // Builder pattern
                        .order(order)
                        .productId(cartItem.getProductId())     // FIX
                        .productName(cartItem.getProductName()) // FIX
                        .productImage(cartItem.getProductImage())// FIX
                        .quantity(cartItem.getQuantity())       // FIX
                        .unitPrice(cartItem.getUnitPrice())     // FIX
                        .totalPrice(cartItem.getTotalPrice())   // FIX
                        .build())
                .collect(Collectors.toList());

        return orderItemRepository.saveAll(items);
    }

    private void addStatusHistory(Order order, OrderStatus status, String comment) {
        addStatusHistory(order, status, comment, null);
    }

    private void addStatusHistory(Order order, OrderStatus status,
                                  String comment, UUID changedBy) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .comment(comment)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();

        order.getStatusHistory().add(history);
    }

    private Order findOrderForCustomer(UUID customerId, UUID orderId) {
        return orderRepository.findById(orderId)
                .filter(o -> o.getCustomerId().equals(customerId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    // ==========================================
    // PRIVATE HELPERS - Kafka Events
    // ==========================================

    /**
     * Build OrderEvent DTO from Order entity
     * FIXED METHOD NAME: buildOrderEvent (not buildEvent)
     */
    private OrderEvent buildOrderEvent(Order order) {
        try {
            // Map items to event items - FIX: Use OrderEvent.Item.builder()
            List<OrderEvent.Item> eventItems = null;

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                eventItems = order.getItems().stream()
                        .map(item -> OrderEvent.Item.builder() // ✅ Inner class builder
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .build())
                        .collect(Collectors.toList());
            }

            // Parse shipping address
            Object address = parseAddress(order.getShippingAddress());

            // BUILD EVENT - status must be String (.name()), not enum
            return OrderEvent.builder()
                    .eventType("")                    // Placeholder
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .customerId(order.getCustomerId())
                    .total(order.getTotal())
                    .status(order.getStatus().name()) // ✅ Enum → String
                    .items(eventItems)
                    .shippingAddress(address)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error building order event: {}", e.getMessage(), e);
            return null;
        }
    }

    // Event publishing methods - ALL USE orderEventProducer (exact field name)

    private void publishOrderCreated(Order order) {
        OrderEvent event = buildOrderEvent(order);
        if (event != null) {
            orderEventProducer.publishOrderCreated(event); // FIX: Correct field name
        }
    }

    private void publishOrderCancelled(Order order) {
        OrderEvent event = buildOrderEvent(order);
        if (event != null) {
            orderEventProducer.publishOrderCancelled(event); // FIX
        }
    }

    private void publishOrderConfirmed(Order order) {
        OrderEvent event = buildOrderEvent(order);
        if (event != null) {
            orderEventProducer.publishOrderConfirmed(event); // FIX
        }
    }

    private void publishOrderShipped(Order order) {
        OrderEvent event = buildOrderEvent(order);
        if (event != null) {
            orderEventProducer.publishOrderShipped(event); // Wait, typo here? Let me fix:
            // Actually it should be: orderEventProducer.publishOrderShipped(event);
        }
    }

    private void publishOrderDelivered(Order order) {
        OrderEvent event = buildOrderEvent(order);
        if (event != null) {
            orderEventProducer.publishOrderDelivered(event); // FIX
        }
    }

    private Object parseAddress(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return new ObjectMapper().readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

    // ==========================================
    // MAPPING: Entity → Response DTO
    // ==========================================

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .status(order.getStatus() != null ? order.getStatus().name() : "UNKNOWN")
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .shippingFee(order.getShippingFee())
                .tax(order.getTax())
                .total(order.getTotal())
                .couponCode(order.getCouponCode())
                .shippingAddress(parseAddress(order.getShippingAddress()))
                .notes(order.getNotes())
                .items(mapItems(order.getItems()))
                .statusHistory(mapHistory(order.getStatusHistory()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private List<OrderItemResponse> mapItems(List<OrderItem> items) {
        if (items == null) return List.of();
        return items.stream().map(this::mapSingleItem).collect(Collectors.toList());
    }

    private OrderItemResponse mapSingleItem(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    private List<OrderStatusResponse> mapHistory(List<OrderStatusHistory> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(h -> OrderStatusResponse.builder()
                        .status(h.getStatus() != null ? h.getStatus().name() : "UNKNOWN")
                        .comment(h.getComment())
                        .changedBy(h.getChangedBy())
                        .changedAt(h.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }
}