package com.simplecommerce_mdm.shop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopAddressUpdateRequest {

    @NotNull(message = "UserAddress ID is required")
    private Long userAddressId;
}
