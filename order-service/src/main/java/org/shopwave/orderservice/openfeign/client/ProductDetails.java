package org.shopwave.orderservice.openfeign.client;



import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetails {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal effectivePrice; // price or sale_price
    private String sku;
    private int stock;
    private boolean inStock;
    private List<ImageDetail> images;
    private String categoryName;
    private String sellerName;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDetail {
        private UUID id;
        private String imageUrl;
        private Boolean isPrimary;
        private Boolean isThumbnail;
    }
}