package com.simplecommerce_mdm.order.repository;

import com.simplecommerce_mdm.order.model.Order;
import com.simplecommerce_mdm.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by master order
     */
    List<Order> findByMasterOrderIdOrderByCreatedAtDesc(Long masterOrderId);

    /**
     * Find orders by shop for seller
     */
    Page<Order> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    /**
     * Find orders by shop and status
     */
    Page<Order> findByShopIdAndOrderStatusOrderByCreatedAtDesc(
            Long shopId, OrderStatus status, Pageable pageable);

    /**
     * Find user's orders through master order
     */
    @Query("SELECT o FROM Order o WHERE o.masterOrder.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find user's orders by status
     */
    @Query("SELECT o FROM Order o WHERE o.masterOrder.user.id = :userId AND o.orderStatus = :status ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(
            @Param("userId") Long userId, @Param("status") OrderStatus status, Pageable pageable);

    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Count orders by shop and status
     */
    long countByShopIdAndOrderStatus(Long shopId, OrderStatus status);

    /**
     * Count orders by status
     */
    long countByOrderStatus(OrderStatus status);

    /**
     * Find orders that need seller confirmation
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'AWAITING_CONFIRMATION' ORDER BY o.createdAt ASC")
    List<Order> findOrdersAwaitingConfirmation();

    /**
     * Find orders for a shop in a date range filtered by statuses
     */
    List<Order> findByShopIdAndCreatedAtBetweenAndOrderStatusIn(
            Long shopId,
            LocalDateTime start,
            LocalDateTime end,
            Collection<OrderStatus> statuses);

    /**
     * Find orders in a date range filtered by statuses (all shops)
     */
    List<Order> findByCreatedAtBetweenAndOrderStatusIn(
            LocalDateTime start,
            LocalDateTime end,
            Collection<OrderStatus> statuses);

    /**
     * Find orders filtered by statuses
     */
    List<Order> findByOrderStatusIn(Collection<OrderStatus> statuses);

    // Average order value for a shop within date range (completed orders)
    @Query("SELECT COALESCE(AVG( (o.subtotalAmount + o.shippingFee + o.taxAmount) - (o.itemDiscountAmount + o.shippingDiscountAmount) ),0) " +
           "FROM Order o WHERE o.shop.id = :shopId AND o.orderStatus = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end")
    java.math.BigDecimal avgOrderValueForShop(@Param("shopId") Long shopId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    // Returns count by shop within date range
    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND o.orderStatus IN ('RETURN_REQUESTED','RETURN_APPROVED','RETURNED') AND o.createdAt BETWEEN :start AND :end")
    long countReturnsForShop(@Param("shopId") Long shopId,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);

    /**
     * Search orders for admin
     */
    @Query("SELECT o FROM Order o WHERE " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.masterOrder.customerEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.shop.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);
} 