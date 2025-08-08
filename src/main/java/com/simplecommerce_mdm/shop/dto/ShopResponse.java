package com.simplecommerce_mdm.shop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ShopResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String coverImageUrl;
    private String contactEmail;
    private String contactPhone;
    private BigDecimal rating;
    private Boolean isActive;
    private OffsetDateTime approvedAt;
    private OffsetDateTime createdAt;
    private String rejectionReason; // If shop was rejected
    
    // Address info
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    
    // Basic statistics
    private Integer totalProducts;
} 