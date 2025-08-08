package com.simplecommerce_mdm.product.repository;

import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    
    // Basic methods
    Optional<Shop> findByUser(User user);
    
    // Admin search methods
    Page<Shop> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<Shop> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Complex admin search with joins
    @Query("SELECT s FROM Shop s " +
           "LEFT JOIN FETCH s.user u " +
           "LEFT JOIN FETCH s.address a " +
           "WHERE (:searchTerm IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "       OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:isActive IS NULL OR s.isActive = :isActive) " +
           "AND (:sellerEmail IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :sellerEmail, '%'))) " +
           "AND (:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:country IS NULL OR LOWER(a.countryCode) LIKE LOWER(CONCAT('%', :country, '%')))")
    Page<Shop> findShopsForAdmin(@Param("searchTerm") String searchTerm,
                                 @Param("isActive") Boolean isActive,
                                 @Param("sellerEmail") String sellerEmail,
                                 @Param("city") String city,
                                 @Param("country") String country,
                                 Pageable pageable);
    
    // Pending shops (for approval)
    @Query("SELECT s FROM Shop s " +
           "LEFT JOIN FETCH s.user u " +
           "LEFT JOIN FETCH s.address a " +
           "WHERE s.isActive = false AND s.approvedAt IS NULL")
    Page<Shop> findPendingShops(Pageable pageable);
    
    // Approved shops
    @Query("SELECT s FROM Shop s " +
           "LEFT JOIN FETCH s.user u " +
           "LEFT JOIN FETCH s.address a " +
           "WHERE s.isActive = true AND s.approvedAt IS NOT NULL")
    Page<Shop> findApprovedShops(Pageable pageable);
    
    // Rejected shops
    @Query("SELECT s FROM Shop s " +
           "LEFT JOIN FETCH s.user u " +
           "LEFT JOIN FETCH s.address a " +
           "WHERE s.isActive = false AND s.rejectionReason IS NOT NULL")
    Page<Shop> findRejectedShops(Pageable pageable);
    
    // Count products for shop
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId")
    Integer countProductsByShopId(@Param("shopId") Long shopId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId AND p.status = 'APPROVED'")
    Integer countApprovedProductsByShopId(@Param("shopId") Long shopId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId AND p.status = 'PENDING_APPROVAL'")
    Integer countPendingProductsByShopId(@Param("shopId") Long shopId);
} 