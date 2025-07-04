package com.simplecommerce_mdm.product.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.product.dto.ProductCreateRequest;
import com.simplecommerce_mdm.product.dto.ProductListResponse;
import com.simplecommerce_mdm.product.dto.ProductResponse;
import com.simplecommerce_mdm.product.dto.ProductSearchRequest;
import com.simplecommerce_mdm.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class ProductSellerController {

    private final ProductService productService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestPart("product") ProductCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ProductResponse createdProduct = productService.createProduct(request, images, userDetails);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Product created successfully and is pending approval.")
                .data(createdProduct)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProductListResponse>> getSellerProducts(
            @RequestParam(defaultValue = "") String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

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
} 