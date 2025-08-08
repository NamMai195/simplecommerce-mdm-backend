package com.simplecommerce_mdm.order.controller;

import com.simplecommerce_mdm.order.service.OrderService;
import com.simplecommerce_mdm.order.dto.*;
import com.simplecommerce_mdm.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin Order Management", description = "APIs for admin order operations")
@PreAuthorize("hasRole('ADMIN')")
public class OrderAdminController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve paginated list of all orders for admin")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getAllOrders(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderListResponse> orders = orderService.getAllOrdersForAdmin(pageable);
        
        return ResponseEntity.ok(ApiResponse.<Page<OrderListResponse>>builder()
                .statusCode(200)
                .message("All orders retrieved successfully")
                .data(orders)
                .build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search orders", description = "Search orders by keyword (order number, customer email, shop name)")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> searchOrders(
            @Parameter(description = "Search keyword")
            @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderListResponse> orders = orderService.searchOrdersForAdmin(keyword, pageable);
        
        return ResponseEntity.ok(ApiResponse.<Page<OrderListResponse>>builder()
                .statusCode(200)
                .message("Orders searched successfully")
                .data(orders)
                .build());
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status (Admin)", description = "Admin override of order status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @Parameter(description = "Order ID")
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        
        OrderResponse order = orderService.updateOrderStatus(orderId, request);
        
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .statusCode(200)
                .message("Order status updated by admin")
                .data(order)
                .build());
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get order statistics", description = "Retrieve order statistics for admin dashboard")
    public ResponseEntity<ApiResponse<OrderStatisticsResponse>> getOrderStatistics() {
        
        OrderStatisticsResponse statistics = orderService.getOrderStatistics();
        
        return ResponseEntity.ok(ApiResponse.<OrderStatisticsResponse>builder()
                .statusCode(200)
                .message("Order statistics retrieved successfully")
                .data(statistics)
                .build());
    }
} 