package org.shopwave.cartservice.openfeign.client;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ProductDetails {
    private UUID id;
    private String name;
    private BigDecimal effectivePrice;
    private Integer stock;
    private boolean inStock;
    private List<ImageDetail> images;

    @Data
    public static class ImageDetail {
        private String imageUrl;
        private Boolean isPrimary;
    }
}
