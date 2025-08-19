package com.simplecommerce_mdm.shop.service;

import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.shop.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface ShopService {

    // User shop creation (no SELLER role needed)
    ShopResponse createShop(ShopCreateRequest createRequest, CustomUserDetails userDetails);
    
    ShopResponse getUserShop(CustomUserDetails userDetails);
    
    ShopResponse updateShop(ShopCreateRequest updateRequest, CustomUserDetails userDetails);
    
    // Admin shop management
    Page<ShopAdminResponse> getShopsForAdmin(ShopAdminSearchRequest searchRequest);
    
    ShopAdminResponse getShopByIdForAdmin(Long shopId);
    
    ShopAdminResponse approveShop(Long shopId, ShopApprovalRequest approvalRequest, CustomUserDetails adminDetails);
    
    ShopAdminResponse rejectShop(Long shopId, ShopApprovalRequest rejectionRequest, CustomUserDetails adminDetails);
    
    ShopAdminResponse suspendShop(Long shopId, ShopApprovalRequest suspensionRequest, CustomUserDetails adminDetails);
    
    ShopAdminResponse reactivateShop(Long shopId, CustomUserDetails adminDetails);
    
    // Seller shop management (after getting SELLER role)
    ShopResponse getSellerShop(CustomUserDetails sellerDetails);
    
    Object getSellerShopStats(CustomUserDetails sellerDetails);

    // Public shop browsing
    ShopResponse getShopByIdPublic(Long shopId);

    // Public profile (minimal)
    ShopProfileResponse getShopProfile(Long shopId);

    // User updates for media
    ShopResponse updateShopLogo(MultipartFile logoFile, CustomUserDetails userDetails);
    ShopResponse updateShopCover(MultipartFile coverFile, CustomUserDetails userDetails);
} 