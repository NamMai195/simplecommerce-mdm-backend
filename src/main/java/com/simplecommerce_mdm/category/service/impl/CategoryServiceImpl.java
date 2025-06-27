package com.simplecommerce_mdm.category.service.impl;

import com.simplecommerce_mdm.category.dto.CategoryRequest;
import com.simplecommerce_mdm.category.dto.CategoryResponse;
import com.simplecommerce_mdm.category.mapper.CategoryMapper;
import com.simplecommerce_mdm.category.model.Category;
import com.simplecommerce_mdm.category.repository.CategoryRepository;
import com.simplecommerce_mdm.category.service.CategoryService;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists.");
        }

        Category category = categoryMapper.toCategory(request);
        category.setSlug(generateSlug(request.getName()));

        if (request.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
            category.setParent(parentCategory);
        }

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Integer id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if the new name is already taken by another category
        if (!Objects.equals(category.getName(), request.getName()) && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists.");
        }

        // Update fields from request
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(generateSlug(request.getName())); // Regenerate slug if name changes

        // Update parent category
        if (request.getParentId() != null) {
            // Prevent setting a category as its own parent
            if (Objects.equals(id, request.getParentId())) {
                throw new IllegalArgumentException("A category cannot be its own parent.");
            }
            Category parentCategory = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
            category.setParent(parentCategory);
        } else {
            category.setParent(null); // Remove parent
        }

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        // Thanks to @SQLDelete, this will perform a soft-delete
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    private String generateSlug(String name) {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        String nowhitespace = WHITESPACE.matcher(name).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
} 