package com.simplecommerce_mdm.order.controller;

import com.simplecommerce_mdm.order.service.OrderService;
import com.simplecommerce_mdm.order.dto.*;
import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.common.enums.OrderStatus;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.product.repository.ShopRepository;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/seller/orders")
@RequiredArgsConstructor
@Tag(name = "Seller Order Management", description = "APIs for seller order operations")
@PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
public class OrderSellerController {

    private final OrderService orderService;
    private final ShopRepository shopRepository;

    @GetMapping
    @Operation(summary = "Get seller's orders", description = "Retrieve paginated list of orders for seller's shop")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getSellerOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by order status")
            @RequestParam(required = false) OrderStatus status) {
        
        // Get seller's shop
        Long shopId = getSellerShopId(userDetails.getUser().getId());
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderListResponse> orders;
        
        if (status != null) {
            orders = orderService.getSellerOrdersByStatus(shopId, status, pageable);
        } else {
            orders = orderService.getSellerOrders(shopId, pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.<Page<OrderListResponse>>builder()
                .statusCode(200)
                .message("Seller orders retrieved successfully")
                .data(orders)
                .build());
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update order status (seller operations)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Order ID")
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        
        OrderResponse order = orderService.updateOrderStatus(orderId, request);
        
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .statusCode(200)
                .message("Order status updated successfully")
                .data(order)
                .build());
    }

    private Long getSellerShopId(Long userId) {
        return shopRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Seller shop not found"))
                .getId();
    }
} 