package com.simplecommerce_mdm.user.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.user.dto.AddressResponse;
import com.simplecommerce_mdm.user.dto.AdminAddressResponse;
import com.simplecommerce_mdm.user.dto.AdminAddressSearchRequest;
import com.simplecommerce_mdm.user.dto.AdminAddressDeleteRequest;
import com.simplecommerce_mdm.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AddressAdminController {

    private final AddressService addressService;

    /**
     * Get all addresses with pagination and filtering (admin only)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminAddressResponse>>> getAllAddresses(
            @ModelAttribute AdminAddressSearchRequest searchRequest) {
        log.info("Admin getting all addresses with filters: {}", searchRequest);
        
        Page<AdminAddressResponse> addresses = addressService.getAllAddressesForAdmin(searchRequest);
        
        return ResponseEntity.ok(ApiResponse.<Page<AdminAddressResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All addresses retrieved successfully")
                .data(addresses)
                .build());
    }

    /**
     * Get addresses by user ID (admin only)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AdminAddressResponse>>> getAddressesByUserId(@PathVariable Long userId) {
        log.info("Admin getting addresses for user: {}", userId);
        
        List<AdminAddressResponse> addresses = addressService.getAddressesByUserIdForAdmin(userId);
        
        return ResponseEntity.ok(ApiResponse.<List<AdminAddressResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User addresses retrieved successfully")
                .data(addresses)
                .build());
    }

    /**
     * Get address by UserAddress ID (admin only)
     */
    @GetMapping("/{userAddressId}")
    public ResponseEntity<ApiResponse<AdminAddressResponse>> getAddressById(@PathVariable Long userAddressId) {
        log.info("Admin getting address by userAddressId: {}", userAddressId);
        
        AdminAddressResponse address = addressService.getAddressByUserAddressIdForAdmin(userAddressId);
        
        return ResponseEntity.ok(ApiResponse.<AdminAddressResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Address retrieved successfully")
                .data(address)
                .build());
    }

    /**
     * Delete address by UserAddress ID (admin only)
     */
    @DeleteMapping("/{userAddressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long userAddressId,
            @RequestBody(required = false) AdminAddressDeleteRequest deleteRequest) {
        
        String reason = deleteRequest != null ? deleteRequest.getReason() : "Admin deletion";
        Long adminId = deleteRequest != null ? deleteRequest.getAdminId() : null;
        
        log.info("Admin {} deleting address: {} with reason: {}", adminId, userAddressId, reason);
        
        addressService.deleteAddressByUserAddressIdForAdmin(userAddressId, adminId, reason);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Address deleted successfully")
                .build());
    }
}
