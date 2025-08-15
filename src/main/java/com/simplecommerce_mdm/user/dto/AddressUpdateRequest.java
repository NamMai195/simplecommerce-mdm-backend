package com.simplecommerce_mdm.user.dto;

import com.simplecommerce_mdm.common.enums.AddressType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressUpdateRequest {

    @Size(max = 255, message = "Street address 1 must not exceed 255 characters")
    private String streetAddress1;

    @Size(max = 255, message = "Street address 2 must not exceed 255 characters")
    private String streetAddress2;

    @Size(max = 100, message = "Ward must not exceed 100 characters")
    private String ward;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
    private String countryCode;

    @Size(max = 255, message = "Contact full name must not exceed 255 characters")
    private String contactFullName;

    @Size(max = 20, message = "Contact phone number must not exceed 20 characters")
    private String contactPhoneNumber;

    private AddressType addressType;

    private Boolean isDefaultShipping;

    private Boolean isDefaultBilling;
}
