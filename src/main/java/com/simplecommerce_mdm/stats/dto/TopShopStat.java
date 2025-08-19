package com.simplecommerce_mdm.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopShopStat {
    private Long shopId;
    private String shopName;
    private BigDecimal revenue;
}


