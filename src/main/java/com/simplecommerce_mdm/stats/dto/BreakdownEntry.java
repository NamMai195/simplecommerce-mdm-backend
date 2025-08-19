package com.simplecommerce_mdm.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BreakdownEntry {
    private String label;
    private Long count;
    private BigDecimal amount;
}


