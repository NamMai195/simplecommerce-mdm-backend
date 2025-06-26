package com.simplecommerce_mdm.category.service.impl;

import com.simplecommerce_mdm.category.dto.CategoryRequest;
import com.simplecommerce_mdm.category.dto.CategoryResponse;
import com.simplecommerce_mdm.category.repository.CategoryRepository;
import com.simplecommerce_mdm.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        // TODO: Implement logic to create category
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CategoryResponse updateCategory(Integer id, CategoryRequest request) {
        // TODO: Implement logic to update category
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteCategory(Integer id) {
        // TODO: Implement logic for soft-delete
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CategoryResponse getCategoryById(Integer id) {
        // TODO: Implement logic to get category by ID
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        // TODO: Implement logic to get all categories
        throw new UnsupportedOperationException("Not implemented yet");
    }
} 