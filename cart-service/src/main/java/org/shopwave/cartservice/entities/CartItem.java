package org.shopwave.cartservice.entities;


import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

    private UUID productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal unitPrice;

    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(
                BigDecimal.valueOf(quantity));
    }
}