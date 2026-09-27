package org.shopwave.cartservice.service;


import org.shopwave.cartservice.Dto.request.*;
import org.shopwave.cartservice.Dto.response.CartResponse;
import java.util.UUID;

public interface CartService {
    CartResponse getCart(UUID userId);
    CartResponse addItem(UUID userId, AddItemRequest request);
    CartResponse updateItem(UUID userId, UUID productId,
                            UpdateItemRequest request);
    CartResponse removeItem(UUID userId, UUID productId);
    CartResponse applyCoupon(UUID userId,
                             CouponRequest request);
    CartResponse removeCoupon(UUID userId);
    void clearCart(UUID userId);
}
