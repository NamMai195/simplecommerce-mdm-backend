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
@Tag(name = "Seller Stats", description = "API thống kê dành cho người bán hàng")
public class SellerStatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Lấy tổng quan thống kê seller", description = "Lấy các chỉ số tổng quan của shop bao gồm doanh thu, đơn hàng, sản phẩm")
    public ResponseEntity<ApiResponse<SellerStatsOverviewResponse>> getOverview(
            @AuthenticationPrincipal CustomUserDetails sellerDetails) {
        SellerStatsOverviewResponse data = statsService.getSellerOverview(sellerDetails);
        return ResponseEntity.ok(ApiResponse.<SellerStatsOverviewResponse>builder()
                .message("Lấy tổng quan thống kê seller thành công")
                .data(data)
                .build());
    }

    @GetMapping("/sales-series")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Lấy biểu đồ doanh thu seller", description = "Lấy dữ liệu biểu đồ doanh thu theo thời gian cho seller")
    public ResponseEntity<ApiResponse<List<SalesSeriesPoint>>> getSalesSeries(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "7d") String range) {
        List<SalesSeriesPoint> data = statsService.getSellerSalesSeries(sellerDetails, range);
        return ResponseEntity.ok(ApiResponse.<List<SalesSeriesPoint>>builder()
                .message("Lấy biểu đồ doanh thu seller thành công")
                .data(data)
                .build());
    }

    @GetMapping("/payment-breakdown")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Lấy phân tích phương thức thanh toán", description = "Lấy thống kê phân tích theo các phương thức thanh toán của seller")
    public ResponseEntity<ApiResponse<List<BreakdownEntry>>> getPaymentBreakdown(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "7d") String range) {
        List<BreakdownEntry> data = statsService.getSellerPaymentBreakdown(sellerDetails, range);
        return ResponseEntity.ok(ApiResponse.<List<BreakdownEntry>>builder()
                .message("Lấy phân tích phương thức thanh toán thành công")
                .data(data)
                .build());
    }

    @GetMapping("/avg-order-value")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Lấy giá trị đơn hàng trung bình (AOV)", description = "Lấy giá trị đơn hàng trung bình của seller trong khoảng thời gian")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getAov(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "30d") String range) {
        java.math.BigDecimal data = statsService.getSellerAvgOrderValue(sellerDetails, range);
        return ResponseEntity.ok(ApiResponse.<java.math.BigDecimal>builder()
                .message("Lấy giá trị đơn hàng trung bình thành công")
                .data(data)
                .build());
    }

    @GetMapping("/returns")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Lấy số lượng đơn trả hàng", description = "Lấy số lượng đơn hàng bị trả lại của seller")
    public ResponseEntity<ApiResponse<Long>> getReturns(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "30d") String range) {
        Long data = statsService.getSellerReturnsCount(sellerDetails, range);
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .message("Lấy số lượng đơn trả hàng thành công")
                .data(data)
                .build());
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Lấy danh sách sản phẩm bán chạy", description = "Lấy danh sách các sản phẩm bán chạy nhất của seller")
    public ResponseEntity<ApiResponse<List<TopProductStat>>> getTopProducts(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "quantity") String sortBy,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<TopProductStat> data = statsService.getSellerTopProducts(sellerDetails, sortBy, limit);
        return ResponseEntity.ok(ApiResponse.<List<TopProductStat>>builder()
                .message("Lấy danh sách sản phẩm bán chạy thành công")
                .data(data)
                .build());
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Lấy danh sách sản phẩm sắp hết hàng", description = "Lấy danh sách các biến thể sản phẩm có số lượng tồn kho thấp")
    public ResponseEntity<ApiResponse<List<LowStockItem>>> getLowStock(
            @AuthenticationPrincipal CustomUserDetails sellerDetails,
            @RequestParam(defaultValue = "5") Integer threshold) {
        List<LowStockItem> data = statsService.getSellerLowStock(sellerDetails, threshold);
        return ResponseEntity.ok(ApiResponse.<List<LowStockItem>>builder()
                .message("Lấy danh sách sản phẩm sắp hết hàng thành công")
                .data(data)
                .build());
    }
}


