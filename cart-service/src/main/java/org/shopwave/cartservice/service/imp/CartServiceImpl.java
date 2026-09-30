package org.shopwave.cartservice.service.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shopwave.cartservice.openfeign.client.ProductDetails;
import org.shopwave.cartservice.openfeign.client.ProductServiceClient;
import org.shopwave.cartservice.Dto.request.*;
import org.shopwave.cartservice.Dto.response.*;
import org.shopwave.cartservice.entities.Cart;
import org.shopwave.cartservice.entities.CartItem;
import org.shopwave.cartservice.service.CartService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Cart> redisTemplate;
    private final ProductServiceClient productServiceClient;

    @Value("${cart.ttl}")
    private long cartTtl;

    private String cartKey(UUID userId) {
        return "cart:" + userId;
    }

    private Cart getOrCreateCart(UUID userId) {
    String key = cartKey(userId);
    
    try {
        Cart cart = redisTemplate.opsForValue().get(key);
        if (cart != null) {
            return cart;
        }
    } catch (Exception e) {
        // Handles stale/corrupted cache during deployments
        log.warn("Failed to deserialize cart for user: {}. " +
                 "Clearing stale cache. Error: {}", 
                 userId, e.getMessage());
        redisTemplate.delete(key); // Clear corrupted entry
    }

    // Build fresh cart
    return Cart.builder()
            .userId(userId)
            .items(new ArrayList<>())
            .discountAmount(BigDecimal.ZERO)
            .build();
}

    private void saveCart(Cart cart) {
        redisTemplate.opsForValue().set(
                cartKey(cart.getUserId()),
                cart,
                cartTtl,
                TimeUnit.SECONDS);
    }

    private String getPrimaryImage(ProductDetails product) {
        if (product.getImages() == null ||
                product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(img -> Boolean.TRUE
                        .equals(img.getIsPrimary()))
                .findFirst()
                .map(ProductDetails.ImageDetail::getImageUrl)
                .orElse(product.getImages()
                        .get(0).getImageUrl());
    }

    private void recalculateDiscount(Cart cart) {
        if (cart.getCouponCode() == null) return;
        BigDecimal discountPercent = getCouponDiscount(
                cart.getCouponCode(),
                cart.getSubtotal());
        cart.setDiscountAmount(
                cart.getSubtotal()
                        .multiply(discountPercent)
                        .setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal getCouponDiscount(
            String code, BigDecimal subtotal) {
        return switch (code.toUpperCase()) {
            case "SAVE10" -> {
                if (subtotal.compareTo(
                        BigDecimal.valueOf(50)) < 0) {
                    throw new RuntimeException(
                            "Minimum order $50 for SAVE10");
                }
                yield BigDecimal.valueOf(0.10);
            }
            case "SAVE20" -> {
                if (subtotal.compareTo(
                        BigDecimal.valueOf(100)) < 0) {
                    throw new RuntimeException(
                            "Minimum order $100 for SAVE20");
                }
                yield BigDecimal.valueOf(0.20);
            }
            case "SAVE50" -> {
                if (subtotal.compareTo(
                        BigDecimal.valueOf(500)) < 0) {
                    throw new RuntimeException(
                            "Minimum order $500 for SAVE50");
                }
                yield BigDecimal.valueOf(0.50);
            }
            default -> throw new RuntimeException(
                    "Invalid coupon code: " + code);
        };
    }

    private CartResponse mapToResponse(Cart cart) {
        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(cart.getItems().stream()
                        .map(item -> CartItemResponse.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .productImage(item.getProductImage())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .build())
                        .toList())
                .totalItems(cart.getTotalItems())
                .subtotal(cart.getSubtotal())
                .couponCode(cart.getCouponCode())
                .discountAmount(cart.getDiscountAmount())
                .total(cart.getTotal())
                .build();
    }

    @Override
    public CartResponse getCart(UUID userId) {
        log.info("Getting cart for user: {}", userId);
        Cart cart = getOrCreateCart(userId);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse addItem(UUID userId,
                                AddItemRequest request) {
        log.info("Adding item {} to cart for user: {}",
                request.getProductId(), userId);

        //  Get real product details from product-service
        ProductDetails  product = productServiceClient
                .getProduct(request.getProductId());

        //  Validate stock
        if (!product.isInStock()) {
            throw new RuntimeException(
                    "Product out of stock: " +
                            product.getName());
        }

        if (request.getQuantity() > product.getStock()) {
            throw new RuntimeException(
                    "Not enough stock. Available: " +
                            product.getStock());
        }

        Cart cart = getOrCreateCart(userId);

        Optional<CartItem> existingItem = cart.getItems()
                .stream()
                .filter(i -> i.getProductId()
                        .equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            //  Update quantity
            int newQuantity = existingItem.get()
                    .getQuantity() + request.getQuantity();

            if (newQuantity > product.getStock()) {
                throw new RuntimeException(
                        "Not enough stock. Available: " +
                                product.getStock());
            }

            existingItem.get().setQuantity(newQuantity);
            //  Update price in case it changed (sale etc)
            existingItem.get().setUnitPrice(
                    product.getEffectivePrice());

        } else {
            //  Add new item with real product data
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(getPrimaryImage(product))
                    .quantity(request.getQuantity())
                    .unitPrice(product.getEffectivePrice())
                    .build();

            cart.getItems().add(newItem);
        }

        //  Recalculate discount if coupon applied
        if (cart.getCouponCode() != null) {
            recalculateDiscount(cart);
        }

        saveCart(cart);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse updateItem(UUID userId,
                                   UUID productId,
                                   UpdateItemRequest request) {
        log.info("Updating item {} for user: {}",
                productId, userId);

        Cart cart = getOrCreateCart(userId);

        //  Find item or throw
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId()
                        .equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Item not found in cart"));

        //  Validate stock from product-service
        ProductDetails  product = productServiceClient
                .getProduct(productId);

        if (request.getQuantity() > product.getStock()) {
            throw new RuntimeException(
                    "Not enough stock. Available: " +
                            product.getStock());
        }

        //  Update quantity and refresh price
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(product.getEffectivePrice());

        //  Recalculate discount
        if (cart.getCouponCode() != null) {
            recalculateDiscount(cart);
        }

        saveCart(cart);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse removeItem(UUID userId,
                                   UUID productId) {
        log.info("Removing item {} for user: {}",
                productId, userId);

        Cart cart = getOrCreateCart(userId);

        boolean removed = cart.getItems().removeIf(
                i -> i.getProductId().equals(productId));

        if (!removed) {
            throw new RuntimeException(
                    "Item not found in cart");
        }

        //  If cart empty remove coupon
        if (cart.getItems().isEmpty()) {
            cart.setCouponCode(null);
            cart.setDiscountAmount(BigDecimal.ZERO);
        } else if (cart.getCouponCode() != null) {
            //  Recalculate discount
            recalculateDiscount(cart);
        }

        saveCart(cart);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse applyCoupon(UUID userId,
                                    CouponRequest request) {
        log.info("Applying coupon {} for user: {}",
                request.getCode(), userId);

        Cart cart = getOrCreateCart(userId);

        // Cart must not be empty
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException(
                    "Cannot apply coupon to empty cart");
        }

        // Validate and get discount percentage
        BigDecimal discountPercent = getCouponDiscount(
                request.getCode(),
                cart.getSubtotal());

        cart.setCouponCode(
                request.getCode().toUpperCase());
        cart.setDiscountAmount(
                cart.getSubtotal()
                        .multiply(discountPercent)
                        .setScale(2, RoundingMode.HALF_UP));

        saveCart(cart);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse removeCoupon(UUID userId) {
        log.info("Removing coupon for user: {}", userId);

        Cart cart = getOrCreateCart(userId);

        cart.setCouponCode(null);
        cart.setDiscountAmount(BigDecimal.ZERO);

        saveCart(cart);
        return mapToResponse(cart);
    }

    @Override
    public void clearCart(UUID userId) {
        log.info("Clearing cart for user: {}", userId);
        redisTemplate.delete(cartKey(userId));
    }
}