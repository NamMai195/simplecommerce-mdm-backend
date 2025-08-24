package com.simplecommerce_mdm.order.model;

import com.simplecommerce_mdm.common.domain.BaseEntity;
import com.simplecommerce_mdm.common.enums.MasterOrderStatus;
import com.simplecommerce_mdm.promotion.model.UserAppliedVoucher;
import com.simplecommerce_mdm.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "master_orders")
public class MasterOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_group_number", nullable = false, unique = true, length = 50)
    private String orderGroupNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "shipping_address_snapshot", nullable = false, columnDefinition = "TEXT")
    private String shippingAddressSnapshot;

    @Column(name = "billing_address_snapshot", columnDefinition = "TEXT")
    private String billingAddressSnapshot;

    // Contact information for shipping (from UserAddress)
    @Column(name = "shipping_contact_name", nullable = false, length = 255)
    private String shippingContactName;

    @Column(name = "shipping_contact_phone", nullable = false, length = 20)
    private String shippingContactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false)
    @Builder.Default
    private MasterOrderStatus overallStatus = MasterOrderStatus.PENDING_PAYMENT;

    @Column(name = "total_amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmountPaid;

    @Column(name = "total_discount_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalDiscountAmount = BigDecimal.valueOf(0.00);

    @Column(name = "payment_method_snapshot", length = 100)
    private String paymentMethodSnapshot;

    @Column(name = "transaction_id_gateway", unique = true, length = 255)
    private String transactionIdGateway;

    @OneToMany(mappedBy = "masterOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "masterOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Payment> payments = new HashSet<>();

    @OneToMany(mappedBy = "masterOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<UserAppliedVoucher> appliedVouchers = new HashSet<>();
} 