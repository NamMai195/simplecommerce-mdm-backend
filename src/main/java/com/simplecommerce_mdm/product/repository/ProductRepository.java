package com.simplecommerce_mdm.product.repository;

import com.simplecommerce_mdm.common.enums.ProductStatus;
import com.simplecommerce_mdm.product.model.Product;
import com.simplecommerce_mdm.product.model.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

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
           "WHERE (:status IS NULL OR p.status = :status) " +
           "AND (:shopId IS NULL OR p.shop.id = :shopId) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    @EntityGraph(value = "Product.withShopAndCategory")
    Page<Product> findProductsForAdmin(@Param("status") ProductStatus status,
                                       @Param("shopId") Long shopId,
                                       @Param("categoryId") Integer categoryId,
                                       @Param("searchTerm") String searchTerm,
                                       Pageable pageable);

    // Buyer/Public methods - Featured products
    @Query("SELECT p FROM Product p " +
           "WHERE p.isFeatured = true " +
           "AND p.status = 'APPROVED'")
    Page<Product> findFeaturedProducts(Pageable pageable);
    
    // Buyer/Public methods - All approved products with search
    @Query("SELECT p FROM Product p " +
           "WHERE p.status = 'APPROVED' " +
           "AND (:searchTerm IS NULL OR p.name LIKE CONCAT('%', :searchTerm, '%') " +
           "     OR p.sku LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Product> findApprovedProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Buyer/Public methods - Find approved product by ID
    @Query("SELECT p FROM Product p " +
           "WHERE p.id = :id AND p.status = 'APPROVED'")
    Optional<Product> findApprovedProductById(@Param("id") Long id);

    // Buyer/Public methods - Products by category
    @Query("SELECT p FROM Product p " +
           "WHERE p.status = 'APPROVED' " +
           "AND p.category.id = :categoryId " +
           "AND (:searchTerm IS NULL OR p.name LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Product> findApprovedProductsByCategory(@Param("categoryId") Integer categoryId,
                                                 @Param("searchTerm") String searchTerm,
                                                 Pageable pageable);

    // Buyer/Public methods - Products by shop
    @Query("SELECT p FROM Product p " +
           "WHERE p.status = 'APPROVED' " +
           "AND p.shop.id = :shopId " +
           "AND (:searchTerm IS NULL OR p.name LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Product> findApprovedProductsByShop(@Param("shopId") Long shopId,
                                             @Param("searchTerm") String searchTerm,
                                             Pageable pageable);

    // Buyer/Public methods - Latest products (recently added)
    @Query("SELECT p FROM Product p " +
           "WHERE p.status = 'APPROVED' " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findLatestApprovedProducts(Pageable pageable);

    // Buyer/Public methods - Products with price range filter
    @Query("SELECT p FROM Product p " +
           "WHERE p.status = 'APPROVED' " +
           "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice) " +
           "AND (:searchTerm IS NULL OR p.name LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Product> findApprovedProductsByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice,
                                                   @Param("maxPrice") java.math.BigDecimal maxPrice,
                                                   @Param("searchTerm") String searchTerm,
                                                   Pageable pageable);

    // Buyer/Public methods - Advanced filters (category + price + shop)
    @Query("SELECT p FROM Product p " +
           "WHERE p.status = 'APPROVED' " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:shopId IS NULL OR p.shop.id = :shopId) " +
           "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice) " +
           "AND (:searchTerm IS NULL OR p.name LIKE CONCAT('%', :searchTerm, '%') " +
           "     OR p.sku LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Product> findApprovedProductsWithFilters(@Param("categoryId") Integer categoryId,
                                                  @Param("shopId") Long shopId,
                                                  @Param("minPrice") java.math.BigDecimal minPrice,
                                                  @Param("maxPrice") java.math.BigDecimal maxPrice,
                                                  @Param("searchTerm") String searchTerm,
                                                  Pageable pageable);
    
    // Native SQL version to avoid Hibernate type mapping issues
    @Query(value = "SELECT p.* FROM products p " +
           "WHERE p.status = 'APPROVED' " +
           "AND p.deleted_at IS NULL " +
           "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
           "AND (:shopId IS NULL OR p.shop_id = :shopId) " +
           "AND (:minPrice IS NULL OR p.base_price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.base_price <= :maxPrice) " +
           "AND (:searchTerm IS NULL OR p.name ILIKE '%' || :searchTerm || '%' " +
           "     OR p.sku ILIKE '%' || :searchTerm || '%')",
           nativeQuery = true)
    List<Product> findApprovedProductsWithFiltersNative(@Param("categoryId") Integer categoryId,
                                                        @Param("shopId") Long shopId,
                                                        @Param("minPrice") java.math.BigDecimal minPrice,
                                                        @Param("maxPrice") java.math.BigDecimal maxPrice,
                                                        @Param("searchTerm") String searchTerm);

    // Buyer/Public methods - Related products (same category, exclude current product)
    @Query("SELECT p FROM Product p " +
           "WHERE p.status = 'APPROVED' " +
           "AND p.category.id = :categoryId " +
           "AND p.id != :excludeProductId")
    Page<Product> findRelatedProducts(@Param("categoryId") Integer categoryId,
                                      @Param("excludeProductId") Long excludeProductId,
                                      Pageable pageable);

    // Buyer/Public methods - Get min and max prices for price range filter
    @Query("SELECT MIN(p.basePrice), MAX(p.basePrice) FROM Product p WHERE p.status = 'APPROVED'")
    Object[] findPriceRange();
    
    // Native SQL method to avoid Hibernate type mapping issues
    @Query(value = "SELECT MIN(base_price) as min_price, MAX(base_price) as max_price FROM products WHERE status = 'APPROVED' AND deleted_at IS NULL", 
           nativeQuery = true)
    Object[] findPriceRangeNative();
    
    // Simple methods to get min and max separately
    @Query(value = "SELECT MIN(base_price) FROM products WHERE status = 'APPROVED' AND deleted_at IS NULL", nativeQuery = true)
    BigDecimal findMinPriceNative();
    
    @Query(value = "SELECT MAX(base_price) FROM products WHERE status = 'APPROVED' AND deleted_at IS NULL", nativeQuery = true)
    BigDecimal findMaxPriceNative();
    
    // Debug method to check products directly
    @Query("SELECT p.id, p.name, p.status, p.basePrice FROM Product p WHERE p.status = 'APPROVED'")
    List<Object[]> findApprovedProductsWithPrices();

    // Stats: Top products by quantity/revenue for a shop
    @Query("SELECT oi.variant.product.id AS productId, oi.variant.product.name AS productName, " +
           "SUM(oi.quantity) AS totalQuantity, SUM(oi.subtotal) AS totalRevenue " +
           "FROM OrderItem oi WHERE oi.order.shop.id = :shopId AND oi.order.orderStatus = 'COMPLETED' " +
           "GROUP BY oi.variant.product.id, oi.variant.product.name ORDER BY totalQuantity DESC")
    List<Object[]> findTopProductsByQuantityForShop(@Param("shopId") Long shopId);

    @Query("SELECT oi.variant.product.id AS productId, oi.variant.product.name AS productName, " +
           "SUM(oi.quantity) AS totalQuantity, SUM(oi.subtotal) AS totalRevenue " +
           "FROM OrderItem oi WHERE oi.order.shop.id = :shopId AND oi.order.orderStatus = 'COMPLETED' " +
           "GROUP BY oi.variant.product.id, oi.variant.product.name ORDER BY totalRevenue DESC")
    List<Object[]> findTopProductsByRevenueForShop(@Param("shopId") Long shopId);
} 