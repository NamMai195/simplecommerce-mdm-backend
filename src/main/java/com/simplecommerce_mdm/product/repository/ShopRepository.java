package com.simplecommerce_mdm.product.repository;

import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    
    // Basic methods
    Optional<Shop> findByUser(User user);
    
    Optional<Shop> findByUserId(Long userId);
    
    // Admin search methods
    Page<Shop> findByIsActive(Boolean isActive, Pageable pageable);
    
    // Use Spring Data JPA method instead of custom query to avoid bytea issues
    Page<Shop> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Complex admin search with native SQL to avoid type mapping issues
    @Query(value = "SELECT s.* FROM shops s " +
           "WHERE s.deleted_at IS NULL " +
           "AND (:searchTerm IS NULL OR s.name ILIKE '%' || :searchTerm || '%') " +
           "AND (:isActive IS NULL OR s.is_active = :isActive)",
           nativeQuery = true)
    List<Shop> findShopsForAdminNative(@Param("searchTerm") String searchTerm,
                                       @Param("isActive") Boolean isActive);
    
    // Pending shops (for approval)
    @Query("SELECT s FROM Shop s " +
           "WHERE s.isActive = false AND s.approvedAt IS NULL")
    Page<Shop> findPendingShops(Pageable pageable);
    
    // Approved shops
    @Query("SELECT s FROM Shop s " +
           "WHERE s.isActive = true AND s.approvedAt IS NOT NULL")
    Page<Shop> findApprovedShops(Pageable pageable);
    
    // Rejected shops
    @Query("SELECT s FROM Shop s " +
           "WHERE s.isActive = false AND s.rejectionReason IS NOT NULL")
    Page<Shop> findRejectedShops(Pageable pageable);
    
    // Count products for shop
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId")
    Integer countProductsByShopId(@Param("shopId") Long shopId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId AND p.status = 'APPROVED'")
    Integer countApprovedProductsByShopId(@Param("shopId") Long shopId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId AND p.status = 'PENDING_APPROVAL'")
    Integer countPendingProductsByShopId(@Param("shopId") Long shopId);

    // Admin: top shops by completed order revenue
    @Query("SELECT o.shop.id, o.shop.name, COALESCE(SUM( (o.subtotalAmount + o.shippingFee + o.taxAmount) - (o.itemDiscountAmount + o.shippingDiscountAmount) ),0) as revenue " +
           "FROM Order o WHERE o.orderStatus = 'COMPLETED' GROUP BY o.shop.id, o.shop.name ORDER BY revenue DESC")
    List<Object[]> findTopShopsByRevenue();
} 