package com.simplecommerce_mdm.cart.repository;

import com.simplecommerce_mdm.cart.model.Cart;
import com.simplecommerce_mdm.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    
    /**
     * Tìm giỏ hàng của user
     */
    Optional<Cart> findByUser(User user);
    
    /**
     * Tìm giỏ hàng của user theo userId
     */
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);
    
    /**
     * Tìm các giỏ hàng đã hết hạn để cleanup
     */
    @Query("SELECT c FROM Cart c WHERE c.expiresAt IS NOT NULL AND c.expiresAt < :currentTime")
    List<Cart> findExpiredCarts(@Param("currentTime") OffsetDateTime currentTime);
    
    /**
     * Đếm số lượng item trong giỏ hàng của user
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.user.id = :userId")
    Long countItemsByUserId(@Param("userId") Long userId);
    
    /**
     * Kiểm tra user đã có giỏ hàng chưa
     */
    boolean existsByUser(User user);
} 