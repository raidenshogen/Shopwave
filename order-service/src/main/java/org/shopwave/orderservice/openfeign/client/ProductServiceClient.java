package org.shopwave.orderservice.openfeign.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ProductDetails getProduct(
            @PathVariable("id") UUID productId,
            @RequestHeader(value = "Authorization", required = false) String auth
    );
}