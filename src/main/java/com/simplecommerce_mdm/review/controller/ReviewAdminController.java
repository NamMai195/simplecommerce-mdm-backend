package com.simplecommerce_mdm.review.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Review Admin Management", description = "Admin APIs for managing product reviews")
public class ReviewAdminController {
    
    private final ReviewService reviewService;
    
    @GetMapping
    @Operation(summary = "Get All Reviews", description = "Get all reviews with pagination (Admin only)")
    public ResponseEntity<ApiResponse<ReviewListResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin getting all reviews with pagination: page={}, size={}", page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        ReviewListResponse response = reviewService.getAllReviews(pageable);
        
        ApiResponse<ReviewListResponse> apiResponse = ApiResponse.<ReviewListResponse>builder()
                .message("All reviews retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/{reviewId}")
    @Operation(summary = "Get Review Details", description = "Get detailed information about a specific review (Admin only)")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewDetails(@PathVariable Long reviewId) {
        log.info("Admin getting review details for id: {}", reviewId);
        
        ReviewResponse response = reviewService.getReviewById(reviewId);
        
        ApiResponse<ReviewResponse> apiResponse = ApiResponse.<ReviewResponse>builder()
                .message("Review details retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Get Pending Reviews", description = "Get reviews that need admin approval (Admin only)")
    public ResponseEntity<ApiResponse<ReviewListResponse>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin getting pending reviews with pagination: page={}, size={}", page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        ReviewListResponse response = reviewService.getPendingReviews(pageable);
        
        ApiResponse<ReviewListResponse> apiResponse = ApiResponse.<ReviewListResponse>builder()
                .message("Pending reviews retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/reported")
    @Operation(summary = "Get Reported Reviews", description = "Get reviews that have been reported (Admin only)")
    public ResponseEntity<ApiResponse<ReviewListResponse>> getReportedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin getting reported reviews with pagination: page={}, size={}", page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        ReviewListResponse response = reviewService.getReportedReviews(pageable);
        
        ApiResponse<ReviewListResponse> apiResponse = ApiResponse.<ReviewListResponse>builder()
                .message("Reported reviews retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @PutMapping("/{reviewId}/moderate")
    @Operation(summary = "Moderate Review", description = "Approve or reject a review (Admin only)")
    public ResponseEntity<ApiResponse<ReviewResponse>> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam Boolean isApproved,
            @RequestParam(required = false) String moderatorNotes) {
        
        log.info("Admin moderating review {}: approved={}", reviewId, isApproved);
        
        ReviewResponse response = reviewService.moderateReview(reviewId, isApproved, moderatorNotes);
        
        String message = isApproved ? "Review approved successfully" : "Review rejected successfully";
        
        ApiResponse<ReviewResponse> apiResponse = ApiResponse.<ReviewResponse>builder()
                .message(message)
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete Review (Admin)", description = "Delete a review by admin (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteReviewByAdmin(@PathVariable Long reviewId) {
        log.info("Admin deleting review: {}", reviewId);
        
        reviewService.deleteReviewByAdmin(reviewId);
        
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .message("Review deleted successfully by admin")
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/statistics/overview")
    @Operation(summary = "Get Review Statistics Overview", description = "Get overall review statistics (Admin only)")
    public ResponseEntity<ApiResponse<ReviewStatisticsResponse>> getReviewStatisticsOverview() {
        log.info("Admin getting review statistics overview");
        
        // This would need a new service method for overall statistics
        // For now, return a placeholder response
        ReviewStatisticsResponse response = ReviewStatisticsResponse.builder()
                .totalReviews(0L)
                .averageRating(0.0)
                .build();
        
        ApiResponse<ReviewStatisticsResponse> apiResponse = ApiResponse.<ReviewStatisticsResponse>builder()
                .message("Review statistics overview retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @PostMapping("/cleanup/orphaned")
    @Operation(summary = "Cleanup Orphaned Reviews", description = "Remove reviews with invalid references (Admin only)")
    public ResponseEntity<ApiResponse<String>> cleanupOrphanedReviews() {
        log.info("Admin starting cleanup of orphaned reviews");
        
        try {
            reviewService.cleanupOrphanedReviews();
            
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .message("Orphaned reviews cleanup completed successfully")
                    .data("Cleanup process finished")
                    .build();
            
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error during orphaned reviews cleanup: {}", e.getMessage());
            
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .message("Error during cleanup: " + e.getMessage())
                    .build();
            
            return ResponseEntity.internalServerError().body(apiResponse);
        }
    }
}
