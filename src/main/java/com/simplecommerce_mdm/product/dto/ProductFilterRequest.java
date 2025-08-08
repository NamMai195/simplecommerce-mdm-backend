package com.simplecommerce_mdm.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilterRequest {
    private String searchTerm;
    private Integer categoryId;
    private Long shopId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
} 