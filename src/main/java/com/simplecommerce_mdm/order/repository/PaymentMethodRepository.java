package com.simplecommerce_mdm.order.repository;

import com.simplecommerce_mdm.order.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

    /**
     * Find payment method by code
     */
    Optional<PaymentMethod> findByCode(String code);

    /**
     * Find active payment methods ordered by sort order
     */
    List<PaymentMethod> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * Find all payment methods ordered by sort order
     */
    List<PaymentMethod> findAllByOrderBySortOrderAsc();

    /**
     * Check if code exists
     */
    boolean existsByCode(String code);

    /**
     * Check if name exists
     */
    boolean existsByName(String name);

    /**
     * Find COD payment method specifically
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.code = 'COD' AND pm.isActive = true")
    Optional<PaymentMethod> findCODPaymentMethod();
} 