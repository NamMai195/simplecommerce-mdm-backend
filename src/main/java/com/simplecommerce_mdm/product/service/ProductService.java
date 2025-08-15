package com.simplecommerce_mdm.product.service;

import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.product.dto.PriceRangeResponse;
import com.simplecommerce_mdm.product.dto.ProductAdminResponse;
import com.simplecommerce_mdm.product.dto.ProductAdminSearchRequest;
import com.simplecommerce_mdm.product.dto.ProductApprovalRequest;
import com.simplecommerce_mdm.product.dto.ProductBuyerListResponse;
import com.simplecommerce_mdm.product.dto.ProductBuyerResponse;
import com.simplecommerce_mdm.product.dto.ProductCreateRequest;
import com.simplecommerce_mdm.product.dto.ProductFilterRequest;
import com.simplecommerce_mdm.product.dto.ProductListResponse;
import com.simplecommerce_mdm.product.dto.ProductResponse;
import com.simplecommerce_mdm.product.dto.ProductSearchRequest;
import com.simplecommerce_mdm.product.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest productRequest, List<MultipartFile> images, CustomUserDetails sellerDetails);
    
    ProductListResponse getSellerProducts(ProductSearchRequest searchRequest, CustomUserDetails sellerDetails);
    
    ProductResponse getSellerProductById(Long productId, CustomUserDetails sellerDetails);
    
    ProductResponse updateProduct(Long productId, ProductUpdateRequest updateRequest, List<MultipartFile> newImages, CustomUserDetails sellerDetails);
    
    void deleteProduct(Long productId, CustomUserDetails sellerDetails);
    
    // Variant management
    void deleteVariant(Long productId, Long variantId, CustomUserDetails sellerDetails);
    
    // Admin methods
    Page<ProductAdminResponse> getProductsForAdmin(ProductAdminSearchRequest searchRequest);
    
    ProductAdminResponse getProductByIdForAdmin(Long productId);
    
    ProductAdminResponse approveProduct(Long productId, CustomUserDetails adminDetails);
    
    ProductAdminResponse rejectProduct(Long productId, ProductApprovalRequest rejectionRequest, CustomUserDetails adminDetails);

    // Buyer/Public methods
    ProductBuyerListResponse getFeaturedProducts(Integer page, Integer size, String sortBy, String sortDirection);
    
    ProductBuyerListResponse getProducts(String searchTerm, Integer page, Integer size, String sortBy, String sortDirection);
    
    ProductBuyerResponse getProductById(Long productId);

    // Additional buyer methods
    ProductBuyerListResponse getProductsByCategory(Integer categoryId, String searchTerm, Integer page, Integer size, String sortBy, String sortDirection);
    
    ProductBuyerListResponse getProductsByShop(Long shopId, String searchTerm, Integer page, Integer size, String sortBy, String sortDirection);
    
    ProductBuyerListResponse getLatestProducts(Integer page, Integer size, String sortBy, String sortDirection);
    
    ProductBuyerListResponse getProductsByPriceRange(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, String searchTerm, Integer page, Integer size, String sortBy, String sortDirection);
    
    ProductBuyerListResponse getProductsWithFilters(ProductFilterRequest filterRequest);
    
    ProductBuyerListResponse getRelatedProducts(Long productId, Integer page, Integer size);
    
    PriceRangeResponse getPriceRange();

} 