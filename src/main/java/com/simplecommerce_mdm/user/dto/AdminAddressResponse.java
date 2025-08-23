package com.simplecommerce_mdm.user.dto;

import com.simplecommerce_mdm.common.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAddressResponse {

    private Long userAddressId;  // UserAddress ID - rõ ràng hơn
    
    // Address ID for frontend operations
    private Long addressId;
    
    // User information
    private Long userId;
    private String userEmail;
    private String userFullName;
    
    // Address details
    private String streetAddress1;
    private String streetAddress2;
    private String ward;
    private String district;
    private String city;
    private String postalCode;
    private String countryCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    // UserAddress specific fields
    private String contactFullName;
    private String contactPhoneNumber;
    private AddressType addressType;
    private Boolean isDefaultShipping;
    private Boolean isDefaultBilling;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private String fullAddress;
    
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (streetAddress1 != null) {
            sb.append(streetAddress1);
        }
        if (streetAddress2 != null && !streetAddress2.trim().isEmpty()) {
            sb.append(", ").append(streetAddress2);
        }
        if (ward != null && !ward.trim().isEmpty()) {
            sb.append(", ").append(ward);
        }
        if (district != null) {
            sb.append(", ").append(district);
        }
        if (city != null) {
            sb.append(", ").append(city);
        }
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            sb.append(", ").append(postalCode);
        }
        if (countryCode != null) {
            sb.append(", ").append(countryCode);
        }
        return sb.toString();
    }
}
