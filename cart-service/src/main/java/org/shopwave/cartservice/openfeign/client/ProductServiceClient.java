package org.shopwave.cartservice.openfeign.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.shopwave.cartservice.config.FeignConfig;
import java.util.UUID;

@FeignClient(
    name = "product-service",
    configuration = FeignConfig.class
)
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ProductDetails getProduct(@PathVariable UUID id);
}