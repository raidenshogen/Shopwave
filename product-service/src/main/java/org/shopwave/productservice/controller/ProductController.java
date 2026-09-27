package org.shopwave.productservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shopwave.productservice.Dto.Request.ProductRequest;
import org.shopwave.productservice.Dto.Request.ReviewRequest;
import org.shopwave.productservice.Dto.Response.PageResponse;
import org.shopwave.productservice.Dto.Response.ProductResponse;
import org.shopwave.productservice.Dto.Response.ReviewResponse;
import org.shopwave.productservice.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            HttpServletRequest request,
            @Valid @RequestBody ProductRequest productRequest) {

        UUID sellerId = UUID.fromString(
                (String) request.getAttribute("userId"));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.createProduct(
                        sellerId, productRequest));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID sellerId,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(
                productService.updateProduct(id, sellerId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID sellerId) {
        productService.deleteProduct(id, sellerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                productService.getProductById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(
            @PathVariable String slug) {
        return ResponseEntity.ok(
                productService.getProductBySlug(slug));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        return ResponseEntity.ok(
                productService.getAllProducts(
                        search, categoryId, minPrice, maxPrice,
                        PageRequest.of(page, size,
                                Sort.by(sortDirection, sort))));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts() {
        return ResponseEntity.ok(
                productService.getFeaturedProducts());
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductResponse>> getSellerProducts(
            @PathVariable UUID sellerId) {
        return ResponseEntity.ok(
                productService.getSellerProducts(sellerId));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.addReview(id, userId, request));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<PageResponse<ReviewResponse>> getProductReviews(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                productService.getProductReviews(id,
                        PageRequest.of(page, size)));
    }
}