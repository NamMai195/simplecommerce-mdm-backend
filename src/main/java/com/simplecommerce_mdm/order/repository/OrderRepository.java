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
     * Search orders for admin
     */
    @Query("SELECT o FROM Order o WHERE " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.masterOrder.customerEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.shop.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);
} 