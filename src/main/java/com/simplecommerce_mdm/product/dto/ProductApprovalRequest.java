package com.simplecommerce_mdm.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductApprovalRequest {
    
    @NotBlank(message = "Rejection reason is required when rejecting a product")
    private String rejectionReason;
} 