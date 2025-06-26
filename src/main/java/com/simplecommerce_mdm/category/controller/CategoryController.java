package com.simplecommerce_mdm.category.controller;

import com.simplecommerce_mdm.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // TODO: Implement GET endpoint for getAllCategories

    // TODO: Implement GET endpoint for getCategoryById
} 