package org.shopwave.productservice.Dto.Response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProductImageResponse {
    private UUID id;
    private String imageUrl;
    private String altText;
    private Boolean isPrimary;
    private Integer displayOrder;
}
