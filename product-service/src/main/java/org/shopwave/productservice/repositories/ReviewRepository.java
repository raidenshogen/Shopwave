package org.shopwave.productservice.repositories;

import org.shopwave.productservice.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ReviewRepository
        extends JpaRepository<Review, UUID> {

    Page<Review> findByProductId(UUID productId,
                                 Pageable pageable);
    boolean existsByProductIdAndUserId(UUID productId,
                                       UUID userId);

    @Query("SELECT AVG(r.rating) FROM Review r " +
            "WHERE r.product.id = :productId")
    Double calculateAverageRating(UUID productId);
}