package org.shopwave.cartservice.entities;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;
import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.lang.annotation.Inherited;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.processing.Generated;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // ADD THIS

public class Cart implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private String couponCode;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
        
    @JsonIgnore // ADD THIS - prevents serialization of computed value
    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @JsonIgnore // ADD THIS - prevents serialization of computed value
    public BigDecimal getTotal() {
        return getSubtotal().subtract(discountAmount);
    }
    
    @JsonIgnore // ADD THIS - prevents serialization of computed value
    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
