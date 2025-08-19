package com.simplecommerce_mdm.stats.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.stats.dto.SalesSeriesPoint;
import com.simplecommerce_mdm.stats.dto.SellerStatsOverviewResponse;
import com.simplecommerce_mdm.stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/seller/stats")
@RequiredArgsConstructor
@Tag(name = "Seller Stats", description = "Statistics for seller dashboard")
public class SellerStatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get seller overview stats")
    public ResponseEntity<ApiResponse<SellerStatsOverviewResponse>> getOverview(
            @AuthenticationPrincipal CustomUserDetails sellerDetails) {
        SellerStatsOverviewResponse data = statsService.getSellerOverview(sellerDetails);
        return ResponseEntity.ok(ApiResponse.<SellerStatsOverviewResponse>builder()
                .message("Seller overview retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/sales-series")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get seller sales series")
    public ResponseEntity<ApiResponse<List<SalesSeriesPoint>>> getSalesSeries(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "7d") String range) {
        List<SalesSeriesPoint> data = statsService.getSellerSalesSeries(sellerDetails, range);
        return ResponseEntity.ok(ApiResponse.<List<SalesSeriesPoint>>builder()
                .message("Seller sales series retrieved successfully")
                .data(data)
                .build());
    }
}


