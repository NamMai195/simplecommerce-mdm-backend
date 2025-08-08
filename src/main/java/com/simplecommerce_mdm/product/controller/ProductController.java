package com.simplecommerce_mdm.product.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.product.dto.ProductBuyerListResponse;
import com.simplecommerce_mdm.product.dto.ProductBuyerResponse;
import com.simplecommerce_mdm.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Controller", description = "Public APIs for browsing products")
@Slf4j(topic = "PRODUCT-CONTROLLER")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/featured")
    @Operation(summary = "Get Featured Products", 
               description = "Get list of featured products available for purchase")
    public ResponseEntity<ApiResponse<ProductBuyerListResponse>> getFeaturedProducts(
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 10)") 
            @RequestParam(defaultValue = "10") Integer size,
            
            @Parameter(description = "Sort field (default createdAt)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (default desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Getting featured products: page={}, size={}, sortBy={}, sortDirection={}", 
                 page, size, sortBy, sortDirection);

        ProductBuyerListResponse featuredProducts = productService.getFeaturedProducts(
                page, size, sortBy, sortDirection);

        ApiResponse<ProductBuyerListResponse> response = ApiResponse.<ProductBuyerListResponse>builder()
                .message("Featured products retrieved successfully")
                .data(featuredProducts)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Search Products", 
               description = "Search all approved products with pagination")
    public ResponseEntity<ApiResponse<ProductBuyerListResponse>> getProducts(
            @Parameter(description = "Search term for product name or SKU") 
            @RequestParam(defaultValue = "") String searchTerm,
            
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 20)") 
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field (default createdAt)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (default desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Searching products: searchTerm='{}', page={}, size={}, sortBy={}, sortDirection={}", 
                 searchTerm, page, size, sortBy, sortDirection);

        ProductBuyerListResponse products = productService.getProducts(
                searchTerm, page, size, sortBy, sortDirection);

        ApiResponse<ProductBuyerListResponse> response = ApiResponse.<ProductBuyerListResponse>builder()
                .message("Products retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Product Details", 
               description = "Get detailed information about a specific product")
    public ResponseEntity<ApiResponse<ProductBuyerResponse>> getProductById(
            @Parameter(description = "Product ID") 
            @PathVariable Long id) {

        log.info("Getting product details for ID: {}", id);

        ProductBuyerResponse product = productService.getProductById(id);

        ApiResponse<ProductBuyerResponse> response = ApiResponse.<ProductBuyerResponse>builder()
                .message("Product details retrieved successfully")
                .data(product)
                .build();

        return ResponseEntity.ok(response);
    }
} 