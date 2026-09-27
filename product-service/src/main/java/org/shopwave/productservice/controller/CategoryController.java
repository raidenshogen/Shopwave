package org.shopwave.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shopwave.productservice.Dto.Request.CategoryRequest;
import org.shopwave.productservice.Dto.Response.CategoryResponse;
import org.shopwave.productservice.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(
                categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                categoryService.getCategoryById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @PathVariable String slug) {
        return ResponseEntity.ok(
                categoryService.getCategoryBySlug(slug));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(
                categoryService.getAllCategories());
    }

    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(
                categoryService.getRootCategories());
    }
}