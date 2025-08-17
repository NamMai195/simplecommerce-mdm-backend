package com.simplecommerce_mdm.product.repository;

import com.simplecommerce_mdm.product.model.Product;
import com.simplecommerce_mdm.product.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
    /**
     * Find variant by ID and ensure it's active
     */
    @Query("SELECT pv FROM ProductVariant pv " +
           "WHERE pv.id = :variantId AND pv.isActive = true AND pv.product.status = 'APPROVED'")
    Optional<ProductVariant> findActiveVariantById(@Param("variantId") Long variantId);
    
    /**
     * Find all variants of a product
     */
    List<ProductVariant> findByProduct(Product product);
    
    /**
     * Find variants by product with active status
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product = :product AND pv.isActive = true")
    List<ProductVariant> findActiveVariantsByProduct(@Param("product") Product product);
    
    /**
     * Check if variant has enough stock
     */
    @Query("SELECT CASE WHEN pv.stockQuantity >= :quantity THEN true ELSE false END " +
           "FROM ProductVariant pv WHERE pv.id = :variantId")
    Boolean hasEnoughStock(@Param("variantId") Long variantId, @Param("quantity") Integer quantity);
    
    /**
     * Get current stock quantity
     */
    @Query("SELECT pv.stockQuantity FROM ProductVariant pv WHERE pv.id = :variantId")
    Optional<Integer> getStockQuantity(@Param("variantId") Long variantId);
    
    /**
     * Find variant by SKU
     */
    Optional<ProductVariant> findBySku(String sku);
} 