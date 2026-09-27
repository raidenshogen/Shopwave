package org.shopwave.productservice.service;

import org.shopwave.productservice.Dto.Request.CategoryRequest;
import org.shopwave.productservice.Dto.Response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(UUID categoryId,
                                    CategoryRequest request);
    void deleteCategory(UUID categoryId);
    CategoryResponse getCategoryById(UUID categoryId);
    CategoryResponse getCategoryBySlug(String slug);
    List<CategoryResponse> getAllCategories();
    List<CategoryResponse> getRootCategories();
}