package com.simplecommerce_mdm.stats.service;

import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.stats.dto.*;

import java.util.List;

public interface StatsService {
    SellerStatsOverviewResponse getSellerOverview(CustomUserDetails sellerDetails);
    List<SalesSeriesPoint> getSellerSalesSeries(CustomUserDetails sellerDetails, String range);

    AdminStatsOverviewResponse getAdminOverview();
    List<SalesSeriesPoint> getAdminSalesSeries(String range);
}


