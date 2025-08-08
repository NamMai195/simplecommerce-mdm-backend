package com.simplecommerce_mdm.product.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductBuyerListResponse {
    private List<ProductBuyerResponse> products;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;
} 