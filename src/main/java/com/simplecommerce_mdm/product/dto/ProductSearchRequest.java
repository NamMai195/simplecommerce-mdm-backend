package com.simplecommerce_mdm.product.dto;

import com.simplecommerce_mdm.common.enums.ProductStatus;
import lombok.Data;

@Data
public class ProductSearchRequest {
    
    private String searchTerm; // Search by product name
    private ProductStatus status; // Filter by product status
    private Integer page = 0; // Page number (default 0)
    private Integer size = 10; // Page size (default 10)
    private String sortBy = "createdAt"; // Sort field (default by createdAt)
    private String sortDirection = "desc"; // Sort direction (default desc)
} 