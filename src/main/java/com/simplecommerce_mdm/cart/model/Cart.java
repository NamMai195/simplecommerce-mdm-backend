package com.simplecommerce_mdm.cart.model;

import com.simplecommerce_mdm.common.domain.BaseEntity;
import com.simplecommerce_mdm.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<CartItem> cartItems = new HashSet<>();
} 