package com.simplecommerce_mdm.review.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.product.repository.ShopRepository;
import com.simplecommerce_mdm.review.dto.*;
import com.simplecommerce_mdm.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/reviews")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Review Seller Management", description = "Seller APIs for managing product reviews")
public class ReviewSellerController {
    
    private final ReviewService reviewService;
    private final ShopRepository shopRepository;
    
    @GetMapping
    @Operation(summary = "Get Shop Reviews", description = "Get all reviews for products in the seller's shop")
    public ResponseEntity<ApiResponse<ReviewListResponse>> getShopReviews(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Seller {} getting shop reviews with pagination: page={}, size={}", 
                sellerDetails.getUser().getEmail(), page, size);
        
        // Get seller's shop
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new RuntimeException("Shop not found for the current seller"));
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        ReviewListResponse response = reviewService.getShopReviews(shop.getId(), pageable);
        
        ApiResponse<ReviewListResponse> apiResponse = ApiResponse.<ReviewListResponse>builder()
                .message("Shop reviews retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get Product Reviews in Shop", description = "Get reviews for a specific product in the seller's shop")
    public ResponseEntity<ApiResponse<ReviewListResponse>> getShopProductReviews(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Seller {} getting reviews for product {} in shop with pagination: page={}, size={}", 
                sellerDetails.getUser().getEmail(), productId, page, size);
        
        // Get seller's shop
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new RuntimeException("Shop not found for the current seller"));
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Only show reviews for products in the current seller's shop
        ReviewListResponse response = reviewService.getShopProductReviews(shop.getId(), productId, pageable);
        
        ApiResponse<ReviewListResponse> apiResponse = ApiResponse.<ReviewListResponse>builder()
                .message("Product reviews in shop retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get Shop Review Statistics", description = "Get review statistics for the seller's shop")
    public ResponseEntity<ApiResponse<ReviewStatisticsResponse>> getShopReviewStatistics(
            @AuthenticationPrincipal CustomUserDetails sellerDetails) {
        
        log.info("Seller {} getting shop review statistics", sellerDetails.getUser().getEmail());
        
        // Get seller's shop
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new RuntimeException("Shop not found for the current seller"));
        
        ReviewStatisticsResponse response = reviewService.getShopReviewStatisticsForSeller(shop.getId());
        
        ApiResponse<ReviewStatisticsResponse> apiResponse = ApiResponse.<ReviewStatisticsResponse>builder()
                .message("Shop review statistics retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/product/{productId}/statistics")
    @Operation(summary = "Get Product Review Statistics in Shop", description = "Get review statistics for a specific product in the seller's shop")
    public ResponseEntity<ApiResponse<ReviewStatisticsResponse>> getShopProductReviewStatistics(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails sellerDetails) {
        
        log.info("Seller {} getting review statistics for product {} in shop", 
                sellerDetails.getUser().getEmail(), productId);
        
        ReviewStatisticsResponse response = reviewService.getProductReviewStatistics(productId);
        
        ApiResponse<ReviewStatisticsResponse> apiResponse = ApiResponse.<ReviewStatisticsResponse>builder()
                .message("Product review statistics in shop retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
}
