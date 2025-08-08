package com.simplecommerce_mdm.product.repository;

import com.simplecommerce_mdm.common.enums.ProductStatus;
import com.simplecommerce_mdm.product.model.Product;
import com.simplecommerce_mdm.product.model.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find all products by shop with pagination
    Page<Product> findByShop(Shop shop, Pageable pageable);
    
    // Find products by shop and status
    Page<Product> findByShopAndStatus(Shop shop, ProductStatus status, Pageable pageable);
    
    // Find product by ID and shop (for authorization check)
    Optional<Product> findByIdAndShop(Long id, Shop shop);
    
    // Search products by name containing text (case insensitive)
    @Query("SELECT p FROM Product p WHERE p.shop = :shop AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> findByShopAndNameContaining(@Param("shop") Shop shop, 
                                              @Param("searchTerm") String searchTerm, 
                                              Pageable pageable);
    
    // Search with status filter
    @Query("SELECT p FROM Product p WHERE p.shop = :shop AND p.status = :status AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> findByShopAndStatusAndNameContaining(@Param("shop") Shop shop, 
                                                       @Param("status") ProductStatus status,
                                                       @Param("searchTerm") String searchTerm, 
                                                       Pageable pageable);
    
    // Admin methods - find all products by status
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    
    // Admin methods - complex search for admin with joins
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.shop s " +
           "LEFT JOIN FETCH s.user u " +
           "LEFT JOIN FETCH p.category c " +
           "WHERE (:status IS NULL OR p.status = :status) " +
           "AND (:shopId IS NULL OR s.id = :shopId) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId) " +
           "AND (:sellerEmail IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :sellerEmail, '%'))) " +
           "AND (:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> findProductsForAdmin(@Param("status") ProductStatus status,
                                       @Param("shopId") Long shopId,
                                       @Param("categoryId") Integer categoryId,
                                       @Param("sellerEmail") String sellerEmail,
                                       @Param("searchTerm") String searchTerm,
                                       Pageable pageable);

    // Buyer/Public methods - Featured products
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.shop s " +
           "LEFT JOIN FETCH p.category c " +
           "WHERE p.isFeatured = true " +
           "AND p.status = 'APPROVED'")
    Page<Product> findFeaturedProducts(Pageable pageable);
    
    // Buyer/Public methods - All approved products with search
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.shop s " +
           "LEFT JOIN FETCH p.category c " +
           "WHERE p.status = 'APPROVED' " +
           "AND (:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> findApprovedProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Buyer/Public methods - Find approved product by ID
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.shop s " +
           "LEFT JOIN FETCH p.category c " +
           "WHERE p.id = :id AND p.status = 'APPROVED'")
    Optional<Product> findApprovedProductById(@Param("id") Long id);
} 