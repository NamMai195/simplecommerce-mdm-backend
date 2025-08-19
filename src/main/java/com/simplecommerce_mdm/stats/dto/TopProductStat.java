package com.simplecommerce_mdm.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopProductStat {
    private Long productId;
    private String productName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}


