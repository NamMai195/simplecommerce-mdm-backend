package com.simplecommerce_mdm.order.controller;

import com.simplecommerce_mdm.order.service.OrderService;
import com.simplecommerce_mdm.order.dto.*;
import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.common.enums.OrderStatus;
import com.simplecommerce_mdm.config.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for order operations")
@PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order from cart (Checkout)", description = "Place order from user's cart items")
    public ResponseEntity<ApiResponse<MasterOrderResponse>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request) {
        
        log.info("Creating order for user: {}", userDetails.getUser().getId());
        
        MasterOrderResponse response = orderService.createOrderFromCart(userDetails.getUser().getId(), request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<MasterOrderResponse>builder()
                        .statusCode(201)
                        .message("Order created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    @Operation(summary = "Get user's order history", description = "Retrieve paginated list of user's orders")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getUserOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by order status")
            @RequestParam(required = false) OrderStatus status) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderListResponse> orders;
        
        if (status != null) {
            orders = orderService.getUserOrdersByStatus(userDetails.getUser().getId(), status, pageable);
        } else {
            orders = orderService.getUserOrders(userDetails.getUser().getId(), pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.<Page<OrderListResponse>>builder()
                .statusCode(200)
                .message("Orders retrieved successfully")
                .data(orders)
                .build());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details", description = "Retrieve detailed information about a specific order")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Order ID")
            @PathVariable Long orderId) {
        
        OrderResponse order = orderService.getOrderDetails(userDetails.getUser().getId(), orderId);
        
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .statusCode(200)
                .message("Order details retrieved successfully")
                .data(order)
                .build());
    }

    @GetMapping("/master/{masterOrderId}")
    @Operation(summary = "Get master order details", description = "Retrieve detailed information about a master order group")
    public ResponseEntity<ApiResponse<MasterOrderResponse>> getMasterOrderDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Master Order ID")
            @PathVariable Long masterOrderId) {
        
        MasterOrderResponse masterOrder = orderService.getMasterOrderDetails(userDetails.getUser().getId(), masterOrderId);
        
        return ResponseEntity.ok(ApiResponse.<MasterOrderResponse>builder()
                .statusCode(200)
                .message("Master order details retrieved successfully")
                .data(masterOrder)
                .build());
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order (only if not yet processed)")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Order ID")
            @PathVariable Long orderId,
            @Parameter(description = "Cancellation reason")
            @RequestParam(required = false, defaultValue = "Cancelled by user") String reason) {
        
        orderService.cancelOrder(userDetails.getUser().getId(), orderId, reason);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Order cancelled successfully")
                .build());
    }
} 