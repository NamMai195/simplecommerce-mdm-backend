package com.simplecommerce_mdm.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.product.dto.ProductCreateRequest;
import com.simplecommerce_mdm.product.dto.ProductResponse;
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
    private final ObjectMapper objectMapper; // Spring Boot auto-configures this bean

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            ProductCreateRequest productRequest = objectMapper.readValue(productJson, ProductCreateRequest.class);

            ProductResponse createdProduct = productService.createProduct(productRequest, images, userDetails);

            ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                    .statusCode(HttpStatus.CREATED.value())
                    .message("Product created successfully and is pending approval.")
                    .data(createdProduct)
                    .build();
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (JsonProcessingException e) {
            ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("Invalid product data format: " + e.getMessage())
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
} 