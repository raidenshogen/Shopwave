package org.shopwave.productservice.Dto.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private BigDecimal effectivePrice;
    private String sku;
    private Integer stock;
    private boolean inStock;
    private boolean onSale;
    private UUID sellerId;
    private CategoryResponse category;
    private String brand;
    private String status;
    private Boolean isFeatured;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private List<ProductImageResponse> images;
    private LocalDateTime createdAt;
}