package org.shopwave.orderservice.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shopwave.orderservice.Dto.request.CancelOrderRequest;
import org.shopwave.orderservice.Dto.request.CreateOrderRequest;
import org.shopwave.orderservice.Dto.response.OrderResponse;
import org.shopwave.orderservice.security.JwtUtil;
import org.shopwave.orderservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {

        UUID userId = jwtUtil.extractUserIdFromRequest(httpRequest);
        String authToken = httpRequest.getHeader("Authorization");

        OrderResponse response = orderService.createOrder(userId, request, authToken);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        UUID userId = jwtUtil.extractUserIdFromRequest(httpRequest);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<OrderResponse> orders = orderService.getUserOrders(userId, pageable);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID orderId,
            HttpServletRequest httpRequest) {

        UUID userId = jwtUtil.extractUserIdFromRequest(httpRequest);
        OrderResponse order = orderService.getOrderById(userId, orderId);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(@PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody(required = false) CancelOrderRequest request,
            HttpServletRequest httpRequest) {

        UUID userId = jwtUtil.extractUserIdFromRequest(httpRequest);
        CancelOrderRequest cancelRequest = request != null ? request : new CancelOrderRequest();

        OrderResponse response = orderService.cancelOrder(userId, orderId, cancelRequest);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID orderId,
            @RequestParam String status,
            @RequestParam(required = false) String comment,
            HttpServletRequest httpRequest) {

        // This endpoint is for admins/sellers - extract their ID
        UUID adminId = jwtUtil.extractUserIdFromRequest(httpRequest);

        OrderResponse response = orderService.updateOrderStatus(orderId, status, comment, adminId);

        return ResponseEntity.ok(response);
    }
}
