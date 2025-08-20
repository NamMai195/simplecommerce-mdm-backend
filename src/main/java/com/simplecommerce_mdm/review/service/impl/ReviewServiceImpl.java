package com.simplecommerce_mdm.review.service.impl;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.exception.InvalidDataException;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.order.model.Order;
import com.simplecommerce_mdm.order.repository.OrderRepository;
import com.simplecommerce_mdm.product.model.Product;
import com.simplecommerce_mdm.product.repository.ProductRepository;
import com.simplecommerce_mdm.review.dto.*;
import com.simplecommerce_mdm.review.model.Review;
import com.simplecommerce_mdm.review.repository.ReviewRepository;
import com.simplecommerce_mdm.review.service.ReviewService;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public ReviewResponse createReview(ReviewCreateRequest request, Long userId) {
        log.info("Creating review for product {} by user {}", request.getProductId(), userId);
        
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        
        // Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
        
        // Validate business rules
        validateReviewCreation(user, product, order);
        
        // Check if user already reviewed this product
        if (reviewRepository.findByUserAndProduct(user, product).isPresent()) {
            throw new InvalidDataException("User has already reviewed this product");
        }
        
        // Create review
        Review review = Review.builder()
                .user(user)
                .product(product)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .isVerifiedPurchase(true)
                .isApproved(true)
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        // Update product rating
        updateProductRating(product);
        
        log.info("Review created successfully with id: {}", savedReview.getId());
        return mapToReviewResponse(savedReview);
    }
    
    @Override
    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request, Long userId) {
        log.info("Updating review {} by user {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        // Check ownership
        if (!review.getUser().getId().equals(userId)) {
            throw new InvalidDataException("You can only update your own reviews");
        }
        
        // Update review
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        Review updatedReview = reviewRepository.save(review);
        
        // Update product rating
        updateProductRating(review.getProduct());
        
        log.info("Review updated successfully");
        return mapToReviewResponse(updatedReview);
    }
    
    @Override
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Deleting review {} by user {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        // Check ownership
        if (!review.getUser().getId().equals(userId)) {
            throw new InvalidDataException("You can only delete your own reviews");
        }
        
        Product product = review.getProduct();
        reviewRepository.delete(review);
        
        // Update product rating
        updateProductRating(product);
        
        log.info("Review deleted successfully");
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        return mapToReviewResponse(review);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getProductReviews(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        Page<Review> reviewPage = reviewRepository.findByProductOrderByCreatedAtDesc(product, pageable);
        
        return buildReviewListResponse(reviewPage, product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getUserReviews(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Page<Review> reviewPage = reviewRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        return buildReviewListResponse(reviewPage, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewStatisticsResponse getProductReviewStatistics(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        return buildProductReviewStatistics(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewStatisticsResponse getShopReviewStatistics(Long shopId) {
        return buildShopReviewStatistics(shopId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getAllReviews(Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAll(pageable);
        return buildReviewListResponse(reviewPage, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getPendingReviews(Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByIsApprovedFalseOrderByCreatedAtDesc(pageable);
        return buildReviewListResponse(reviewPage, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getReportedReviews(Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByIsReportedTrueOrderByCreatedAtDesc(pageable);
        return buildReviewListResponse(reviewPage, null);
    }
    
    @Override
    public ReviewResponse moderateReview(Long reviewId, Boolean isApproved, String moderatorNotes) {
        log.info("Moderating review {}: approved={}", reviewId, isApproved);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        review.setIsApproved(isApproved);
        review.setModeratorNotes(moderatorNotes);
        
        Review updatedReview = reviewRepository.save(review);
        
        // Update product rating if approval status changed
        if (review.getIsApproved() != isApproved) {
            updateProductRating(review.getProduct());
        }
        
        log.info("Review moderation completed");
        return mapToReviewResponse(updatedReview);
    }
    
    @Override
    public void deleteReviewByAdmin(Long reviewId) {
        log.info("Admin deleting review {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        Product product = review.getProduct();
        reviewRepository.delete(review);
        
        // Update product rating
        updateProductRating(product);
        
        log.info("Review deleted by admin");
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getShopReviews(Long shopId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByShopId(shopId, pageable);
        return buildReviewListResponse(reviewPage, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getShopProductReviews(Long shopId, Long productId, Pageable pageable) {
        // Implementation for shop product reviews
        // This would need additional repository method or custom query
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewStatisticsResponse getShopReviewStatisticsForSeller(Long shopId) {
        return buildShopReviewStatistics(shopId);
    }
    
    @Override
    public void markReviewAsHelpful(Long reviewId, Long userId) {
        log.info("Marking review {} as helpful by user {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
        
        log.info("Review marked as helpful");
    }
    
    @Override
    public void reportReview(Long reviewId, Long userId, String reportReason) {
        log.info("Reporting review {} by user {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        review.setIsReported(true);
        review.setReportReason(reportReason);
        reviewRepository.save(review);
        
        log.info("Review reported successfully");
    }
    
    // Private helper methods
    
    private void validateReviewCreation(User user, Product product, Order order) {
        // Check if order belongs to user
        if (!order.getMasterOrder().getUser().getId().equals(user.getId())) {
            throw new InvalidDataException("Order does not belong to the current user");
        }
        
        // Check if order contains the product
        boolean orderContainsProduct = order.getOrderItems().stream()
                .anyMatch(item -> item.getVariant().getProduct().getId().equals(product.getId()));
        
        if (!orderContainsProduct) {
            throw new InvalidDataException("Order does not contain the specified product");
        }
        
        // Check if order is completed (delivered)
        if (order.getOrderStatus().name().equals("DELIVERED")) {
            throw new InvalidDataException("Cannot review product from an incomplete order");
        }
    }
    
    private void updateProductRating(Product product) {
        Double averageRating = reviewRepository.getAverageRatingByProduct(product);
        Long reviewCount = reviewRepository.countByProduct(product);
        
        if (averageRating != null) {
            product.setRating(averageRating.floatValue());
        }
        if (reviewCount != null) {
            product.setReviewCount(reviewCount.intValue());
        }
        
        productRepository.save(product);
    }
    
    private ReviewResponse mapToReviewResponse(Review review) {
        // Manually map to avoid ModelMapper ambiguity on userName (firstName/lastName/fullName)
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName((review.getUser().getFirstName() != null ? review.getUser().getFirstName() : "")
                        + (review.getUser().getLastName() != null ? " " + review.getUser().getLastName() : ""))
                .userEmail(review.getUser().getEmail())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .orderId(review.getOrder().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .isReported(review.getIsReported())
                .reportReason(review.getReportReason())
                .isApproved(review.getIsApproved())
                .moderatorNotes(review.getModeratorNotes())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
    
    private ReviewListResponse buildReviewListResponse(Page<Review> reviewPage, Product product) {
        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
        
        Double averageRating = null;
        Long totalReviews = null;
        
        if (product != null) {
            averageRating = reviewRepository.getAverageRatingByProduct(product);
            totalReviews = reviewRepository.countByProduct(product);
        }
        
        return ReviewListResponse.builder()
                .reviews(reviews)
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .currentPage(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();
    }
    
    private ReviewStatisticsResponse buildProductReviewStatistics(Product product) {
        Double averageRating = reviewRepository.getAverageRatingByProduct(product);
        Long totalReviews = reviewRepository.countByProduct(product);
        
        // Count by rating
        Long fiveStarCount = reviewRepository.countByProductAndRatingAndIsApprovedTrue(product, 5);
        Long fourStarCount = reviewRepository.countByProductAndRatingAndIsApprovedTrue(product, 4);
        Long threeStarCount = reviewRepository.countByProductAndRatingAndIsApprovedTrue(product, 3);
        Long twoStarCount = reviewRepository.countByProductAndRatingAndIsApprovedTrue(product, 2);
        Long oneStarCount = reviewRepository.countByProductAndRatingAndIsApprovedTrue(product, 1);
        
        // Calculate percentages
        Double fiveStarPercentage = totalReviews > 0 ? (double) fiveStarCount / totalReviews * 100 : 0.0;
        Double fourStarPercentage = totalReviews > 0 ? (double) fourStarCount / totalReviews * 100 : 0.0;
        Double threeStarPercentage = totalReviews > 0 ? (double) threeStarCount / totalReviews * 100 : 0.0;
        Double twoStarPercentage = totalReviews > 0 ? (double) twoStarCount / totalReviews * 100 : 0.0;
        Double oneStarPercentage = totalReviews > 0 ? (double) oneStarCount / totalReviews * 100 : 0.0;
        
        return ReviewStatisticsResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .fiveStarCount(fiveStarCount)
                .fourStarCount(fourStarCount)
                .threeStarCount(threeStarCount)
                .twoStarCount(twoStarCount)
                .oneStarCount(oneStarCount)
                .fiveStarPercentage(fiveStarPercentage)
                .fourStarPercentage(fourStarPercentage)
                .threeStarPercentage(threeStarPercentage)
                .twoStarPercentage(twoStarPercentage)
                .oneStarPercentage(oneStarPercentage)
                .build();
    }
    
    private ReviewStatisticsResponse buildShopReviewStatistics(Long shopId) {
        Double averageRating = reviewRepository.getAverageRatingByShopId(shopId);
        Long totalReviews = reviewRepository.countByShopId(shopId);
        
        return ReviewStatisticsResponse.builder()
                .productId(null) // Not applicable for shop
                .productName(null) // Not applicable for shop
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();
    }
}
