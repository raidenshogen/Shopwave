package org.shopwave.productservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "avg_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isInStock() {
        return stock > 0;
    }

    public boolean isOnSale() {
        return salePrice != null &&
                salePrice.compareTo(price) < 0;
    }

    public BigDecimal getEffectivePrice() {
        return isOnSale() ? salePrice : price;
    }

    public enum ProductStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK
    }
}