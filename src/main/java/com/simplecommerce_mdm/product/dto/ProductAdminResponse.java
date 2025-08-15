package com.simplecommerce_mdm.product.dto;

import com.simplecommerce_mdm.common.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ProductAdminResponse {
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private BigDecimal basePrice;
    private Integer categoryId;
    private String categoryName;
    private Long shopId;
    private String shopName;
    private String sellerEmail;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private OffsetDateTime approvedAt;
    private String rejectionReason;
    private Boolean isFeatured;
    private OffsetDateTime publishedAt;
    private BigDecimal weightGrams;
    private String dimensions;
    private String attributes;
    private List<ProductVariantAdminResponse> variants;
    private List<String> imageUrls;

    @Data
    public static class ProductVariantAdminResponse {
        private Long id;
        private String sku;
        private BigDecimal finalPrice;
        private BigDecimal compareAtPrice;
        private Integer stockQuantity;
        private String options;
        private Boolean isActive;
    }
} 