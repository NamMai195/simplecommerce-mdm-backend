package com.simplecommerce_mdm.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsResponse {

    // Order counts by status
    private Long totalOrders;
    private Long pendingOrders;
    private Long awaitingConfirmationOrders;
    private Long processingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    
    // Revenue statistics
    private BigDecimal totalRevenue;
    private BigDecimal pendingRevenue;
    private BigDecimal completedRevenue;
    
    // Payment statistics
    private Long codOrders;
    private Long paidOrders;
    
    // Today's statistics
    private Long todayOrders;
    private BigDecimal todayRevenue;
} 