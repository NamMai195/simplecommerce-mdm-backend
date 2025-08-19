package com.simplecommerce_mdm.shop.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.shop.dto.ShopResponse;
import com.simplecommerce_mdm.shop.dto.ShopProfileResponse;
import com.simplecommerce_mdm.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
@Tag(name = "Public Shop", description = "Public APIs for shop info")
public class ShopPublicController {

    private final ShopService shopService;

    @GetMapping("/{shopId}")
    @Operation(summary = "Get public shop details", description = "Retrieve public details of an approved and active shop by ID")
    public ResponseEntity<ApiResponse<ShopResponse>> getPublicShop(
            @Parameter(description = "Shop ID") @PathVariable Long shopId) {

        log.info("Public get shop details: {}", shopId);

        ShopResponse shopResponse = shopService.getShopByIdPublic(shopId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<ShopResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Shop details retrieved successfully")
                        .data(shopResponse)
                        .build());
    }

    @GetMapping("/{shopId}/profile")
    @Operation(summary = "Get public shop profile", description = "Minimal profile info for shop page header")
    public ResponseEntity<ApiResponse<ShopProfileResponse>> getPublicShopProfile(
            @Parameter(description = "Shop ID") @PathVariable Long shopId) {

        log.info("Public get shop profile: {}", shopId);

        ShopProfileResponse profile = shopService.getShopProfile(shopId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<ShopProfileResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Shop profile retrieved successfully")
                        .data(profile)
                        .build());
    }
}


