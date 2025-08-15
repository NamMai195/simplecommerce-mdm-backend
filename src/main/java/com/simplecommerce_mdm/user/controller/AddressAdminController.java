package com.simplecommerce_mdm.user.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.user.dto.AddressResponse;
import com.simplecommerce_mdm.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Get all addresses (admin only)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllAddresses() {
        log.info("Admin getting all addresses");
        
        // TODO: Implement pagination and filtering for admin
        // For now, return empty list - will be implemented later
        List<AddressResponse> addresses = List.of();
        
        return ResponseEntity.ok(ApiResponse.<List<AddressResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All addresses retrieved successfully")
                .data(addresses)
                .build());
    }

    /**
     * Get addresses by user ID (admin only)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddressesByUserId(@PathVariable Long userId) {
        log.info("Admin getting addresses for user: {}", userId);
        
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        
        return ResponseEntity.ok(ApiResponse.<List<AddressResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User addresses retrieved successfully")
                .data(addresses)
                .build());
    }

    /**
     * Get address by ID (admin only)
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(@PathVariable Long addressId) {
        log.info("Admin getting address: {}", addressId);
        
        // TODO: Implement admin address retrieval
        // For now, return error - will be implemented later
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.<AddressResponse>builder()
                        .statusCode(HttpStatus.NOT_IMPLEMENTED.value())
                        .message("Admin address retrieval not implemented yet")
                        .build());
    }

    /**
     * Delete address by ID (admin only)
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long addressId) {
        log.info("Admin deleting address: {}", addressId);
        
        // TODO: Implement admin address deletion
        // For now, return error - will be implemented later
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.<Void>builder()
                        .statusCode(HttpStatus.NOT_IMPLEMENTED.value())
                        .message("Admin address deletion not implemented yet")
                        .build());
    }
}
