package com.simplecommerce_mdm.promotion.model;

import com.simplecommerce_mdm.common.enums.AppliedDiscountType;
import com.simplecommerce_mdm.order.model.MasterOrder;
import com.simplecommerce_mdm.user.model.User;
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
@Table(name = "user_applied_vouchers")
public class UserAppliedVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_order_id", nullable = false)
    private MasterOrder masterOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "applied_at", nullable = false)
    @Builder.Default
    private OffsetDateTime appliedAt = OffsetDateTime.now();

    @Column(name = "discount_amount_applied_on_master_order", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmountAppliedOnMasterOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_of_discount", nullable = false)
    private AppliedDiscountType typeOfDiscount;
} 