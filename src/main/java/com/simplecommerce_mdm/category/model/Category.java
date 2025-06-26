package com.simplecommerce_mdm.category.model;

import com.simplecommerce_mdm.common.domain.BaseEntity;
import com.simplecommerce_mdm.product.model.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
@SQLDelete(sql = "UPDATE categories SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Category> children = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Product> products = new HashSet<>();
} 