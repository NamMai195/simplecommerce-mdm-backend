package com.simplecommerce_mdm.shop.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.shop.dto.*;
import com.simplecommerce_mdm.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/shops")
@RequiredArgsConstructor
@Tag(name = "Shop Admin Management", description = "APIs for admin to manage shop approvals")
@SecurityRequirement(name = "bearerAuth")
public class ShopAdminController {

    private final ShopService shopService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all shops for admin", 
               description = "Admin can view all shops with advanced search and filtering")
    public ResponseEntity<ApiResponse<ShopAdminListResponse>> getShopsForAdmin(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sellerEmail,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Admin getting shops with filters - status: {}, search: {}", status, searchTerm);
        
        ShopAdminSearchRequest searchRequest = new ShopAdminSearchRequest();
        searchRequest.setSearchTerm(searchTerm);
        searchRequest.setStatus(status);
        searchRequest.setSellerEmail(sellerEmail);
        searchRequest.setCity(city);
        searchRequest.setCountry(country);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        Page<ShopAdminResponse> shopsPage = shopService.getShopsForAdmin(searchRequest);
        
        ShopAdminListResponse response = new ShopAdminListResponse();
        response.setShops(shopsPage.getContent());
        response.setCurrentPage(shopsPage.getNumber());
        response.setTotalPages(shopsPage.getTotalPages());
        response.setTotalElements(shopsPage.getTotalElements());
        response.setPageSize(shopsPage.getSize());
        response.setHasNext(shopsPage.hasNext());
        response.setHasPrevious(shopsPage.hasPrevious());
        
        return ResponseEntity.ok(
                ApiResponse.<ShopAdminListResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Shops retrieved successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/{shopId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get shop details for admin", 
               description = "Admin can view detailed shop information")
    public ResponseEntity<ApiResponse<ShopAdminResponse>> getShopByIdForAdmin(
            @Parameter(description = "Shop ID") @PathVariable Long shopId) {
        
        log.info("Admin getting shop details: {}", shopId);
        
        try {
            ShopAdminResponse shopResponse = shopService.getShopByIdForAdmin(shopId);
            
            return ResponseEntity.ok(
                    ApiResponse.<ShopAdminResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Shop details retrieved successfully")
                            .data(shopResponse)
                            .build());
                            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ShopAdminResponse>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message("Shop not found")
                            .build());
        }
    }

    @PutMapping("/{shopId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve shop", 
               description = "Admin approves shop and automatically grants SELLER role to owner")
    public ResponseEntity<ApiResponse<ShopAdminResponse>> approveShop(
            @Parameter(description = "Shop ID") @PathVariable Long shopId,
            @Valid @RequestBody(required = false) ShopApprovalRequest approvalRequest,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        
        log.info("Admin {} approving shop: {}", adminDetails.getUser().getEmail(), shopId);
        
        try {
            if (approvalRequest == null) {
                approvalRequest = new ShopApprovalRequest();
            }
            
            ShopAdminResponse shopResponse = shopService.approveShop(shopId, approvalRequest, adminDetails);
            
            return ResponseEntity.ok(
                    ApiResponse.<ShopAdminResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Shop approved successfully. SELLER role granted to owner.")
                            .data(shopResponse)
                            .build());
                            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ShopAdminResponse>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping("/{shopId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject shop", 
               description = "Admin rejects shop with reason")
    public ResponseEntity<ApiResponse<ShopAdminResponse>> rejectShop(
            @Parameter(description = "Shop ID") @PathVariable Long shopId,
            @Valid @RequestBody ShopApprovalRequest rejectionRequest,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        
        log.info("Admin {} rejecting shop: {}", adminDetails.getUser().getEmail(), shopId);
        
        try {
            ShopAdminResponse shopResponse = shopService.rejectShop(shopId, rejectionRequest, adminDetails);
            
            return ResponseEntity.ok(
                    ApiResponse.<ShopAdminResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Shop rejected successfully")
                            .data(shopResponse)
                            .build());
                            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ShopAdminResponse>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping("/{shopId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspend shop", 
               description = "Admin suspends an active shop")
    public ResponseEntity<ApiResponse<ShopAdminResponse>> suspendShop(
            @Parameter(description = "Shop ID") @PathVariable Long shopId,
            @Valid @RequestBody ShopApprovalRequest suspensionRequest,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        
        log.info("Admin {} suspending shop: {}", adminDetails.getUser().getEmail(), shopId);
        
        try {
            ShopAdminResponse shopResponse = shopService.suspendShop(shopId, suspensionRequest, adminDetails);
            
            return ResponseEntity.ok(
                    ApiResponse.<ShopAdminResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Shop suspended successfully")
                            .data(shopResponse)
                            .build());
                            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ShopAdminResponse>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping("/{shopId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivate shop", 
               description = "Admin reactivates a suspended shop")
    public ResponseEntity<ApiResponse<ShopAdminResponse>> reactivateShop(
            @Parameter(description = "Shop ID") @PathVariable Long shopId,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        
        log.info("Admin {} reactivating shop: {}", adminDetails.getUser().getEmail(), shopId);
        
        try {
            ShopAdminResponse shopResponse = shopService.reactivateShop(shopId, adminDetails);
            
            return ResponseEntity.ok(
                    ApiResponse.<ShopAdminResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Shop reactivated successfully")
                            .data(shopResponse)
                            .build());
                            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ShopAdminResponse>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        }
    }
} 