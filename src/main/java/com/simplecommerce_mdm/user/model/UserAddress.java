package com.simplecommerce_mdm.user.model;

import com.simplecommerce_mdm.common.domain.BaseEntity;
import com.simplecommerce_mdm.common.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_addresses")
@SQLDelete(sql = "UPDATE user_addresses SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class UserAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "contact_full_name", nullable = false, length = 255)
    private String contactFullName;

    @Column(name = "contact_phone_number", nullable = false, length = 20)
    private String contactPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type")
    private AddressType addressType;

    @Column(name = "is_default_shipping", nullable = false)
    @Builder.Default
    private Boolean isDefaultShipping = false;

    @Column(name = "is_default_billing", nullable = false)
    @Builder.Default
    private Boolean isDefaultBilling = false;
} 