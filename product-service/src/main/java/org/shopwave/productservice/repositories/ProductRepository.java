package org.shopwave.productservice.repositories;

import org.shopwave.productservice.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository
        extends JpaRepository<Product, UUID> {

    Optional<Product> findBySlug(String slug);
    boolean existsBySku(String sku);
    List<Product> findBySellerId(UUID sellerId);
    List<Product> findByIsFeaturedTrue();
    Page<Product> findByCategoryId(UUID categoryId,
                                   Pageable pageable);

   @Query("""
        SELECT p FROM Product p
        WHERE p.status = org.shopwave.productservice.entities.Product$ProductStatus.ACTIVE
        AND (:search = '' 
             OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:#{#categoryId} IS NULL OR p.category.id = :#{#categoryId})
        AND (:#{#minPrice} IS NULL OR p.price >= :#{#minPrice})
        AND (:#{#maxPrice} IS NULL OR p.price <= :#{#maxPrice})
        """)
Page<Product> searchProducts(
        @Param("search") String search,
        @Param("categoryId") UUID categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable);
}