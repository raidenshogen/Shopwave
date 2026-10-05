package org.shopwave.orderservice.openfeign.client;




import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service")
public interface CartServiceClient {

    @GetMapping("/api/cart")
    CartResponse getCart(
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/api/cart/clear")
    void clearCart(
            @RequestHeader("Authorization") String authorization
    );
}
