package com.simplecommerce_mdm.user.service;

import com.simplecommerce_mdm.user.dto.AddressCreateRequest;
import com.simplecommerce_mdm.user.dto.AddressResponse;
import com.simplecommerce_mdm.user.dto.AddressUpdateRequest;
import com.simplecommerce_mdm.user.dto.AdminAddressResponse;
import com.simplecommerce_mdm.user.dto.AdminAddressSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AddressService {

    /**
     * Create a new address for the current user
     */
    AddressResponse createAddress(AddressCreateRequest request, Long userId);

    /**
     * Get all addresses for the current user
     */
    List<AddressResponse> getUserAddresses(Long userId);

    /**
     * Get address by ID (with user validation)
     */
    AddressResponse getAddressById(Long addressId, Long userId);

    /**
     * Update address by ID (with user validation)
     */
    AddressResponse updateAddress(Long addressId, AddressUpdateRequest request, Long userId);

    /**
     * Delete address by ID (with user validation)
     */
    void deleteAddress(Long addressId, Long userId);

    /**
     * Set address as default shipping
     */
    AddressResponse setDefaultShippingAddress(Long addressId, Long userId);

    /**
     * Set address as default billing
     */
    AddressResponse setDefaultBillingAddress(Long addressId, Long userId);

    /**
     * Get default shipping address for user
     */
    AddressResponse getDefaultShippingAddress(Long userId);

    /**
     * Get default billing address for user
     */
    AddressResponse getDefaultBillingAddress(Long userId);

    /**
     * Validate address exists and belongs to user
     */
    boolean validateAddressOwnership(Long addressId, Long userId);

    // Admin methods

    /**
     * Get all addresses with pagination and filtering (admin only)
     */
    Page<AdminAddressResponse> getAllAddressesForAdmin(AdminAddressSearchRequest searchRequest);

    /**
     * Get address by ID for admin (no user validation)
     */
    AdminAddressResponse getAddressByIdForAdmin(Long addressId);

    /**
     * Delete address by ID for admin (no user validation)
     */
    void deleteAddressForAdmin(Long addressId, Long adminId, String reason);

    /**
     * Get addresses by user ID for admin
     */
    List<AdminAddressResponse> getAddressesByUserIdForAdmin(Long userId);
}
