package com.simplecommerce_mdm.user.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.user.dto.AddressCreateRequest;
import com.simplecommerce_mdm.user.dto.AddressResponse;
import com.simplecommerce_mdm.user.dto.AddressUpdateRequest;
import com.simplecommerce_mdm.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.simplecommerce_mdm.config.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * Create a new address for the current user
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Valid @RequestBody AddressCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("Creating address for user: {}", userId);
        
        AddressResponse address = addressService.createAddress(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AddressResponse>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .message("Address created successfully")
                        .data(address)
                        .build());
    }

    /**
     * Get all addresses for the current user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("Getting addresses for user: {}", userId);
        
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        
        return ResponseEntity.ok(ApiResponse.<List<AddressResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Addresses retrieved successfully")
                .data(addresses)
                .build());
    }

    /**
     * Get address by ID
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @PathVariable Long addressId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("Getting address: {} for user: {}", addressId, userId);
        
        AddressResponse address = addressService.getAddressById(addressId, userId);
        
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Address retrieved successfully")
                .data(address)
                .build());
    }

    /**
     * Update address by ID
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("Updating address: {} for user: {}", addressId, userId);
        
        AddressResponse address = addressService.updateAddress(addressId, request, userId);
        
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Address updated successfully")
                .data(address)
                .build());
    }

    /**
     * Delete address by ID
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("Deleting address: {} for user: {}", addressId, userId);
        
        addressService.deleteAddress(addressId, userId);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Address deleted successfully")
                .build());
    }

    /**
     * Set address as default shipping
     */
    @PatchMapping("/{addressId}/default-shipping")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultShippingAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("Setting default shipping address: {} for user: {}", addressId, userId);
        
        AddressResponse address = addressService.setDefaultShippingAddress(addressId, userId);
        
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Default shipping address set successfully")
                .data(address)
                .build());
    }

    /**
     * Set address as default billing
     */
    @PatchMapping("/{addressId}/default-billing")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultBillingAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("Setting default shipping address: {} for user: {}", addressId, userId);
        
        AddressResponse address = addressService.setDefaultBillingAddress(addressId, userId);
        
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Default billing address set successfully")
                .data(address)
                .build());
    }

    /**
     * Get default shipping address
     */
    @GetMapping("/default-shipping")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultShippingAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("Getting default shipping address for user: {}", userId);
        
        AddressResponse address = addressService.getDefaultShippingAddress(userId);
        
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Default shipping address retrieved successfully")
                .data(address)
                .build());
    }

    /**
     * Get default billing address
     */
    @GetMapping("/default-billing")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultBillingAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        log.info("Getting default billing address for user: {}", userId);
        
        AddressResponse address = addressService.getDefaultBillingAddress(userId);
        
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Default billing address retrieved successfully")
                .data(address)
                .build());
    }
}
