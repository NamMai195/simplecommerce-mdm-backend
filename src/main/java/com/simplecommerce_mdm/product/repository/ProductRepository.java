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
} 