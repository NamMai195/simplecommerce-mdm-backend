package com.simplecommerce_mdm.order.repository;

import com.simplecommerce_mdm.order.model.Payment;
import com.simplecommerce_mdm.common.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Find payments by master order
     */
    List<Payment> findByMasterOrderIdOrderByCreatedAtDesc(Long masterOrderId);

    /**
     * Find payments by status
     */
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    /**
     * Find payments by payment method
     */
    List<Payment> findByPaymentMethodId(Integer paymentMethodId);

    /**
     * Check if transaction ID exists
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * Find pending payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' ORDER BY p.createdAt ASC")
    List<Payment> findPendingPayments();

    /**
     * Find successful payments for a user
     */
    @Query("SELECT p FROM Payment p WHERE p.masterOrder.user.id = :userId AND p.status = 'COMPLETED' ORDER BY p.createdAt DESC")
    List<Payment> findSuccessfulPaymentsByUserId(@Param("userId") Long userId);

    /**
     * Calculate total payment amount by master order
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.masterOrder.id = :masterOrderId AND p.status = 'COMPLETED'")
    java.math.BigDecimal sumCompletedPaymentsByMasterOrder(@Param("masterOrderId") Long masterOrderId);

    /**
     * Count payments by status
     */
    long countByStatus(PaymentStatus status);

    // Payment breakdown: count and sum by method within date range
    @Query("SELECT pm.code, COUNT(p.id), COALESCE(SUM(p.amount),0) FROM Payment p JOIN p.paymentMethod pm " +
           "WHERE p.status = 'COMPLETED' AND p.createdAt BETWEEN :start AND :end GROUP BY pm.code")
    List<Object[]> breakdownByMethodAll(LocalDateTime start, LocalDateTime end);
} 