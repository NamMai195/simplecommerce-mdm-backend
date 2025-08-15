package com.simplecommerce_mdm.product.model;

import com.simplecommerce_mdm.common.domain.BaseEntity;
import com.simplecommerce_mdm.user.model.Address;
import com.simplecommerce_mdm.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shops")
@SQLDelete(sql = "UPDATE shops SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@NamedEntityGraph(
    name = "Shop.withUserAndAddress",
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("address")
    }
)
public class Shop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "name", nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 255, columnDefinition = "VARCHAR(255)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String description;

    @Column(name = "logo_cloudinary_public_id", length = 255, columnDefinition = "VARCHAR(255)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String logoCloudinaryPublicId;

    @Column(name = "cover_image_cloudinary_public_id", length = 255, columnDefinition = "VARCHAR(255)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String coverImageCloudinaryPublicId;

    @Column(name = "contact_email", length = 255, columnDefinition = "VARCHAR(255)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20, columnDefinition = "VARCHAR(20)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String contactPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(name = "rating")
    @Builder.Default
    private BigDecimal rating = BigDecimal.valueOf(0.00);

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @OneToMany(mappedBy = "shop", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Product> products = new HashSet<>();
} 