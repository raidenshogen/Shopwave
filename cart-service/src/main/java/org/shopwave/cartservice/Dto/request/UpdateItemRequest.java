package org.shopwave.cartservice.Dto.request;

// UpdateItemRequest.java


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateItemRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}