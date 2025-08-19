package com.simplecommerce_mdm.stats.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.stats.dto.SalesSeriesPoint;
import com.simplecommerce_mdm.stats.dto.SellerStatsOverviewResponse;
import com.simplecommerce_mdm.stats.dto.TopProductStat;
import com.simplecommerce_mdm.stats.dto.LowStockItem;
import com.simplecommerce_mdm.stats.dto.BreakdownEntry;
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

    @GetMapping("/payment-breakdown")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get payment breakdown for seller")
    public ResponseEntity<ApiResponse<List<BreakdownEntry>>> getPaymentBreakdown(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "7d") String range) {
        List<BreakdownEntry> data = statsService.getSellerPaymentBreakdown(sellerDetails, range);
        return ResponseEntity.ok(ApiResponse.<List<BreakdownEntry>>builder()
                .message("Seller payment breakdown retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/avg-order-value")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get average order value (AOV) for seller")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getAov(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "30d") String range) {
        java.math.BigDecimal data = statsService.getSellerAvgOrderValue(sellerDetails, range);
        return ResponseEntity.ok(ApiResponse.<java.math.BigDecimal>builder()
                .message("Seller AOV retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/returns")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get returns count for seller")
    public ResponseEntity<ApiResponse<Long>> getReturns(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "30d") String range) {
        Long data = statsService.getSellerReturnsCount(sellerDetails, range);
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .message("Seller returns count retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get top products for seller")
    public ResponseEntity<ApiResponse<List<TopProductStat>>> getTopProducts(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "quantity") String sortBy,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<TopProductStat> data = statsService.getSellerTopProducts(sellerDetails, sortBy, limit);
        return ResponseEntity.ok(ApiResponse.<List<TopProductStat>>builder()
                .message("Seller top products retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get low stock variants for seller")
    public ResponseEntity<ApiResponse<List<LowStockItem>>> getLowStock(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "5") Integer threshold) {
        List<LowStockItem> data = statsService.getSellerLowStock(sellerDetails, threshold);
        return ResponseEntity.ok(ApiResponse.<List<LowStockItem>>builder()
                .message("Seller low stock retrieved successfully")
                .data(data)
                .build());
    }
}


