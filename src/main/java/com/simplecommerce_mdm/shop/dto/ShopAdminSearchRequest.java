package com.simplecommerce_mdm.shop.dto;

import lombok.Data;

@Data
public class ShopAdminSearchRequest {
    private String searchTerm;
    private String status; // "pending", "approved", "rejected"
    private String sellerEmail;
    private String city;
    private String country;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
} 