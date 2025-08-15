package com.simplecommerce_mdm.product.model;

import com.simplecommerce_mdm.category.model.Category;
import com.simplecommerce_mdm.common.domain.BaseEntity;
import com.simplecommerce_mdm.common.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_shop_slug", columnList = "shop_id, slug", unique = true)
})
@SQLDelete(sql = "UPDATE products SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@NamedEntityGraph(
    name = "Product.withShopAndCategory",
    attributeNodes = {
        @NamedAttributeNode("shop"),
        @NamedAttributeNode("category")
    }
)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.PENDING_APPROVAL;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "weight_grams", precision = 10, scale = 2)
    private BigDecimal weightGrams;

    @Column(name = "dimensions", columnDefinition = "TEXT")
    private String dimensions;

    @Column(name = "attributes", columnDefinition = "TEXT")
    @Builder.Default
    private String attributes = "{}";

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<ProductVariant> variants = new HashSet<>();
    
    @Column(name = "rating")
    private Float rating;
    
    @Column(name = "review_count")
    private Integer reviewCount;
}