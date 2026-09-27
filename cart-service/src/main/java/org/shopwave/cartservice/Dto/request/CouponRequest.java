package org.shopwave.cartservice.Dto.request;

// CouponRequest.java

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;
}