package com.simplecommerce_mdm.stats.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SellerStatsOverviewResponse {
    private Long shopId;
    private String shopName;

    private Long totalOrders;
    private Long pendingOrders;
    private Long processingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long completedOrders;
    private Long cancelledOrders;

    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
}


