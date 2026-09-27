package org.shopwave.productservice.service;

import org.shopwave.productservice.Dto.Request.ProductRequest;
import org.shopwave.productservice.Dto.Request.ReviewRequest;
import org.shopwave.productservice.Dto.Response.PageResponse;
import org.shopwave.productservice.Dto.Response.ProductResponse;
import org.shopwave.productservice.Dto.Response.ReviewResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponse createProduct(UUID sellerId,
                                  ProductRequest request);
    ProductResponse updateProduct(UUID productId,
                                  UUID sellerId,
                                  ProductRequest request);
    void deleteProduct(UUID productId, UUID sellerId);
    ProductResponse getProductById(UUID productId);
    ProductResponse getProductBySlug(String slug);
    PageResponse<ProductResponse> getAllProducts(
            String search,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable);
    List<ProductResponse> getFeaturedProducts();
    List<ProductResponse> getSellerProducts(UUID sellerId);
    ReviewResponse addReview(UUID productId,
                             UUID userId,
                             ReviewRequest request);
    PageResponse<ReviewResponse> getProductReviews(
            UUID productId,
            Pageable pageable);
    void updateStock(UUID productId, Integer quantity);
}