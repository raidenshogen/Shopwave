package org.shopwave.productservice.repositories;

import org.shopwave.productservice.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository
        extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);
    boolean existsByName(String name);
    List<Category> findByParentIsNull();
    List<Category> findByIsActiveTrue();
}