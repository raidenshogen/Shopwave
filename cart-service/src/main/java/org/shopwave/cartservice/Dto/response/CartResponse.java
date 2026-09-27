package org.shopwave.cartservice.Dto.response;

// CartResponse.java

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID userId;
    private List<CartItemResponse> items;
    private int totalItems;
    private BigDecimal subtotal;
    private String couponCode;
    private BigDecimal discountAmount;
    private BigDecimal total;
}
