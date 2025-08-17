package com.simplecommerce_mdm.product.dto;

import com.simplecommerce_mdm.common.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductBuyerResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal basePrice;
    private ProductStatus status;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    
    // Category info
    private Integer categoryId;
    private String categoryName;
    
    // Shop info
    private Long shopId;
    private String shopName;
    private String shopSlug;
    private BigDecimal shopRating;
    
    // Images
    private List<String> imageUrls;
    
    // Variants info
    private List<ProductVariantBuyerResponse> variants;
    
    @Data
    public static class ProductVariantBuyerResponse {
        private Long id;
        private String sku;
        private BigDecimal finalPrice;
        private BigDecimal compareAtPrice;
        private Integer stockQuantity;
        private String options;
        private Boolean isActive;
    }
} 