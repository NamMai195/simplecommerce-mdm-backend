package com.simplecommerce_mdm.shop.service.impl;

import com.simplecommerce_mdm.cloudinary.service.CloudinaryService;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.product.repository.ShopRepository;
import com.simplecommerce_mdm.shop.dto.*;
import com.simplecommerce_mdm.shop.service.ShopService;
import com.simplecommerce_mdm.user.model.Role;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.RoleRepository;
import com.simplecommerce_mdm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ShopResponse createShop(ShopCreateRequest createRequest, CustomUserDetails userDetails) {
        log.info("Creating shop for user: {}", userDetails.getUser().getEmail());

        User user = userDetails.getUser();
        
        // Check if user already has a shop
        if (shopRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("User already has a shop");
        }

        // Create shop entity
        Shop shop = Shop.builder()
                .user(user)
                .name(createRequest.getName())
                .slug(generateSlug(createRequest.getName()))
                .description(createRequest.getDescription())
                .contactEmail(createRequest.getContactEmail())
                .contactPhone(createRequest.getContactPhone())
                .isActive(false) // Pending approval
                .build();

        Shop savedShop = shopRepository.save(shop);
        
        log.info("Shop created successfully with ID: {} for user: {}", savedShop.getId(), user.getEmail());
        
        return convertToShopResponse(savedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getUserShop(CustomUserDetails userDetails) {
        Shop shop = shopRepository.findByUser(userDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("User has no shop"));
        
        return convertToShopResponse(shop);
    }

    @Override
    @Transactional
    public ShopResponse updateShop(ShopCreateRequest updateRequest, CustomUserDetails userDetails) {
        Shop shop = shopRepository.findByUser(userDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("User has no shop"));
        
        if (shop.getIsActive()) {
            throw new IllegalStateException("Cannot update approved shop. Use seller update endpoint.");
        }
        
        shop.setName(updateRequest.getName());
        shop.setSlug(generateSlug(updateRequest.getName()));
        shop.setDescription(updateRequest.getDescription());
        shop.setContactEmail(updateRequest.getContactEmail());
        shop.setContactPhone(updateRequest.getContactPhone());
        
        Shop savedShop = shopRepository.save(shop);
        return convertToShopResponse(savedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShopAdminResponse> getShopsForAdmin(ShopAdminSearchRequest searchRequest) {
        Boolean isActive = parseStatusFilter(searchRequest.getStatus());
        
        Sort sort = searchRequest.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(searchRequest.getSortBy()).descending()
                : Sort.by(searchRequest.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        Page<Shop> shopPage = shopRepository.findShopsForAdmin(
                searchRequest.getSearchTerm(),
                isActive,
                searchRequest.getSellerEmail(),
                searchRequest.getCity(),
                searchRequest.getCountry(),
                pageable);

        return shopPage.map(this::convertToShopAdminResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopAdminResponse getShopByIdForAdmin(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopId));
        
        return convertToShopAdminResponse(shop);
    }

    @Override
    @Transactional
    public ShopAdminResponse approveShop(Long shopId, ShopApprovalRequest approvalRequest, CustomUserDetails adminDetails) {
        log.info("Admin {} approving shop: {}", adminDetails.getUser().getEmail(), shopId);
        
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopId));
        
        // Update shop status
        shop.setIsActive(true);
        shop.setApprovedAt(OffsetDateTime.now());
        shop.setRejectionReason(null);
        
        // **AUTO ADD SELLER ROLE**
        User seller = shop.getUser();
        Role sellerRole = roleRepository.findByName("SELLER")
                .orElseThrow(() -> new ResourceNotFoundException("SELLER role not found"));
        
        if (!seller.getRoles().contains(sellerRole)) {
            seller.getRoles().add(sellerRole);
            userRepository.save(seller);
            log.info("Added SELLER role to user: {}", seller.getEmail());
        }
        
        Shop savedShop = shopRepository.save(shop);
        
        log.info("Shop approved successfully: {}", shopId);
        return convertToShopAdminResponse(savedShop);
    }

    @Override
    @Transactional
    public ShopAdminResponse rejectShop(Long shopId, ShopApprovalRequest rejectionRequest, CustomUserDetails adminDetails) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopId));
        
        shop.setIsActive(false);
        shop.setApprovedAt(null);
        shop.setRejectionReason(rejectionRequest.getRejectionReason());
        
        Shop savedShop = shopRepository.save(shop);
        return convertToShopAdminResponse(savedShop);
    }

    @Override
    @Transactional
    public ShopAdminResponse suspendShop(Long shopId, ShopApprovalRequest suspensionRequest, CustomUserDetails adminDetails) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopId));
        
        shop.setIsActive(false);
        shop.setRejectionReason(suspensionRequest.getRejectionReason());
        
        Shop savedShop = shopRepository.save(shop);
        return convertToShopAdminResponse(savedShop);
    }

    @Override
    @Transactional
    public ShopAdminResponse reactivateShop(Long shopId, CustomUserDetails adminDetails) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopId));
        
        shop.setIsActive(true);
        shop.setRejectionReason(null);
        if (shop.getApprovedAt() == null) {
            shop.setApprovedAt(OffsetDateTime.now());
        }
        
        Shop savedShop = shopRepository.save(shop);
        return convertToShopAdminResponse(savedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getSellerShop(CustomUserDetails sellerDetails) {
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));
        
        if (!shop.getIsActive()) {
            throw new IllegalStateException("Shop is not active");
        }
        
        return convertToShopResponse(shop);
    }

    @Override
    @Transactional
    public ShopResponse updateSellerShop(ShopCreateRequest updateRequest, CustomUserDetails sellerDetails) {
        Shop shop = shopRepository.findByUser(sellerDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Seller has no shop"));
        
        if (!shop.getIsActive()) {
            throw new IllegalStateException("Shop is not active");
        }
        
        shop.setDescription(updateRequest.getDescription());
        shop.setContactEmail(updateRequest.getContactEmail());
        shop.setContactPhone(updateRequest.getContactPhone());
        
        Shop savedShop = shopRepository.save(shop);
        return convertToShopResponse(savedShop);
    }

    // Helper methods
    private String generateSlug(String name) {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private Boolean parseStatusFilter(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        
        return switch (status.toLowerCase()) {
            case "pending" -> false;
            case "approved" -> true;
            case "rejected" -> false;
            default -> null;
        };
    }

    private ShopResponse convertToShopResponse(Shop shop) {
        ShopResponse response = modelMapper.map(shop, ShopResponse.class);
        
        if (shop.getLogoCloudinaryPublicId() != null) {
            response.setLogoUrl(cloudinaryService.getImageUrl(shop.getLogoCloudinaryPublicId()));
        }
        
        if (shop.getCoverImageCloudinaryPublicId() != null) {
            response.setCoverImageUrl(cloudinaryService.getImageUrl(shop.getCoverImageCloudinaryPublicId()));
        }
        
        if (shop.getAddress() != null) {
            response.setAddressLine1(shop.getAddress().getAddressLine1());
            response.setAddressLine2(shop.getAddress().getAddressLine2());
            response.setCity(shop.getAddress().getCity());
            response.setState(shop.getAddress().getState());
            response.setCountry(shop.getAddress().getCountry());
            response.setPostalCode(shop.getAddress().getPostalCode());
        }
        
        response.setTotalProducts(shopRepository.countProductsByShopId(shop.getId()));
        
        return response;
    }

    private ShopAdminResponse convertToShopAdminResponse(Shop shop) {
        ShopAdminResponse response = modelMapper.map(shop, ShopAdminResponse.class);
        
        if (shop.getLogoCloudinaryPublicId() != null) {
            response.setLogoUrl(cloudinaryService.getImageUrl(shop.getLogoCloudinaryPublicId()));
        }
        
        if (shop.getCoverImageCloudinaryPublicId() != null) {
            response.setCoverImageUrl(cloudinaryService.getImageUrl(shop.getCoverImageCloudinaryPublicId()));
        }
        
        if (shop.getUser() != null) {
            response.setSellerId(shop.getUser().getId());
            response.setSellerEmail(shop.getUser().getEmail());
            response.setSellerFullName(shop.getUser().getFullName());
            response.setSellerPhone(shop.getUser().getPhoneNumber());
        }
        
        if (shop.getAddress() != null) {
            response.setAddressLine1(shop.getAddress().getAddressLine1());
            response.setAddressLine2(shop.getAddress().getAddressLine2());
            response.setCity(shop.getAddress().getCity());
            response.setState(shop.getAddress().getState());
            response.setCountry(shop.getAddress().getCountry());
            response.setPostalCode(shop.getAddress().getPostalCode());
        }
        
        response.setTotalProducts(shopRepository.countProductsByShopId(shop.getId()));
        response.setApprovedProducts(shopRepository.countApprovedProductsByShopId(shop.getId()));
        response.setPendingProducts(shopRepository.countPendingProductsByShopId(shop.getId()));
        
        return response;
    }
} 