package org.shopwave.orderservice.Dto.request;



import lombok.Data;

@Data
public class CancelOrderRequest {
    private String reason;
}