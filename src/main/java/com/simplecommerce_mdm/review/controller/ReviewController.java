package com.simplecommerce_mdm.review.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Management", description = "APIs for managing product reviews")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create Review", description = "Create a new review for a product")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Creating review for product {} by user {}", request.getProductId(), userDetails.getUser().getEmail());
        
        ReviewResponse response = reviewService.createReview(request, userDetails.getUser().getId());
        
        ApiResponse<ReviewResponse> apiResponse = ApiResponse.<ReviewResponse>builder()
                .message("Review created successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update Review", description = "Update an existing review")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Updating review {} by user {}", reviewId, userDetails.getUser().getEmail());
        
        ReviewResponse response = reviewService.updateReview(reviewId, request, userDetails.getUser().getId());
        
        ApiResponse<ReviewResponse> apiResponse = ApiResponse.<ReviewResponse>builder()
                .message("Review updated successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete Review", description = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Deleting review {} by user {}", reviewId, userDetails.getUser().getEmail());
        
        reviewService.deleteReview(reviewId, userDetails.getUser().getId());
        
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .message("Review deleted successfully")
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/{reviewId}")
    @Operation(summary = "Get Review", description = "Get review details by ID")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(@PathVariable Long reviewId) {
        log.info("Getting review with id: {}", reviewId);
        
        ReviewResponse response = reviewService.getReviewById(reviewId);
        
        ApiResponse<ReviewResponse> apiResponse = ApiResponse.<ReviewResponse>builder()
                .message("Review retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get Product Reviews", description = "Get all reviews for a specific product")
    public ResponseEntity<ApiResponse<ReviewListResponse>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting reviews for product {} with pagination: page={}, size={}", productId, page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        ReviewListResponse response = reviewService.getProductReviews(productId, pageable);
        
        ApiResponse<ReviewListResponse> apiResponse = ApiResponse.<ReviewListResponse>builder()
                .message("Product reviews retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get User Reviews", description = "Get all reviews by the current user")
    public ResponseEntity<ApiResponse<ReviewListResponse>> getUserReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting reviews for user: {}", userDetails.getUser().getEmail());
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        ReviewListResponse response = reviewService.getUserReviews(userDetails.getUser().getId(), pageable);
        
        ApiResponse<ReviewListResponse> apiResponse = ApiResponse.<ReviewListResponse>builder()
                .message("User reviews retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/product/{productId}/statistics")
    @Operation(summary = "Get Product Review Statistics", description = "Get review statistics for a product")
    public ResponseEntity<ApiResponse<ReviewStatisticsResponse>> getProductReviewStatistics(@PathVariable Long productId) {
        log.info("Getting review statistics for product: {}", productId);
        
        ReviewStatisticsResponse response = reviewService.getProductReviewStatistics(productId);
        
        ApiResponse<ReviewStatisticsResponse> apiResponse = ApiResponse.<ReviewStatisticsResponse>builder()
                .message("Product review statistics retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @PostMapping("/{reviewId}/helpful")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Mark Review as Helpful", description = "Mark a review as helpful")
    public ResponseEntity<ApiResponse<Void>> markReviewAsHelpful(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Marking review {} as helpful by user {}", reviewId, userDetails.getUser().getEmail());
        
        reviewService.markReviewAsHelpful(reviewId, userDetails.getUser().getId());
        
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .message("Review marked as helpful")
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @PostMapping("/{reviewId}/report")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Report Review", description = "Report a review for inappropriate content")
    public ResponseEntity<ApiResponse<Void>> reportReview(
            @PathVariable Long reviewId,
            @RequestParam String reportReason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Reporting review {} by user {} with reason: {}", reviewId, userDetails.getUser().getEmail(), reportReason);
        
        reviewService.reportReview(reviewId, userDetails.getUser().getId(), reportReason);
        
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .message("Review reported successfully")
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
}
