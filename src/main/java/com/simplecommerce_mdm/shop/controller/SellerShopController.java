package com.simplecommerce_mdm.shop.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.shop.dto.ShopResponse;
import com.simplecommerce_mdm.shop.dto.ShopAddressUpdateRequest;
import com.simplecommerce_mdm.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/seller/shop")
@RequiredArgsConstructor
@Tag(name = "Seller Shop Management", description = "APIs for sellers to manage their approved shop")
@SecurityRequirement(name = "bearerAuth")
public class SellerShopController {

    private final ShopService shopService;

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get seller's shop details", 
               description = "Seller can view their approved shop details")
    public ResponseEntity<ApiResponse<ShopResponse>> getSellerShop(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Seller {} getting their shop details", userDetails.getUser().getEmail());
        
        try {
            ShopResponse shopResponse = shopService.getSellerShop(userDetails);
            
            return ResponseEntity.ok(
                    ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Shop details retrieved successfully")
                            .data(shopResponse)
                            .build());
                            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message("Seller has no approved shop")
                            .build());
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get seller's shop statistics", 
               description = "Get basic stats about seller's shop (products, orders, etc.)")
    public ResponseEntity<ApiResponse<Object>> getSellerShopStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Seller {} getting shop statistics", userDetails.getUser().getEmail());
        
        try {
            // Placeholder for shop statistics
            // This would include product count, order count, revenue, etc.
            Object stats = shopService.getSellerShopStats(userDetails);
            
            return ResponseEntity.ok(
                    ApiResponse.<Object>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Shop statistics retrieved successfully")
                            .data(stats)
                            .build());
                            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Object>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message("Unable to retrieve shop statistics")
                            .build());
        }
    }

    @PutMapping("/address")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Update shop address", 
               description = "Seller can update their shop address")
    public ResponseEntity<ApiResponse<ShopResponse>> updateShopAddress(
            @Valid @RequestBody ShopAddressUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Seller {} updating shop address to userAddressId: {}", userDetails.getUser().getEmail(), request.getUserAddressId());
        
        try {
            ShopResponse shopResponse = shopService.updateShopAddress(request.getUserAddressId(), userDetails);
            
            return ResponseEntity.ok(
                    ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Shop address updated successfully")
                            .data(shopResponse)
                            .build());
                            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message("Failed to update shop address: " + e.getMessage())
                            .build());
        }
    }
} 