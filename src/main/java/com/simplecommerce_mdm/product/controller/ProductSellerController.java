package com.simplecommerce_mdm.product.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.product.dto.ProductCreateRequest;
import com.simplecommerce_mdm.product.dto.ProductListResponse;
import com.simplecommerce_mdm.product.dto.ProductResponse;
import com.simplecommerce_mdm.product.dto.ProductSearchRequest;
import com.simplecommerce_mdm.product.dto.ProductUpdateRequest;
import com.simplecommerce_mdm.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import com.simplecommerce_mdm.product.dto.ProductImageDeleteRequest;
import com.simplecommerce_mdm.product.dto.ProductImageListResponse;

@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Product Seller Controller")
@Slf4j(topic = "PRODUCT-SELLER-CONTROLLER")
public class ProductSellerController {

    private final ProductService productService;

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Create Product (JSON)", description = "API create new product using JSON")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Creating product (JSON) for seller: {}", userDetails.getUser().getEmail());
        
        ProductResponse createdProduct = productService.createProduct(request, null, userDetails);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Product created successfully and is pending approval.")
                .data(createdProduct)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Create Product with Images", description = "API create new product with image upload")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestPart("product") ProductCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Creating product (Multipart) for seller: {}", userDetails.getUser().getEmail());
        
        ProductResponse createdProduct = productService.createProduct(request, images, userDetails);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Product created successfully and is pending approval.")
                .data(createdProduct)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get Seller Products", description = "API get all products for seller with search and pagination")
    public ResponseEntity<ApiResponse<ProductListResponse>> getSellerProducts(
            @RequestParam(defaultValue = "") String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Getting products for seller: {} with search: '{}'", userDetails.getUser().getEmail(), searchTerm);

        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setSearchTerm(searchTerm);
        if (status != null && !status.trim().isEmpty()) {
            try {
                searchRequest.setStatus(com.simplecommerce_mdm.common.enums.ProductStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid status value, ignore it
            }
        }
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);

        ProductListResponse productList = productService.getSellerProducts(searchRequest, userDetails);

        ApiResponse<ProductListResponse> response = ApiResponse.<ProductListResponse>builder()
                .message("Products retrieved successfully")
                .data(productList)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Product by ID", description = "API get product detail by ID for seller")
    public ResponseEntity<ApiResponse<ProductResponse>> getSellerProductById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Getting product {} for seller: {}", id, userDetails.getUser().getEmail());

        ProductResponse product = productService.getSellerProductById(id, userDetails);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .message("Product retrieved successfully")
                .data(product)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Update Product (JSON)", description = "API update product using JSON")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Updating product {} (JSON) for seller: {}", id, userDetails.getUser().getEmail());

        ProductResponse updatedProduct = productService.updateProduct(id, request, null, userDetails);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .message("Product updated successfully")
                .data(updatedProduct)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Update Product with Images", description = "API update product with new image upload")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("product") ProductUpdateRequest request,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Updating product {} (Multipart) for seller: {}", id, userDetails.getUser().getEmail());

        ProductResponse updatedProduct = productService.updateProduct(id, request, newImages, userDetails);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .message("Product updated successfully")
                .data(updatedProduct)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Product", description = "API delete product for seller")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Deleting product {} for seller: {}", id, userDetails.getUser().getEmail());

        productService.deleteProduct(id, userDetails);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Product deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}/variants/{variantId}")
    @Operation(summary = "Delete Product Variant", description = "API delete specific variant from product for seller")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Deleting variant {} from product {} for seller: {}", variantId, productId, userDetails.getUser().getEmail());

        productService.deleteVariant(productId, variantId, userDetails);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Variant deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}/images")
    @Operation(summary = "Delete Product Images", description = "Delete selected images of a product by IDs (Seller)")
    public ResponseEntity<ApiResponse<Void>> deleteProductImages(
            @PathVariable Long productId,
            @RequestBody ProductImageDeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Deleting product images for product {} by seller {}", productId, userDetails.getUser().getEmail());

        productService.deleteProductImages(productId, request.getImageIds(), userDetails);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Product images deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}/images")
    @Operation(summary = "List Product Images", description = "List images of a product with IDs for deletion (Seller)")
    public ResponseEntity<ApiResponse<ProductImageListResponse>> listProductImages(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ProductImageListResponse list = productService.listProductImages(productId, userDetails);

        ApiResponse<ProductImageListResponse> response = ApiResponse.<ProductImageListResponse>builder()
                .message("Product images retrieved successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }
} 