package com.simplecommerce_mdm.product.service;

import com.simplecommerce_mdm.product.dto.*;
import com.simplecommerce_mdm.config.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    // ===== SELLER METHODS =====
    ProductResponse createProduct(ProductCreateRequest createRequest, List<MultipartFile> images, CustomUserDetails sellerDetails);
    ProductListResponse getSellerProducts(ProductSearchRequest searchRequest, CustomUserDetails sellerDetails);
    ProductResponse getSellerProductById(Long productId, CustomUserDetails sellerDetails);
    ProductResponse updateProduct(Long productId, ProductUpdateRequest updateRequest, List<MultipartFile> newImages, CustomUserDetails sellerDetails);
    void deleteProduct(Long productId, CustomUserDetails sellerDetails);
    void deleteVariant(Long productId, Long variantId, CustomUserDetails sellerDetails);
    ProductImageListResponse listProductImages(Long productId, CustomUserDetails sellerDetails);
    void deleteProductImages(Long productId, List<Long> imageIds, CustomUserDetails sellerDetails);

    // ===== ADMIN METHODS =====
    Page<ProductAdminResponse> getProductsForAdmin(ProductAdminSearchRequest searchRequest);
    ProductAdminResponse getProductByIdForAdmin(Long productId);
    ProductAdminResponse approveProduct(Long productId, CustomUserDetails adminDetails);
    ProductAdminResponse rejectProduct(Long productId, ProductApprovalRequest rejectionRequest, CustomUserDetails adminDetails);

    // ===== BUYER/PUBLIC METHODS =====
    ProductBuyerListResponse getFeaturedProducts(Integer page, Integer size, String sortBy, String sortDirection);
    ProductBuyerListResponse getProducts(String searchTerm, Integer page, Integer size, String sortBy, String sortDirection);
    ProductBuyerResponse getProductById(Long productId);
    ProductBuyerListResponse getProductsByCategory(Integer categoryId, String searchTerm, Integer page, Integer size, String sortBy, String sortDirection);
    ProductBuyerListResponse getProductsByShop(Long shopId, String searchTerm, Integer page, Integer size, String sortBy, String sortDirection);
    ProductBuyerListResponse getLatestProducts(Integer page, Integer size, String sortBy, String sortDirection);
    ProductBuyerListResponse getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, String searchTerm, Integer page, Integer size, String sortBy, String sortDirection);
    ProductBuyerListResponse getProductsWithFilters(ProductFilterRequest filterRequest);
    ProductBuyerListResponse getRelatedProducts(Long productId, Integer page, Integer size);
    PriceRangeResponse getPriceRange();

    // ===== LEGACY/GENERAL =====
    ProductResponse getProductById(Long id, CustomUserDetails sellerDetails);
    ProductListResponse getAllProducts(Pageable pageable);
    ProductAdminResponse getProductDetailsForAdmin(Long productId);
} 