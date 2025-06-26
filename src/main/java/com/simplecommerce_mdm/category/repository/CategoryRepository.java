package com.simplecommerce_mdm.category.repository;

import com.simplecommerce_mdm.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);
} 