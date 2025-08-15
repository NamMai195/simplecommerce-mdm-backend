package com.simplecommerce_mdm.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long productId;
    private String productName;
    private Long orderId;
    private Integer rating;
    private String comment;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private Boolean isReported;
    private String reportReason;
    private Boolean isApproved;
    private String moderatorNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
