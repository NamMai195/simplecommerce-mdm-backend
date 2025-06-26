package com.simplecommerce_mdm.category.service;

import com.simplecommerce_mdm.category.dto.CategoryRequest;
import com.simplecommerce_mdm.category.dto.CategoryResponse;
import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Integer id, CategoryRequest request);
    void deleteCategory(Integer id);
    CategoryResponse getCategoryById(Integer id);
    List<CategoryResponse> getAllCategories();
} 