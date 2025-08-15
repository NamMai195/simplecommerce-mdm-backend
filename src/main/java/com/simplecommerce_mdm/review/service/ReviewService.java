package com.simplecommerce_mdm.review.service;

import com.simplecommerce_mdm.review.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    
    // Tạo review mới
    ReviewResponse createReview(ReviewCreateRequest request, Long userId);
    
    // Cập nhật review
    ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request, Long userId);
    
    // Xóa review
    void deleteReview(Long reviewId, Long userId);
    
    // Lấy review theo ID
    ReviewResponse getReviewById(Long reviewId);
    
    // Lấy reviews của product với pagination
    ReviewListResponse getProductReviews(Long productId, Pageable pageable);
    
    // Lấy reviews của user hiện tại
    ReviewListResponse getUserReviews(Long userId, Pageable pageable);
    
    // Lấy review statistics của product
    ReviewStatisticsResponse getProductReviewStatistics(Long productId);
    
    // Lấy review statistics của shop
    ReviewStatisticsResponse getShopReviewStatistics(Long shopId);
    
    // Admin: Lấy tất cả reviews với pagination
    ReviewListResponse getAllReviews(Pageable pageable);
    
    // Admin: Lấy reviews chưa approved
    ReviewListResponse getPendingReviews(Pageable pageable);
    
    // Admin: Lấy reviews bị reported
    ReviewListResponse getReportedReviews(Pageable pageable);
    
    // Admin: Approve/Reject review
    ReviewResponse moderateReview(Long reviewId, Boolean isApproved, String moderatorNotes);
    
    // Admin: Xóa review
    void deleteReviewByAdmin(Long reviewId);
    
    // Seller: Lấy reviews của shop
    ReviewListResponse getShopReviews(Long shopId, Pageable pageable);
    
    // Seller: Lấy reviews của product trong shop
    ReviewListResponse getShopProductReviews(Long shopId, Long productId, Pageable pageable);
    
    // Seller: Lấy review statistics của shop
    ReviewStatisticsResponse getShopReviewStatisticsForSeller(Long shopId);
    
    // Mark review as helpful
    void markReviewAsHelpful(Long reviewId, Long userId);
    
    // Report review
    void reportReview(Long reviewId, Long userId, String reportReason);
}
