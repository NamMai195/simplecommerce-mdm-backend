package com.simplecommerce_mdm.shop.service.impl;

import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.product.repository.ShopRepository;
import com.simplecommerce_mdm.shop.dto.*;
import com.simplecommerce_mdm.shop.service.ShopService;
import com.simplecommerce_mdm.user.model.Role;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.model.Address;
import com.simplecommerce_mdm.user.repository.RoleRepository;
import com.simplecommerce_mdm.user.repository.UserRepository;
import com.simplecommerce_mdm.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ShopResponse createShop(ShopCreateRequest createRequest, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        
        // Check if user already has a shop (ONE SHOP PER USER RULE)
        if (shopRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("User already has a shop. Each user can only create one shop.");
        }

        Shop shop = Shop.builder()
                .user(user)
                .name(createRequest.getName())
                .slug(createRequest.getName().toLowerCase().replaceAll("\\s+", "-"))
                .description(createRequest.getDescription())
                .contactEmail(createRequest.getContactEmail())
                .contactPhone(createRequest.getContactPhone())
                .isActive(false) // Pending approval
                .rating(BigDecimal.valueOf(0.0))
                .build();

        // Set address if provided
        if (createRequest.getAddressId() != null) {
            Address address = addressRepository.findById(createRequest.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + createRequest.getAddressId()));
            shop.setAddress(address);
        }

        shop = shopRepository.save(shop);
        log.info("Shop created for user {}: {}", user.getEmail(), shop.getName());
        
        return convertToShopResponse(shop);
    }

    @Override
    public ShopResponse getUserShop(CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Shop shop = shopRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User has no shop"));
        
        return convertToShopResponse(shop);
    }

    @Override
    @Transactional
    public ShopResponse updateShop(ShopCreateRequest updateRequest, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Shop shop = shopRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User has no shop"));
        
        // Only allow updates if shop is not approved yet
        if (shop.getIsActive() && shop.getApprovedAt() != null) {
            throw new IllegalStateException("Cannot update approved shop. Contact admin for changes.");
        }
        
        // Update shop details
        shop.setName(updateRequest.getName());
        shop.setSlug(updateRequest.getName().toLowerCase().replaceAll("\\s+", "-"));
        shop.setDescription(updateRequest.getDescription());
        shop.setContactEmail(updateRequest.getContactEmail());
        shop.setContactPhone(updateRequest.getContactPhone());
        
        shop = shopRepository.save(shop);
        log.info("Shop updated for user {}: {}", user.getEmail(), shop.getName());
        
        return convertToShopResponse(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShopAdminResponse> getShopsForAdmin(ShopAdminSearchRequest searchRequest) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(searchRequest.getSortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
                searchRequest.getSortBy()
        );
        
        // Use native query to avoid type mapping issues
        List<Shop> allShops = shopRepository.findShopsForAdminNative(
                searchRequest.getSearchTerm(),
                parseStatus(searchRequest.getStatus())
        );
        
        // Apply case-insensitive search filtering if searchTerm is provided
        if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            String searchTerm = searchRequest.getSearchTerm().toLowerCase();
            allShops = allShops.stream()
                    .filter(shop -> shop.getName() != null && 
                                   shop.getName().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
        }
        
        // Apply sorting
        allShops.sort((s1, s2) -> {
            int result = 0;
            switch (searchRequest.getSortBy().toLowerCase()) {
                case "createdat":
                    result = s1.getCreatedAt().compareTo(s2.getCreatedAt());
                    break;
                case "name":
                    result = s1.getName().compareTo(s2.getName());
                    break;
                case "isactive":
                    result = s1.getIsActive().compareTo(s2.getIsActive());
                    break;
                default:
                    result = s1.getCreatedAt().compareTo(s2.getCreatedAt());
            }
            return searchRequest.getSortDirection().equalsIgnoreCase("desc") ? -result : result;
        });
        
        // Apply pagination manually
        int totalElements = allShops.size();
        int pageSize = searchRequest.getSize();
        int currentPage = searchRequest.getPage();
        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalElements);
        
        List<Shop> pageContent = allShops.subList(startIndex, endIndex);
        
        // Create Page object manually
        Pageable pageable = PageRequest.of(currentPage, pageSize, sort);
        Page<Shop> shopsPage = new PageImpl<>(pageContent, pageable, totalElements);
        
        // Convert to DTOs - this will trigger lazy loading of User and Address
        return shopsPage.map(this::convertToShopAdminResponse);
    }

    @Override
    public ShopAdminResponse getShopByIdForAdmin(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        
        return convertToShopAdminResponse(shop);
    }

    @Override
    @Transactional
    public ShopAdminResponse approveShop(Long shopId, ShopApprovalRequest approvalRequest, CustomUserDetails adminDetails) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        
        if (shop.getIsActive() && shop.getApprovedAt() != null) {
            throw new IllegalStateException("Shop is already approved");
        }
        
        // Approve shop
        shop.setIsActive(true);
        shop.setApprovedAt(OffsetDateTime.now());
        shop.setRejectionReason(null); // Clear any previous rejection reason
        
        // Auto-assign SELLER role to shop owner
        User shopOwner = shop.getUser();
        Role sellerRole = roleRepository.findByRoleName("SELLER")
                .orElseThrow(() -> new ResourceNotFoundException("SELLER role not found"));
        
        if (!shopOwner.getRoles().contains(sellerRole)) {
            shopOwner.getRoles().add(sellerRole);
            userRepository.save(shopOwner);
            log.info("SELLER role granted to user {}", shopOwner.getEmail());
        }
        
        shop = shopRepository.save(shop);
        log.info("Shop {} approved by admin {}", shop.getName(), adminDetails.getUser().getEmail());
        
        return convertToShopAdminResponse(shop);
    }

    @Override
    @Transactional
    public ShopAdminResponse rejectShop(Long shopId, ShopApprovalRequest rejectionRequest, CustomUserDetails adminDetails) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        
        shop.setIsActive(false);
        shop.setApprovedAt(null);
        shop.setRejectionReason(rejectionRequest.getRejectionReason());
        
        shop = shopRepository.save(shop);
        log.info("Shop {} rejected by admin {}", shop.getName(), adminDetails.getUser().getEmail());
        
        return convertToShopAdminResponse(shop);
    }

    @Override
    @Transactional
    public ShopAdminResponse suspendShop(Long shopId, ShopApprovalRequest suspensionRequest, CustomUserDetails adminDetails) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        
        if (!shop.getIsActive()) {
            throw new IllegalStateException("Shop is not active");
        }
        
        shop.setIsActive(false);
        shop.setRejectionReason(suspensionRequest.getRejectionReason());
        
        shop = shopRepository.save(shop);
        log.info("Shop {} suspended by admin {}", shop.getName(), adminDetails.getUser().getEmail());
        
        return convertToShopAdminResponse(shop);
    }

    @Override
    @Transactional
    public ShopAdminResponse reactivateShop(Long shopId, CustomUserDetails adminDetails) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        
        if (shop.getIsActive()) {
            throw new IllegalStateException("Shop is already active");
        }
        
        shop.setIsActive(true);
        shop.setRejectionReason(null);
        
        shop = shopRepository.save(shop);
        log.info("Shop {} reactivated by admin {}", shop.getName(), adminDetails.getUser().getEmail());
        
        return convertToShopAdminResponse(shop);
    }

    @Override
    public ShopResponse getSellerShop(CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));
        
        if (!shop.getIsActive() || shop.getApprovedAt() == null) {
            throw new IllegalStateException("Shop is not approved yet");
        }
        
        return convertToShopResponse(shop);
    }

    @Override
    public Object getSellerShopStats(CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));
        
        if (!shop.getIsActive() || shop.getApprovedAt() == null) {
            throw new IllegalStateException("Shop is not approved yet");
        }
        
        // Basic shop statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("shopId", shop.getId());
        stats.put("shopName", shop.getName());
        stats.put("totalProducts", shopRepository.countProductsByShopId(shop.getId()));
        stats.put("approvedProducts", shopRepository.countApprovedProductsByShopId(shop.getId()));
        stats.put("pendingProducts", shopRepository.countPendingProductsByShopId(shop.getId()));
        stats.put("rating", shop.getRating());
        stats.put("isActive", shop.getIsActive());
        stats.put("approvedAt", shop.getApprovedAt());
        
        return stats;
    }

    @Override
    @Transactional
    public ShopResponse updateShopAddress(Long addressId, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));
        
        if (!shop.getIsActive() || shop.getApprovedAt() == null) {
            throw new IllegalStateException("Shop is not approved yet");
        }
        
        // Validate that the address exists
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        
        // TODO: Add address ownership validation
        // For now, we'll allow any address to be used
        // In production, you might want to validate that the address belongs to the seller
        // or that the seller has permission to use this address
        
        // Update shop address
        shop.setAddress(address);
        shop = shopRepository.save(shop);
        
        log.info("Shop address updated for shop {}: {}", shop.getName(), addressId);
        
        return convertToShopResponse(shop);
    }

    private Boolean parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        
        return switch (status.toLowerCase()) {
            case "active", "approved" -> true;
            case "inactive", "pending", "rejected" -> false;
            default -> null;
        };
    }

    private ShopResponse convertToShopResponse(Shop shop) {
        ShopResponse response = modelMapper.map(shop, ShopResponse.class);
        
        // Set address info with correct field names
        if (shop.getAddress() != null) {
            response.setAddressLine1(shop.getAddress().getStreetAddress1());
            response.setAddressLine2(shop.getAddress().getStreetAddress2());
            response.setCity(shop.getAddress().getCity());
            response.setPostalCode(shop.getAddress().getPostalCode());
            response.setCountry(shop.getAddress().getCountryCode());
        }
        
        // Set product count
        response.setTotalProducts(shopRepository.countProductsByShopId(shop.getId()));
        
        return response;
    }

    private ShopAdminResponse convertToShopAdminResponse(Shop shop) {
        ShopAdminResponse response = modelMapper.map(shop, ShopAdminResponse.class);
        
        // Set seller info
        if (shop.getUser() != null) {
            response.setSellerId(shop.getUser().getId());
            response.setSellerEmail(shop.getUser().getEmail());
            response.setSellerFullName(shop.getUser().getFullName());
            response.setSellerPhone(shop.getUser().getPhoneNumber());
        }
        
        // Set address info with correct field names
        if (shop.getAddress() != null) {
            response.setAddressLine1(shop.getAddress().getStreetAddress1());
            response.setAddressLine2(shop.getAddress().getStreetAddress2());
            response.setCity(shop.getAddress().getCity());
            response.setPostalCode(shop.getAddress().getPostalCode());
            response.setCountry(shop.getAddress().getCountryCode());
        }
        
        // Set product statistics
        response.setTotalProducts(shopRepository.countProductsByShopId(shop.getId()));
        response.setApprovedProducts(shopRepository.countApprovedProductsByShopId(shop.getId()));
        response.setPendingProducts(shopRepository.countPendingProductsByShopId(shop.getId()));
        
        return response;
    }
} 