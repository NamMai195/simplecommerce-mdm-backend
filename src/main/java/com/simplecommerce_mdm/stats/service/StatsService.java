package com.simplecommerce_mdm.stats.service;

import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.stats.dto.*;

import java.util.List;

public interface StatsService {
    SellerStatsOverviewResponse getSellerOverview(CustomUserDetails sellerDetails);
    List<SalesSeriesPoint> getSellerSalesSeries(CustomUserDetails sellerDetails, String range);
    List<TopProductStat> getSellerTopProducts(CustomUserDetails sellerDetails, String sortBy, Integer limit);
    List<LowStockItem> getSellerLowStock(CustomUserDetails sellerDetails, Integer threshold);
    List<BreakdownEntry> getSellerPaymentBreakdown(CustomUserDetails sellerDetails, String range);
    java.math.BigDecimal getSellerAvgOrderValue(CustomUserDetails sellerDetails, String range);
    Long getSellerReturnsCount(CustomUserDetails sellerDetails, String range);

    AdminStatsOverviewResponse getAdminOverview();
    List<SalesSeriesPoint> getAdminSalesSeries(String range);
    List<BreakdownEntry> getAdminPaymentBreakdown(String range);
    List<TopShopStat> getAdminTopShops(Integer limit);
}


