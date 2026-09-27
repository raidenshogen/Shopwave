package org.shopwave.productservice.Dto.Request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    private BigDecimal salePrice;

    @NotBlank
    private String sku;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotNull
    private UUID categoryId;

    private String brand;
}