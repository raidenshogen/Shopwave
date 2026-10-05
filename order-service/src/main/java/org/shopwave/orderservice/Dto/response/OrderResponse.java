package org.shopwave.orderservice.Dto.response;



import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private UUID id;
    private String orderNumber;
    private UUID customerId;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shippingFee;
    private BigDecimal tax;
    private BigDecimal total;
    private String couponCode;
    private Object shippingAddress; // Can be a Map or custom class
    private String notes;
    private List<OrderItemResponse> items;
    private List<OrderStatusResponse> statusHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}