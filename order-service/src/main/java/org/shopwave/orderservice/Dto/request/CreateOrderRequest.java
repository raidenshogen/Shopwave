package org.shopwave.orderservice.Dto.request;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Address ID is required")
    private UUID addressId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CARD, PAYPAL, etc.

    private String notes;
}