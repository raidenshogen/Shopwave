package org.shopwave.cartservice.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shopwave.cartservice.Dto.request.*;
import org.shopwave.cartservice.Dto.response.CartResponse;
import org.shopwave.cartservice.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private UUID getCurrentUserId() {
        return UUID.fromString(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName());
    }

    // SCRUM-32 - View cart + total
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        log.info("Getting cart for user: {}",
                getCurrentUserId());
        return ResponseEntity.ok(
                cartService.getCart(getCurrentUserId()));
    }

    // SCRUM-28 - Add item to cart
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody AddItemRequest request) {
        log.info("Adding item to cart for user: {}",
                getCurrentUserId());
        return ResponseEntity.ok(
                cartService.addItem(
                        getCurrentUserId(), request));
    }

    // SCRUM-30 - Update item quantity
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateItemRequest request) {
        log.info("Updating item {} for user: {}",
                productId, getCurrentUserId());
        return ResponseEntity.ok(
                cartService.updateItem(
                        getCurrentUserId(),
                        productId, request));
    }

    // SCRUM-29 - Remove item from cart
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable UUID productId) {
        log.info("Removing item {} for user: {}",
                productId, getCurrentUserId());
        return ResponseEntity.ok(
                cartService.removeItem(
                        getCurrentUserId(), productId));
    }

    // SCRUM-31 - Apply coupon
    @PostMapping("/coupon")
    public ResponseEntity<CartResponse> applyCoupon(
            @Valid @RequestBody CouponRequest request) {
        log.info("Applying coupon {} for user: {}",
                request.getCode(), getCurrentUserId());
        return ResponseEntity.ok(
                cartService.applyCoupon(
                        getCurrentUserId(), request));
    }

    // SCRUM-31 - Remove coupon
    @DeleteMapping("/coupon")
    public ResponseEntity<CartResponse> removeCoupon() {
        log.info("Removing coupon for user: {}",
                getCurrentUserId());
        return ResponseEntity.ok(
                cartService.removeCoupon(
                        getCurrentUserId()));
    }

    // Clear cart
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        log.info("Clearing cart for user: {}",
                getCurrentUserId());
        cartService.clearCart(getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}