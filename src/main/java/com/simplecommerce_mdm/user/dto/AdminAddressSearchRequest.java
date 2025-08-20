package com.simplecommerce_mdm.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAddressSearchRequest {
    
    private String userEmail;
    private String contactPhone;
    private String city;
    private String district;
    private String countryCode;
    private String contactFullName;
    private String streetAddress;
    
    // Pagination and sorting
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
