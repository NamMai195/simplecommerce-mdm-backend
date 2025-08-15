package com.simplecommerce_mdm.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatisticsResponse {
    
    private Long productId;
    private String productName;
    private Double averageRating;
    private Long totalReviews;
    private Long fiveStarCount;
    private Long fourStarCount;
    private Long threeStarCount;
    private Long twoStarCount;
    private Long oneStarCount;
    private Double fiveStarPercentage;
    private Double fourStarPercentage;
    private Double threeStarPercentage;
    private Double twoStarPercentage;
    private Double oneStarPercentage;
}
