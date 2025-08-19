package com.simplecommerce_mdm.shop.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.shop.dto.ShopCreateRequest;
import com.simplecommerce_mdm.shop.dto.ShopResponse;
import com.simplecommerce_mdm.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/user/shop")
@RequiredArgsConstructor
@Tag(name = "User Shop Management", description = "APIs for users to create and manage their shop")
@SecurityRequirement(name = "bearerAuth")
public class UserShopController {

    private final ShopService shopService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a new shop", 
               description = "User can create only ONE shop. Shop will be pending approval.")
    public ResponseEntity<ApiResponse<ShopResponse>> createShop(
            @Valid @RequestBody ShopCreateRequest createRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("User {} creating shop: {}", userDetails.getUser().getEmail(), createRequest.getName());
        
        try {
            ShopResponse shopResponse = shopService.createShop(createRequest, userDetails);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.CREATED.value())
                            .message("Shop created successfully and pending approval")
                            .data(shopResponse)
                            .build());
                            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.CONFLICT.value())
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping("/logo")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update shop logo", description = "Upload a new shop logo (avatar)")
    public ResponseEntity<ApiResponse<ShopResponse>> updateShopLogo(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ShopResponse shopResponse = shopService.updateShopLogo(file, userDetails);
        return ResponseEntity.ok(
                ApiResponse.<ShopResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Shop logo updated successfully")
                        .data(shopResponse)
                        .build());
    }

    @PutMapping("/cover")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update shop cover image", description = "Upload a new background image for shop")
    public ResponseEntity<ApiResponse<ShopResponse>> updateShopCover(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ShopResponse shopResponse = shopService.updateShopCover(file, userDetails);
        return ResponseEntity.ok(
                ApiResponse.<ShopResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Shop cover updated successfully")
                        .data(shopResponse)
                        .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user's shop", 
               description = "Get the shop owned by the current user")
    public ResponseEntity<ApiResponse<ShopResponse>> getUserShop(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("User {} getting their shop", userDetails.getUser().getEmail());
        
        try {
            ShopResponse shopResponse = shopService.getUserShop(userDetails);
            
            return ResponseEntity.ok(
                    ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Shop retrieved successfully")
                            .data(shopResponse)
                            .build());
                            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message("User has no shop")
                            .build());
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update user's shop", 
               description = "Update shop details (only if not approved yet)")
    public ResponseEntity<ApiResponse<ShopResponse>> updateShop(
            @Valid @RequestBody ShopCreateRequest updateRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("User {} updating their shop", userDetails.getUser().getEmail());
        
        try {
            ShopResponse shopResponse = shopService.updateShop(updateRequest, userDetails);
            
            return ResponseEntity.ok(
                    ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Shop updated successfully")
                            .data(shopResponse)
                            .build());
                            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ShopResponse>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message("User has no shop")
                            .build());
        }
    }
} 