package org.shopwave.productservice.service.imp;

import lombok.RequiredArgsConstructor;
import org.shopwave.productservice.Dto.Request.ProductRequest;
import org.shopwave.productservice.Dto.Request.ReviewRequest;
import org.shopwave.productservice.Dto.Response.*;
import org.shopwave.productservice.entities.*;
import org.shopwave.productservice.repositories.*;
import org.shopwave.productservice.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImp implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ProductImageRepository imageRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(UUID sellerId,
                                         ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("SKU already exists");
        }

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .slug(generateSlug(request.getName()))
                .description(request.getDescription())
                .price(request.getPrice())
                .salePrice(request.getSalePrice())
                .sku(request.getSku())
                .stock(request.getStock())
                .sellerId(sellerId)
                .category(category)
                .brand(request.getBrand())
                .build();

        productRepository.save(product);
        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productId,
                                         UUID sellerId,
                                         ProductRequest request) {
        Product product = findProductById(productId);

        if (!product.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSalePrice(request.getSalePrice());
        product.setStock(request.getStock());
        product.setBrand(request.getBrand());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow(() ->
                            new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        productRepository.save(product);
        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productId, UUID sellerId) {
        Product product = findProductById(productId);
        if (!product.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized");
        }
        productRepository.delete(product);
    }

    @Override
    public ProductResponse getProductById(UUID productId) {
        return mapToProductResponse(findProductById(productId));
    }

    @Override
    public ProductResponse getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(this::mapToProductResponse)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));
    }

    @Override
    public PageResponse<ProductResponse> getAllProducts(
            String search,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {
         String searchTerm = (search != null) ? search : "";
        Page<Product> page = productRepository.searchProducts(
                searchTerm, categoryId, minPrice, maxPrice, pageable);

        return PageResponse.<ProductResponse>builder()
                .content(page.getContent()
                        .stream()
                        .map(this::mapToProductResponse)
                        .toList())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrue()
                .stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> getSellerProducts(UUID sellerId) {
        return productRepository.findBySellerId(sellerId)
                .stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReviewResponse addReview(UUID productId,
                                    UUID userId,
                                    ReviewRequest request) {
        Product product = findProductById(productId);

        if (reviewRepository.existsByProductIdAndUserId(
                productId, userId)) {
            throw new RuntimeException(
                    "You already reviewed this product");
        }

        Review review = Review.builder()
                .product(product)
                .userId(userId)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);
        updateProductRating(product);

        return mapToReviewResponse(review);
    }

    @Override
    public PageResponse<ReviewResponse> getProductReviews(
            UUID productId, Pageable pageable) {

        Page<Review> page = reviewRepository
                .findByProductId(productId, pageable);

        return PageResponse.<ReviewResponse>builder()
                .content(page.getContent()
                        .stream()
                        .map(this::mapToReviewResponse)
                        .toList())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public void updateStock(UUID productId, Integer quantity) {
        Product product = findProductById(productId);
        product.setStock(product.getStock() + quantity);
        if (product.getStock() <= 0) {
            product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        }
        productRepository.save(product);
    }

    // ─────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────

    private Product findProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));
    }

    private void updateProductRating(Product product) {
        Double avg = reviewRepository
                .calculateAverageRating(product.getId());
        if (avg != null) {
            product.setAvgRating(
                    BigDecimal.valueOf(avg)
                            .setScale(2, RoundingMode.HALF_UP));
        }
        product.setReviewCount(product.getReviewCount() + 1);
        productRepository.save(product);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .effectivePrice(product.getEffectivePrice())
                .sku(product.getSku())
                .stock(product.getStock())
                .inStock(product.isInStock())
                .onSale(product.isOnSale())
                .sellerId(product.getSellerId())
                .category(product.getCategory() != null ?
                        CategoryResponse.builder()
                                .id(product.getCategory().getId())
                                .name(product.getCategory().getName())
                                .slug(product.getCategory().getSlug())
                                .build() : null)
                .brand(product.getBrand())
                .status(product.getStatus().name())
                .isFeatured(product.getIsFeatured())
                .avgRating(product.getAvgRating())
                .reviewCount(product.getReviewCount())
                .images(product.getImages()
                        .stream()
                        .map(img -> ProductImageResponse.builder()
                                .id(img.getId())
                                .imageUrl(img.getImageUrl())
                                .altText(img.getAltText())
                                .isPrimary(img.getIsPrimary())
                                .displayOrder(img.getDisplayOrder())
                                .build())
                        .toList())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerified(review.getIsVerified())
                .createdAt(review.getCreatedAt())
                .build();
    }
}