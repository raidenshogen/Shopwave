package org.shopwave.productservice.repositories;

import org.shopwave.productservice.entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByProductId(UUID productId);

    @Modifying
    @Transactional
    @Query("UPDATE ProductImage pi SET pi.isPrimary = false " +
            "WHERE pi.product.id = :productId")
    void clearPrimaryImage(UUID productId);
}