package com.simplecommerce_mdm.order.model;

import com.simplecommerce_mdm.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shipping_methods")
public class ShippingMethod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_cost", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal baseCost = BigDecimal.valueOf(0);

    @Column(name = "calculation_rules", columnDefinition = "TEXT")
    private String calculationRules;

    @Column(name = "estimated_delivery_time_min_days")
    private Integer estimatedDeliveryTimeMinDays;

    @Column(name = "estimated_delivery_time_max_days")
    private Integer estimatedDeliveryTimeMaxDays;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
} 