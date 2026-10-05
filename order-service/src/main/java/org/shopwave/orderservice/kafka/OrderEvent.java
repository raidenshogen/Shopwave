package org.shopwave.orderservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String eventType;       // String, NOT enum
    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private BigDecimal total;
    private String status;          // String, NOT OrderStatus enum

    private List<Item> items;
    private Object shippingAddress;
    private LocalDateTime timestamp;
    private Object metadata;

    // Inner item class for events
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private UUID productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}