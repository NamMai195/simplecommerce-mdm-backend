package com.simplecommerce_mdm.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LowStockItem {
    private Long variantId;
    private String sku;
    private String productName;
    private Integer stockQuantity;
}


