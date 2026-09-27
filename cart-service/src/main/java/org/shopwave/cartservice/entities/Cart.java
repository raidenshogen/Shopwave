package org.shopwave.cartservice.entities;


import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {

    private UUID userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private String couponCode;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return getSubtotal().subtract(discountAmount);
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
