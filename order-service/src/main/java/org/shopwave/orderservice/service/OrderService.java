package org.shopwave.orderservice.service;




import org.shopwave.orderservice.Dto.request.CancelOrderRequest;
import org.shopwave.orderservice.Dto.request.CreateOrderRequest;
import org.shopwave.orderservice.Dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(UUID customerId, CreateOrderRequest request, String token);

    OrderResponse getOrderById(UUID customerId, UUID orderId);

    OrderResponse getOrderByNumber(String orderNumber);

    Page<OrderResponse> getUserOrders(UUID customerId, Pageable pageable);

    OrderResponse cancelOrder(UUID customerId, UUID orderId, CancelOrderRequest request);

    OrderResponse updateOrderStatus(UUID orderId, String status, String comment, UUID adminId);
}
