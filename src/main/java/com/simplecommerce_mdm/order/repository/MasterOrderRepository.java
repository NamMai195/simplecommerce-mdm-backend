package com.simplecommerce_mdm.order.repository;

import com.simplecommerce_mdm.order.model.MasterOrder;
import com.simplecommerce_mdm.common.enums.MasterOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterOrderRepository extends JpaRepository<MasterOrder, Long> {

    /**
     * Find master order by order group number
     */
    Optional<MasterOrder> findByOrderGroupNumber(String orderGroupNumber);

    /**
     * Find all master orders for a specific user
     */
    Page<MasterOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find master orders by user and status
     */
    Page<MasterOrder> findByUserIdAndOverallStatusOrderByCreatedAtDesc(
            Long userId, MasterOrderStatus status, Pageable pageable);

    /**
     * Find master orders by status
     */
    Page<MasterOrder> findByOverallStatusOrderByCreatedAtDesc(MasterOrderStatus status, Pageable pageable);

    /**
     * Check if order group number exists
     */
    boolean existsByOrderGroupNumber(String orderGroupNumber);

    /**
     * Count orders by user and status
     */
    long countByUserIdAndOverallStatus(Long userId, MasterOrderStatus status);

    /**
     * Find recent orders for admin dashboard
     */
    @Query("SELECT mo FROM MasterOrder mo ORDER BY mo.createdAt DESC")
    List<MasterOrder> findRecentOrders(Pageable pageable);

    /**
     * Search master orders by customer info
     */
    @Query("SELECT mo FROM MasterOrder mo WHERE " +
           "LOWER(mo.customerEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(mo.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(mo.orderGroupNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<MasterOrder> searchByCustomerInfo(@Param("keyword") String keyword, Pageable pageable);
} 