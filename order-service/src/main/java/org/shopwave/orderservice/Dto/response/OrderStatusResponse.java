package org.shopwave.orderservice.Dto.response;



import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponse {

    private String status;
    private String comment;
    private UUID changedBy;
    private LocalDateTime changedAt;
}
