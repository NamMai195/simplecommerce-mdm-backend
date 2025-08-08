package com.simplecommerce_mdm.shop.dto;

import lombok.Data;

@Data
public class ShopApprovalRequest {
    private String rejectionReason; // Required for rejection, optional for approval
} 