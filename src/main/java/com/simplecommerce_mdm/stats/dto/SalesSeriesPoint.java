package com.simplecommerce_mdm.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SalesSeriesPoint {
    private LocalDate date;
    private Long orders;
    private BigDecimal revenue;
}


