package org.shopwave.productservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}