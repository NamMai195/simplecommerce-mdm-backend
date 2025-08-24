package com.simplecommerce_mdm.user.service.impl;

import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.exception.InvalidDataException;
import com.simplecommerce_mdm.user.dto.AddressCreateRequest;
import com.simplecommerce_mdm.user.dto.AddressResponse;
import com.simplecommerce_mdm.user.dto.AddressUpdateRequest;
import com.simplecommerce_mdm.user.dto.AdminAddressResponse;
import com.simplecommerce_mdm.user.dto.AdminAddressSearchRequest;
import com.simplecommerce_mdm.user.model.Address;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.model.UserAddress;
import com.simplecommerce_mdm.user.repository.AddressRepository;
import com.simplecommerce_mdm.user.repository.UserAddressRepository;
import com.simplecommerce_mdm.user.repository.UserRepository;
import com.simplecommerce_mdm.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressResponse createAddress(AddressCreateRequest request, Long userId) {
        log.info("Creating address for user: {}", userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Create Address entity
        Address address = Address.builder()
                .streetAddress1(request.getStreetAddress1())
                .streetAddress2(request.getStreetAddress2())
                .ward(request.getWard())
                .district(request.getDistrict())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .countryCode(request.getCountryCode())
                .latitude(null) // TODO: Add geocoding service integration
                .longitude(null)
                .build();

        Address savedAddress = addressRepository.save(address);

        // Create UserAddress entity
        UserAddress userAddress = UserAddress.builder()
                .user(user)
                .address(savedAddress)
                .contactFullName(request.getContactFullName())
                .contactPhoneNumber(request.getContactPhoneNumber())
                .addressType(request.getAddressType())
                .isDefaultShipping(request.getIsDefaultShipping())
                .isDefaultBilling(request.getIsDefaultBilling())
                .build();

        // Handle default flags
        if (Boolean.TRUE.equals(request.getIsDefaultShipping())) {
            resetDefaultShippingAddresses(userId);
        }
        if (Boolean.TRUE.equals(request.getIsDefaultBilling())) {
            resetDefaultBillingAddresses(userId);
        }

        UserAddress savedUserAddress = userAddressRepository.save(userAddress);
        
        log.info("Address created successfully with id: {}", savedAddress.getId());
        return mapToAddressResponse(savedUserAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        log.info("Getting addresses for user: {}", userId);
        
        List<UserAddress> userAddresses = userAddressRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return userAddresses.stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId, Long userId) {
        log.info("Getting address: {} for user: {}", addressId, userId);
        
        UserAddress userAddress = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        
        return mapToAddressResponse(userAddress);
    }

    @Override
    public AddressResponse updateAddress(Long addressId, AddressUpdateRequest request, Long userId) {
        log.info("Updating address: {} for user: {}", addressId, userId);
        
        UserAddress userAddress = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Update Address entity
        Address address = userAddress.getAddress();
        if (request.getStreetAddress1() != null) {
            address.setStreetAddress1(request.getStreetAddress1());
        }
        if (request.getStreetAddress2() != null) {
            address.setStreetAddress2(request.getStreetAddress2());
        }
        if (request.getWard() != null) {
            address.setWard(request.getWard());
        }
        if (request.getDistrict() != null) {
            address.setDistrict(request.getDistrict());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getPostalCode() != null) {
            address.setPostalCode(request.getPostalCode());
        }
        if (request.getCountryCode() != null) {
            address.setCountryCode(request.getCountryCode());
        }

        // Update UserAddress entity
        if (request.getContactFullName() != null) {
            userAddress.setContactFullName(request.getContactFullName());
        }
        if (request.getContactPhoneNumber() != null) {
            userAddress.setContactPhoneNumber(request.getContactPhoneNumber());
        }
        if (request.getAddressType() != null) {
            userAddress.setAddressType(request.getAddressType());
        }

        // Handle default flags
        if (request.getIsDefaultShipping() != null) {
            if (Boolean.TRUE.equals(request.getIsDefaultShipping())) {
                resetDefaultShippingAddresses(userId);
            }
            userAddress.setIsDefaultShipping(request.getIsDefaultShipping());
        }
        if (request.getIsDefaultBilling() != null) {
            if (Boolean.TRUE.equals(request.getIsDefaultBilling())) {
                resetDefaultBillingAddresses(userId);
            }
            userAddress.setIsDefaultBilling(request.getIsDefaultBilling());
        }

        Address savedAddress = addressRepository.save(address);
        UserAddress savedUserAddress = userAddressRepository.save(userAddress);
        
        log.info("Address updated successfully with id: {}", addressId);
        return mapToAddressResponse(savedUserAddress);
    }

    @Override
    public void deleteAddress(Long addressId, Long userId) {
        log.info("Deleting address: {} for user: {}", addressId, userId);
        
        UserAddress userAddress = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Check if this is the only address
        long addressCount = userAddressRepository.countByUserId(userId);
        if (addressCount <= 1) {
            throw new InvalidDataException("Cannot delete the only address. User must have at least one address.");
        }

        // Check if this is a default address
        if (Boolean.TRUE.equals(userAddress.getIsDefaultShipping()) || 
            Boolean.TRUE.equals(userAddress.getIsDefaultBilling())) {
            throw new InvalidDataException("Cannot delete default address. Please set another address as default first.");
        }

        // Delete UserAddress first (maintains referential integrity)
        userAddressRepository.delete(userAddress);
        
        // Check if Address is used by other users
        if (!userAddressRepository.existsByAddress(userAddress.getAddress())) {
            addressRepository.delete(userAddress.getAddress());
        }
        
        log.info("Address deleted successfully with id: {}", addressId);
    }

    @Override
    public AddressResponse setDefaultShippingAddress(Long addressId, Long userId) {
        log.info("Setting default shipping address: {} for user: {}", addressId, userId);
        
        UserAddress userAddress = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Reset all default shipping addresses
        resetDefaultShippingAddresses(userId);
        
        // Set this address as default shipping
        userAddress.setIsDefaultShipping(true);
        UserAddress savedUserAddress = userAddressRepository.save(userAddress);
        
        log.info("Default shipping address set successfully");
        return mapToAddressResponse(savedUserAddress);
    }

    @Override
    public AddressResponse setDefaultBillingAddress(Long addressId, Long userId) {
        log.info("Setting default billing address: {} for user: {}", addressId, userId);
        
        UserAddress userAddress = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Reset all default billing addresses
        resetDefaultBillingAddresses(userId);
        
        // Set this address as default billing
        userAddress.setIsDefaultBilling(true);
        UserAddress savedUserAddress = userAddressRepository.save(userAddress);
        
        log.info("Default billing address set successfully");
        return mapToAddressResponse(savedUserAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getDefaultShippingAddress(Long userId) {
        log.info("Getting default shipping address for user: {}", userId);
        
        UserAddress userAddress = userAddressRepository.findByUserIdAndIsDefaultShippingTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No default shipping address found for user: " + userId));
        
        return mapToAddressResponse(userAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getDefaultBillingAddress(Long userId) {
        log.info("Getting default billing address for user: {}", userId);
        
        UserAddress userAddress = userAddressRepository.findByUserIdAndIsDefaultBillingTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No default billing address found for user: " + userId));
        
        return mapToAddressResponse(userAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateAddressOwnership(Long addressId, Long userId) {
        return userAddressRepository.findByIdAndUserId(addressId, userId).isPresent();
    }

    // Admin methods

    @Override
    @Transactional(readOnly = true)
    public Page<AdminAddressResponse> getAllAddressesForAdmin(AdminAddressSearchRequest searchRequest) {
        log.info("Admin getting all addresses with filters: {}", searchRequest);
        
        // Create Pageable object
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
            searchRequest.getPage(), 
            searchRequest.getSize(),
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.fromString(searchRequest.getSortDirection()),
                searchRequest.getSortBy()
            )
        );
        
        // Build dynamic query based on filters
        List<UserAddress> userAddresses = buildFilteredUserAddressQuery(searchRequest);
        
        // Convert to Page<AdminAddressResponse>
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), userAddresses.size());
        
        List<AdminAddressResponse> pageContent = userAddresses.subList(start, end)
            .stream()
            .map(this::mapToAdminAddressResponse)
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, 
            pageable, 
            userAddresses.size()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAddressResponse getAddressByIdForAdmin(Long addressId) {
        log.info("Admin getting address: {}", addressId);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        
        // Get UserAddress to get user info
        UserAddress userAddress = userAddressRepository.findByAddressId(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("UserAddress not found for address: " + addressId));
        
        return mapToAdminAddressResponse(userAddress);
    }

    @Override
    public AdminAddressResponse getAddressByUserAddressIdForAdmin(Long userAddressId) {
        log.info("Admin getting address by userAddressId: {}", userAddressId);
        
        UserAddress userAddress = userAddressRepository.findById(userAddressId)
                .orElseThrow(() -> new ResourceNotFoundException("UserAddress not found with id: " + userAddressId));
        
        return mapToAdminAddressResponse(userAddress);
    }



    @Override
    @Transactional
    public void deleteAddressByUserAddressIdForAdmin(Long userAddressId, Long adminId, String reason) {
        log.info("Admin {} deleting address by userAddressId: {} with reason: {}", adminId, userAddressId, reason);
        
        UserAddress userAddress = userAddressRepository.findById(userAddressId)
                .orElseThrow(() -> new ResourceNotFoundException("UserAddress not found with id: " + userAddressId));
        
        // Check if this is a default address
        if (Boolean.TRUE.equals(userAddress.getIsDefaultShipping()) || Boolean.TRUE.equals(userAddress.getIsDefaultBilling())) {
            throw new InvalidDataException("Cannot delete default address. Please set another address as default first.");
        }
        
        // Soft delete the UserAddress (Address entity remains for potential reuse)
        userAddressRepository.delete(userAddress);
        
        log.info("Address deleted successfully by admin {} with reason: {}", adminId, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAddressResponse> getAddressesByUserIdForAdmin(Long userId) {
        log.info("Admin getting addresses for user: {}", userId);
        
        List<UserAddress> userAddresses = userAddressRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return userAddresses.stream()
                .map(this::mapToAdminAddressResponse)
                .collect(Collectors.toList());
    }

    private List<UserAddress> buildFilteredUserAddressQuery(AdminAddressSearchRequest searchRequest) {
        // Get all user addresses with user and address data
        List<UserAddress> userAddresses = userAddressRepository.findAllUserAddressesWithUserAndAddress();
        
        // Apply filters in Java code
        return userAddresses.stream()
            .filter(ua -> searchRequest.getUserEmail() == null || 
                         ua.getUser().getEmail().toLowerCase().contains(searchRequest.getUserEmail().toLowerCase()))
            .filter(ua -> searchRequest.getContactPhone() == null || 
                         ua.getContactPhoneNumber().toLowerCase().contains(searchRequest.getContactPhone().toLowerCase()))
            .filter(ua -> searchRequest.getContactFullName() == null || 
                         ua.getContactFullName().toLowerCase().contains(searchRequest.getContactFullName().toLowerCase()))
            .filter(ua -> searchRequest.getCity() == null || 
                         ua.getAddress().getCity().equalsIgnoreCase(searchRequest.getCity()))
            .filter(ua -> searchRequest.getDistrict() == null || 
                         ua.getAddress().getDistrict().equalsIgnoreCase(searchRequest.getDistrict()))
            .filter(ua -> searchRequest.getCountryCode() == null || 
                         ua.getAddress().getCountryCode().equalsIgnoreCase(searchRequest.getCountryCode()))
            .filter(ua -> searchRequest.getStreetAddress() == null || 
                         ua.getAddress().getStreetAddress1().toLowerCase().contains(searchRequest.getStreetAddress().toLowerCase()))
            .collect(Collectors.toList());
    }

    private AdminAddressResponse mapToAdminAddressResponse(UserAddress userAddress) {
        Address address = userAddress.getAddress();
        User user = userAddress.getUser();
        
        return AdminAddressResponse.builder()
                .userAddressId(userAddress.getId())    // UserAddress ID - rõ ràng hơn
                .addressId(address.getId())            // Address ID for frontend operations
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userFullName(user.getFullName())
                .streetAddress1(address.getStreetAddress1())
                .streetAddress2(address.getStreetAddress2())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .countryCode(address.getCountryCode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .contactFullName(userAddress.getContactFullName())
                .contactPhoneNumber(userAddress.getContactPhoneNumber())
                .addressType(userAddress.getAddressType())
                .isDefaultShipping(userAddress.getIsDefaultShipping())
                .isDefaultBilling(userAddress.getIsDefaultBilling())
                .createdAt(userAddress.getCreatedAt())
                .updatedAt(userAddress.getUpdatedAt())
                .build();
    }

    private void resetDefaultShippingAddresses(Long userId) {
        userAddressRepository.resetDefaultShippingAddresses(userId);
    }

    private void resetDefaultBillingAddresses(Long userId) {
        userAddressRepository.resetDefaultBillingAddresses(userId);
    }

    private AddressResponse mapToAddressResponse(UserAddress userAddress) {
        Address address = userAddress.getAddress();
        
        return AddressResponse.builder()
                .id(userAddress.getId())
                .streetAddress1(address.getStreetAddress1())
                .streetAddress2(address.getStreetAddress2())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .countryCode(address.getCountryCode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .contactFullName(userAddress.getContactFullName())
                .contactPhoneNumber(userAddress.getContactPhoneNumber())
                .addressType(userAddress.getAddressType())
                .isDefaultShipping(userAddress.getIsDefaultShipping())
                .isDefaultBilling(userAddress.getIsDefaultBilling())
                .createdAt(userAddress.getCreatedAt())
                .updatedAt(userAddress.getUpdatedAt())
                .build();
    }
}
