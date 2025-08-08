package com.simplecommerce_mdm.order.service;

import com.simplecommerce_mdm.order.dto.*;
import com.simplecommerce_mdm.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /**
     * Create order from user's cart (Checkout process)
     */
    MasterOrderResponse createOrderFromCart(Long userId, CheckoutRequest request);

    /**
     * Get user's order history
     */
    Page<OrderListResponse> getUserOrders(Long userId, Pageable pageable);

    /**
     * Get user's orders by status
     */
    Page<OrderListResponse> getUserOrdersByStatus(Long userId, OrderStatus status, Pageable pageable);

    /**
     * Get order details by ID (with user validation)
     */
    OrderResponse getOrderDetails(Long userId, Long orderId);

    /**
     * Get master order details by ID (with user validation)
     */
    MasterOrderResponse getMasterOrderDetails(Long userId, Long masterOrderId);

    /**
     * Cancel order by user
     */
    void cancelOrder(Long userId, Long orderId, String reason);

    /**
     * Get orders for seller (by shop)
     */
    Page<OrderListResponse> getSellerOrders(Long shopId, Pageable pageable);

    /**
     * Get seller orders by status
     */
    Page<OrderListResponse> getSellerOrdersByStatus(Long shopId, OrderStatus status, Pageable pageable);

    /**
     * Update order status (by seller/admin)
     */
    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);

    /**
     * Get all orders for admin
     */
    Page<OrderListResponse> getAllOrdersForAdmin(Pageable pageable);

    /**
     * Search orders for admin
     */
    Page<OrderListResponse> searchOrdersForAdmin(String keyword, Pageable pageable);

    /**
     * Get order statistics
     */
    OrderStatisticsResponse getOrderStatistics();
} 