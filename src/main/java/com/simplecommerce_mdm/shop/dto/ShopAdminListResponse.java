package com.simplecommerce_mdm.shop.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShopAdminListResponse {
    private List<ShopAdminResponse> shops;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;
} 