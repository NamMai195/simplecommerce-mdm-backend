package com.simplecommerce_mdm.product.service;

import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.product.dto.ProductAdminResponse;
import com.simplecommerce_mdm.product.dto.ProductAdminSearchRequest;
import com.simplecommerce_mdm.product.dto.ProductApprovalRequest;
import com.simplecommerce_mdm.product.dto.ProductCreateRequest;
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
    
    // Admin methods
    Page<ProductAdminResponse> getProductsForAdmin(ProductAdminSearchRequest searchRequest);
    
    ProductAdminResponse getProductByIdForAdmin(Long productId);
    
    ProductAdminResponse approveProduct(Long productId, CustomUserDetails adminDetails);
    
    ProductAdminResponse rejectProduct(Long productId, ProductApprovalRequest rejectionRequest, CustomUserDetails adminDetails);

} 