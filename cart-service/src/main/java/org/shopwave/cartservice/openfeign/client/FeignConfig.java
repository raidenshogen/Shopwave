package org.shopwave.cartservice.openfeign.client;


import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new RuntimeException(
                        "Product not found");
            }
            if (response.status() == 503) {
                return new RuntimeException(
                        "Product service unavailable");
            }
            return new RuntimeException(
                    "Error calling product service: " +
                            response.status());
        };
    }
}