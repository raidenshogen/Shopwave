package org.shopwave.productservice.service.imp;

import lombok.RequiredArgsConstructor;
import org.shopwave.productservice.Dto.Request.CategoryRequest;
import org.shopwave.productservice.Dto.Response.CategoryResponse;
import org.shopwave.productservice.entities.Category;
import org.shopwave.productservice.repositories.CategoryRepository;
import org.shopwave.productservice.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImp implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(generateSlug(request.getName()))
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .build();

        if (request.getParentId() != null) {
            Category parent = categoryRepository
                    .findById(request.getParentId())
                    .orElseThrow(() ->
                            new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        categoryRepository.save(category);
        return mapToCategoryResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID categoryId,
                                           CategoryRequest request) {
        Category category = findCategoryById(categoryId);
        category.setName(request.getName());
        category.setSlug(generateSlug(request.getName()));
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        categoryRepository.save(category);
        return mapToCategoryResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category category = findCategoryById(categoryId);
        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponse getCategoryById(UUID categoryId) {
        return mapToCategoryResponse(findCategoryById(categoryId));
    }

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::mapToCategoryResponse)
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }

    // ─────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────

    private Category findCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParent() != null ?
                        category.getParent().getId() : null)
                .isActive(category.getIsActive())
                .build();
    }
}