package com.simplecommerce_mdm.user.dto;

import com.simplecommerce_mdm.common.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateRequest {

    @NotBlank(message = "Street address 1 is required")
    @Size(max = 255, message = "Street address 1 must not exceed 255 characters")
    private String streetAddress1;

    @Size(max = 255, message = "Street address 2 must not exceed 255 characters")
    private String streetAddress2;

    @NotBlank(message = "Ward is required")
    @Size(max = 100, message = "Ward must not exceed 100 characters")
    private String ward;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city; 

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
    private String countryCode = "VN";

    @NotBlank(message = "Contact full name is required")
    @Size(max = 255, message = "Contact full name must not exceed 255 characters")
    private String contactFullName;

    @NotBlank(message = "Contact phone number is required")
    @Size(max = 20, message = "Contact phone number must not exceed 20 characters")
    private String contactPhoneNumber;

    @NotNull(message = "Address type is required")
    private AddressType addressType;

    @Builder.Default
    private Boolean isDefaultShipping = false;

    @Builder.Default
    private Boolean isDefaultBilling = false;
}
