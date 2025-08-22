package com.simplecommerce_mdm.stats.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.stats.dto.AdminStatsOverviewResponse;
import com.simplecommerce_mdm.stats.dto.SalesSeriesPoint;
import com.simplecommerce_mdm.stats.dto.BreakdownEntry;
import com.simplecommerce_mdm.stats.dto.TopShopStat;
import com.simplecommerce_mdm.stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin Stats", description = "API thống kê dành cho trang quản trị admin")
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy tổng quan thống kê admin", description = "Lấy các chỉ số tổng quan của toàn hệ thống bao gồm doanh thu, đơn hàng, người dùng")
    public ResponseEntity<ApiResponse<AdminStatsOverviewResponse>> getOverview() {
        AdminStatsOverviewResponse data = statsService.getAdminOverview();
        return ResponseEntity.ok(ApiResponse.<AdminStatsOverviewResponse>builder()
                .message("Lấy tổng quan thống kê admin thành công")
                .data(data)
                .build());
    }

    @GetMapping("/sales-series")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy biểu đồ doanh thu admin", description = "Lấy dữ liệu biểu đồ doanh thu theo thời gian cho admin")
    public ResponseEntity<ApiResponse<List<SalesSeriesPoint>>> getSalesSeries(
            @RequestParam(defaultValue = "7d") String range) {
        List<SalesSeriesPoint> data = statsService.getAdminSalesSeries(range);
        return ResponseEntity.ok(ApiResponse.<List<SalesSeriesPoint>>builder()
                .message("Lấy biểu đồ doanh thu admin thành công")
                .data(data)
                .build());
    }

    @GetMapping("/payment-breakdown")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy phân tích phương thức thanh toán", description = "Lấy thống kê phân tích theo các phương thức thanh toán")
    public ResponseEntity<ApiResponse<List<BreakdownEntry>>> getPaymentBreakdown(
            @RequestParam(defaultValue = "30d") String range) {
        List<BreakdownEntry> data = statsService.getAdminPaymentBreakdown(range);
        return ResponseEntity.ok(ApiResponse.<List<BreakdownEntry>>builder()
                .message("Lấy phân tích phương thức thanh toán thành công")
                .data(data)
                .build());
    }

    @GetMapping("/top-shops")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách shop hàng đầu", description = "Lấy danh sách các shop có doanh thu cao nhất")
    public ResponseEntity<ApiResponse<List<TopShopStat>>> getTopShops(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<TopShopStat> data = statsService.getAdminTopShops(limit);
        return ResponseEntity.ok(ApiResponse.<List<TopShopStat>>builder()
                .message("Lấy danh sách shop hàng đầu thành công")
                .data(data)
                .build());
    }
}


