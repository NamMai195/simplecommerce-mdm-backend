package com.simplecommerce_mdm.order.repository;

import com.simplecommerce_mdm.order.model.OrderItem;
import com.simplecommerce_mdm.common.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find order items by order
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Find order items by order and status
     */
    List<OrderItem> findByOrderIdAndStatus(Long orderId, OrderItemStatus status);

    /**
     * Find order items by variant
     */
    List<OrderItem> findByVariantId(Long variantId);

    /**
     * Count items by order
     */
    long countByOrderId(Long orderId);

    /**
     * Sum quantity by order
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Integer sumQuantityByOrderId(@Param("orderId") Long orderId);

    /**
     * Calculate total subtotal by order
     */
    @Query("SELECT COALESCE(SUM(oi.subtotal), 0) FROM OrderItem oi WHERE oi.order.id = :orderId")
    java.math.BigDecimal sumSubtotalByOrderId(@Param("orderId") Long orderId);

    /**
     * Find items that need fulfillment
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.status IN ('PENDING', 'PROCESSING') ORDER BY oi.createdAt ASC")
    List<OrderItem> findItemsAwaitingFulfillment();

    /**
     * Find order items by user through master order
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.masterOrder.user.id = :userId ORDER BY oi.createdAt DESC")
    List<OrderItem> findByUserId(@Param("userId") Long userId);
} 