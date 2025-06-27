package com.simplecommerce_mdm.category.controller;

import com.simplecommerce_mdm.category.dto.CategoryRequest;
import com.simplecommerce_mdm.category.dto.CategoryResponse;
import com.simplecommerce_mdm.category.service.CategoryService;
import com.simplecommerce_mdm.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryAdminController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse createdCategory = categoryService.createCategory(request);
        ApiResponse<CategoryResponse> response = ApiResponse.<CategoryResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Category created successfully")
                .data(createdCategory)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryRequest request) {
        CategoryResponse updatedCategory = categoryService.updateCategory(id, request);
        ApiResponse<CategoryResponse> response = ApiResponse.<CategoryResponse>builder()
                .message("Category updated successfully")
                .data(updatedCategory)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Category deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Integer id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        ApiResponse<CategoryResponse> response = ApiResponse.<CategoryResponse>builder()
                .data(category)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        ApiResponse<List<CategoryResponse>> response = ApiResponse.<List<CategoryResponse>>builder()
                .data(categories)
                .build();
        return ResponseEntity.ok(response);
    }
} 