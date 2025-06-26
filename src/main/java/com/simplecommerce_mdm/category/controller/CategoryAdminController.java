package com.simplecommerce_mdm.category.controller;

import com.simplecommerce_mdm.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryAdminController {

    private final CategoryService categoryService;

    // TODO: Implement POST endpoint for createCategory
    
    // TODO: Implement PUT endpoint for updateCategory

    // TODO: Implement DELETE endpoint for deleteCategory
} 