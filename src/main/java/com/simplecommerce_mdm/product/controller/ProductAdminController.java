package com.simplecommerce_mdm.product.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.product.dto.ProductAdminResponse;
import com.simplecommerce_mdm.product.dto.ProductAdminSearchRequest;
import com.simplecommerce_mdm.product.dto.ProductApprovalRequest;
import com.simplecommerce_mdm.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Product Admin Controller", description = "Admin APIs for product approval management")
@Slf4j(topic = "PRODUCT-ADMIN-CONTROLLER")
public class ProductAdminController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get All Products for Admin", 
               description = "Get all products with filtering and pagination for admin management")
    public ResponseEntity<ApiResponse<Page<ProductAdminResponse>>> getAllProducts(
            @Parameter(description = "Search term for product name or SKU") 
            @RequestParam(defaultValue = "") String searchTerm,
            
            @Parameter(description = "Filter by product status") 
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Filter by shop ID") 
            @RequestParam(required = false) Long shopId,
            
            @Parameter(description = "Filter by category ID") 
            @RequestParam(required = false) Integer categoryId,
            
            @Parameter(description = "Filter by seller email") 
            @RequestParam(required = false) String sellerEmail,
            
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 20)") 
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field (default createdAt)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (default desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Admin getting products with filters: search='{}', status={}, shopId={}, categoryId={}", 
                searchTerm, status, shopId, categoryId);

        ProductAdminSearchRequest searchRequest = new ProductAdminSearchRequest();
        searchRequest.setSearchTerm(searchTerm);
        
        if (status != null && !status.trim().isEmpty()) {
            try {
                searchRequest.setStatus(com.simplecommerce_mdm.common.enums.ProductStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}", status);
            }
        }
        
        searchRequest.setShopId(shopId);
        searchRequest.setCategoryId(categoryId);
        searchRequest.setSellerEmail(sellerEmail);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);

        Page<ProductAdminResponse> products = productService.getProductsForAdmin(searchRequest);

        ApiResponse<Page<ProductAdminResponse>> response = ApiResponse.<Page<ProductAdminResponse>>builder()
                .message("Products retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get Pending Products", 
               description = "Get all products with PENDING_APPROVAL status for admin review")
    public ResponseEntity<ApiResponse<Page<ProductAdminResponse>>> getPendingProducts(
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

        log.info("Admin getting pending products for approval with search: '{}'", searchTerm);

        ProductAdminSearchRequest searchRequest = new ProductAdminSearchRequest();
        searchRequest.setSearchTerm(searchTerm);
        searchRequest.setStatus(com.simplecommerce_mdm.common.enums.ProductStatus.PENDING_APPROVAL);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);

        Page<ProductAdminResponse> products = productService.getProductsForAdmin(searchRequest);

        ApiResponse<Page<ProductAdminResponse>> response = ApiResponse.<Page<ProductAdminResponse>>builder()
                .message("Pending products retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Product by ID for Admin", 
               description = "Get detailed product information for admin review")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> getProductById(
            @Parameter(description = "Product ID") 
            @PathVariable Long id) {

        log.info("Admin getting product details for ID: {}", id);

        ProductAdminResponse product = productService.getProductByIdForAdmin(id);

        ApiResponse<ProductAdminResponse> response = ApiResponse.<ProductAdminResponse>builder()
                .message("Product retrieved successfully")
                .data(product)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve Product", 
               description = "Approve a pending product to make it available for sale")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> approveProduct(
            @Parameter(description = "Product ID") 
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {

        log.info("Admin {} approving product {}", adminDetails.getUser().getEmail(), id);

        ProductAdminResponse approvedProduct = productService.approveProduct(id, adminDetails);

        ApiResponse<ProductAdminResponse> response = ApiResponse.<ProductAdminResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Product approved successfully")
                .data(approvedProduct)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject Product", 
               description = "Reject a pending product with a reason")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> rejectProduct(
            @Parameter(description = "Product ID") 
            @PathVariable Long id,
            @Valid @RequestBody ProductApprovalRequest rejectionRequest,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {

        log.info("Admin {} rejecting product {} with reason: {}", 
                adminDetails.getUser().getEmail(), id, rejectionRequest.getRejectionReason());

        ProductAdminResponse rejectedProduct = productService.rejectProduct(id, rejectionRequest, adminDetails);

        ApiResponse<ProductAdminResponse> response = ApiResponse.<ProductAdminResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Product rejected successfully")
                .data(rejectedProduct)
                .build();

        return ResponseEntity.ok(response);
    }
} 