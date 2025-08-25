package com.simplecommerce_mdm.cart.repository;

import com.simplecommerce_mdm.cart.model.Cart;
import com.simplecommerce_mdm.cart.model.CartItem;
import com.simplecommerce_mdm.product.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * Tìm item trong giỏ hàng theo cart và variant
     */
    Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);
    
    /**
     * Tìm tất cả items trong giỏ hàng của user
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = :userId ORDER BY ci.addedAt DESC")
    List<CartItem> findByCartUserId(@Param("userId") Long userId);
    
    /**
     * Tìm specific items trong giỏ hàng của user theo IDs
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = :userId AND ci.id IN :cartItemIds ORDER BY ci.addedAt DESC")
    List<CartItem> findByCartUserIdAndIdIn(@Param("userId") Long userId, @Param("cartItemIds") List<Long> cartItemIds);
    
    /**
     * Tìm item theo ID và userId (để bảo mật)
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.id = :itemId AND ci.cart.user.id = :userId")
    Optional<CartItem> findByIdAndUserId(@Param("itemId") Long itemId, @Param("userId") Long userId);
    
    /**
     * Xóa tất cả items trong giỏ hàng của user
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.user.id = :userId")
    void deleteByCartUserId(@Param("userId") Long userId);
    
    /**
     * Xóa specific items trong giỏ hàng của user theo IDs
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.user.id = :userId AND ci.id IN :cartItemIds")
    void deleteByCartUserIdAndIdIn(@Param("userId") Long userId, @Param("cartItemIds") List<Long> cartItemIds);
    
    /**
     * Xóa tất cả items của một cart
     */
    void deleteByCart(Cart cart);
    
    /**
     * Đếm số lượng items trong cart
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long countByCartId(@Param("cartId") UUID cartId);
    
    /**
     * Tính tổng số lượng sản phẩm trong cart
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer sumQuantityByCartId(@Param("cartId") UUID cartId);
} 