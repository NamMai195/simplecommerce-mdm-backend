package com.simplecommerce_mdm.stats.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.stats.dto.AdminStatsOverviewResponse;
import com.simplecommerce_mdm.stats.dto.SalesSeriesPoint;
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
@Tag(name = "Admin Stats", description = "Statistics for admin dashboard")
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin overview stats")
    public ResponseEntity<ApiResponse<AdminStatsOverviewResponse>> getOverview() {
        AdminStatsOverviewResponse data = statsService.getAdminOverview();
        return ResponseEntity.ok(ApiResponse.<AdminStatsOverviewResponse>builder()
                .message("Admin overview retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/sales-series")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin sales series")
    public ResponseEntity<ApiResponse<List<SalesSeriesPoint>>> getSalesSeries(
            @RequestParam(defaultValue = "7d") String range) {
        List<SalesSeriesPoint> data = statsService.getAdminSalesSeries(range);
        return ResponseEntity.ok(ApiResponse.<List<SalesSeriesPoint>>builder()
                .message("Admin sales series retrieved successfully")
                .data(data)
                .build());
    }
}


