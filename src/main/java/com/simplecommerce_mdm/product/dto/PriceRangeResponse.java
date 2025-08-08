package com.simplecommerce_mdm.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceRangeResponse {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
} 