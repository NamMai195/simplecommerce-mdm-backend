package com.simplecommerce_mdm.product.dto;

import com.simplecommerce_mdm.common.enums.ProductStatus;
import lombok.Data;

@Data
public class ProductAdminSearchRequest {
    
    private String searchTerm; // Search by product name or SKU
    private ProductStatus status; // Filter by product status
    private Long shopId; // Filter by shop
    private Integer categoryId; // Filter by category
    private String sellerEmail; // Filter by seller email
    private Integer page = 0; // Page number (default 0)
    private Integer size = 20; // Page size (default 20 for admin)
    private String sortBy = "createdAt"; // Sort field (default by createdAt)
    private String sortDirection = "desc"; // Sort direction (default desc)
} 