package com.simplecommerce_mdm.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAddressDeleteRequest {
    
    private String reason;
    private Long adminId;
}
