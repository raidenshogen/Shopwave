package org.shopwave.productservice.Dto.Response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID userId;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}