package com.simplecommerce_mdm.cart.model;

import com.simplecommerce_mdm.product.model.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_variant", columnList = "cart_id, variant_id", unique = true)
})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_at_addition", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtAddition;

    @Column(name = "added_at", nullable = false)
    @Builder.Default
    private OffsetDateTime addedAt = OffsetDateTime.now();
} 