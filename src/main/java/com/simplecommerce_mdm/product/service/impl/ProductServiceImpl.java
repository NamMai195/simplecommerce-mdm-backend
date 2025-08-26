package com.simplecommerce_mdm.product.service.impl;

import com.simplecommerce_mdm.category.model.Category;
import com.simplecommerce_mdm.category.repository.CategoryRepository;
import com.simplecommerce_mdm.cloudinary.service.CloudinaryService;
import com.simplecommerce_mdm.common.enums.ImageTargetType;
import com.simplecommerce_mdm.common.enums.ProductStatus;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.exception.InvalidDataException;
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
import com.simplecommerce_mdm.product.dto.ProductVariantUpdateRequest;
import com.simplecommerce_mdm.product.model.Product;
import com.simplecommerce_mdm.product.model.ProductImage;
import com.simplecommerce_mdm.product.model.ProductVariant;
import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.product.repository.ProductImageRepository;
import com.simplecommerce_mdm.product.repository.ProductRepository;
import com.simplecommerce_mdm.product.repository.ShopRepository;
import com.simplecommerce_mdm.product.service.ProductService;
import com.simplecommerce_mdm.user.model.User;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Arrays;
import com.simplecommerce_mdm.product.repository.ProductVariantRepository;
import com.simplecommerce_mdm.product.dto.ProductImageListResponse;
import com.simplecommerce_mdm.product.dto.ProductImageItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest productRequest, List<MultipartFile> images, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        log.info("Starting product creation process for seller: {}", seller.getEmail());

        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for the current seller."));
        
        // Validate shop status before allowing product creation
        validateShopStatusForProductCreation(shop);
        
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productRequest.getCategoryId()));

        Product product = modelMapper.map(productRequest, Product.class);
        product.setShop(shop);
        product.setCategory(category);
        product.setStatus(ProductStatus.PENDING_APPROVAL);
        product.setSlug(generateSlug(productRequest.getName()));
        
        Set<ProductVariant> variants = productRequest.getVariants().stream()
                .map(variantRequest -> {
                    ProductVariant variant = modelMapper.map(variantRequest, ProductVariant.class);
                    variant.setProduct(product);
                    return variant;
                })
                .collect(Collectors.toSet());
        product.setVariants(variants);
        
        Product savedProduct = productRepository.save(product);
        log.info("Saved new product with ID: {}", savedProduct.getId());
        
        List<ProductImage> productImages = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            final String productIdStr = savedProduct.getId().toString();
            images.forEach(imageFile -> {
                CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadProductImage(imageFile, productIdStr);
                log.info("Uploaded image to Cloudinary with public_id: {}", uploadResult.publicId());
                imageUrls.add(uploadResult.url());

                ProductImage productImage = ProductImage.builder()
                        .targetId(savedProduct.getId())
                        .targetType(ImageTargetType.PRODUCT)
                        .cloudinaryPublicId(uploadResult.publicId())
                        .build();
                productImages.add(productImage);
            });
            productImageRepository.saveAll(productImages);
            log.info("Saved {} product images to the database.", productImages.size());
        }

        ProductResponse response = modelMapper.map(savedProduct, ProductResponse.class);
        response.setImageUrls(imageUrls);
        response.setCategoryId(savedProduct.getCategory().getId());
        response.setShopId(savedProduct.getShop().getId());
        
        // Explicitly set timestamps to ensure they are not null
        response.setCreatedAt(savedProduct.getCreatedAt());
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListResponse getSellerProducts(ProductSearchRequest searchRequest, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        log.info("Getting products for seller: {}", seller.getEmail());

        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for the current seller."));

        // Create Pageable object with sorting
        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(searchRequest.getSortDirection()) 
                    ? Sort.Direction.ASC 
                    : Sort.Direction.DESC,
                searchRequest.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(), 
                searchRequest.getSize(), 
                sort
        );

        // Query products based on search criteria
        Page<Product> productPage;
        
        if (StringUtils.hasText(searchRequest.getSearchTerm()) && searchRequest.getStatus() != null) {
            // Both search term and status filter
            productPage = productRepository.findByShopAndStatusAndNameContaining(
                    shop, searchRequest.getStatus(), searchRequest.getSearchTerm(), pageable);
        } else if (StringUtils.hasText(searchRequest.getSearchTerm())) {
            // Only search term
            productPage = productRepository.findByShopAndNameContaining(
                    shop, searchRequest.getSearchTerm(), pageable);
        } else if (searchRequest.getStatus() != null) {
            // Only status filter
            productPage = productRepository.findByShopAndStatus(shop, searchRequest.getStatus(), pageable);
        } else {
            // No filters
            productPage = productRepository.findByShop(shop, pageable);
        }

        // Convert to ProductResponse DTOs
        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());

        // Build response with pagination metadata
        return ProductListResponse.builder()
                .products(productResponses)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .pageSize(productPage.getSize())
                .hasNext(productPage.hasNext())
                .hasPrevious(productPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getSellerProductById(Long productId, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        log.info("Getting product {} for seller: {}", productId, seller.getEmail());

        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for the current seller."));

        // Find product by ID and shop (authorization check)
        Product product = productRepository.findByIdAndShop(productId, shop)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId + " or you don't have permission to access it."));

        log.info("Found product: {} for seller: {}", product.getName(), seller.getEmail());
        
        ProductResponse response = convertToProductResponse(product);
        // Explicitly set timestamps to ensure they are not null
        response.setCreatedAt(product.getCreatedAt());
        return response;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest updateRequest, List<MultipartFile> newImages, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        log.info("Updating product {} for seller: {}", productId, seller.getEmail());

        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for the current seller."));

        // Validate shop status for product updates (less strict than creation)
        validateShopStatusForProductUpdate(shop);

        // Find product by ID and shop (authorization check)
        Product existingProduct = productRepository.findByIdAndShop(productId, shop)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId + " or you don't have permission to access it."));

        // Check if category exists
        Category category = categoryRepository.findById(updateRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + updateRequest.getCategoryId()));

        // Check if significant changes require approval reset
        boolean needsReapproval = checkIfNeedsReapproval(existingProduct, updateRequest);

        // Update basic product fields
        existingProduct.setName(updateRequest.getName());
        existingProduct.setDescription(updateRequest.getDescription());
        existingProduct.setBasePrice(updateRequest.getBasePrice());
        existingProduct.setCategory(category);
        existingProduct.setSlug(generateSlug(updateRequest.getName()));
        
        // Update additional fields if provided
        if (updateRequest.getAttributes() != null) {
            existingProduct.setAttributes(updateRequest.getAttributes());
        }
        if (updateRequest.getWeightGrams() != null) {
            existingProduct.setWeightGrams(updateRequest.getWeightGrams());
        }
        if (updateRequest.getDimensions() != null) {
            existingProduct.setDimensions(updateRequest.getDimensions());
        }

        // Reset status if needed
        if (needsReapproval && existingProduct.getStatus() == ProductStatus.APPROVED) {
            existingProduct.setStatus(ProductStatus.PENDING_APPROVAL);
            existingProduct.setApprovedAt(null);
            log.info("Product status reset to PENDING_APPROVAL due to significant changes");
        }

        // Update variants
        updateProductVariants(existingProduct, updateRequest.getVariants());

        // Save product
        Product savedProduct = productRepository.save(existingProduct);
        log.info("Updated product with ID: {}", savedProduct.getId());

        // Handle new images if provided
        if (newImages != null && !newImages.isEmpty()) {
            addNewProductImages(savedProduct, newImages);
        }

        ProductResponse response = convertToProductResponse(savedProduct);
        // Explicitly set timestamps to ensure they are not null
        response.setCreatedAt(savedProduct.getCreatedAt());
        return response;
    }

    private boolean checkIfNeedsReapproval(Product existingProduct, ProductUpdateRequest updateRequest) {
        // Check for significant changes that require re-approval
        return !existingProduct.getName().equals(updateRequest.getName()) ||
               !existingProduct.getBasePrice().equals(updateRequest.getBasePrice()) ||
               !existingProduct.getCategory().getId().equals(updateRequest.getCategoryId());
    }

    private void updateProductVariants(Product product, List<ProductVariantUpdateRequest> variantRequests) {
        Set<ProductVariant> existingVariants = product.getVariants();
        Set<ProductVariant> updatedVariants = new HashSet<>();

        // Process each variant request
        for (ProductVariantUpdateRequest variantRequest : variantRequests) {
            ProductVariant variant;
            
            if (variantRequest.getId() != null) {
                // Update existing variant
                variant = existingVariants.stream()
                        .filter(v -> v.getId().equals(variantRequest.getId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantRequest.getId()));
                
                // Update variant fields
                variant.setSku(variantRequest.getSku());
                variant.setFinalPrice(variantRequest.getFinalPrice());
                variant.setCompareAtPrice(variantRequest.getCompareAtPrice());
                variant.setStockQuantity(variantRequest.getStockQuantity());
                variant.setOptions(variantRequest.getOptions() != null ? variantRequest.getOptions() : "{}");
                
                if (variantRequest.getReorderThreshold() != null) {
                    variant.setReorderThreshold(variantRequest.getReorderThreshold());
                }
                if (variantRequest.getWeightGrams() != null) {
                    variant.setWeightGrams(variantRequest.getWeightGrams());
                }
                if (variantRequest.getDimensions() != null) {
                    variant.setDimensions(variantRequest.getDimensions());
                }
                if (variantRequest.getIsActive() != null) {
                    variant.setIsActive(variantRequest.getIsActive());
                }
            } else {
                // Create new variant
                variant = ProductVariant.builder()
                        .product(product)
                        .sku(variantRequest.getSku())
                        .finalPrice(variantRequest.getFinalPrice())
                        .compareAtPrice(variantRequest.getCompareAtPrice())
                        .stockQuantity(variantRequest.getStockQuantity())
                        .options(variantRequest.getOptions() != null ? variantRequest.getOptions() : "{}")
                        .reorderThreshold(variantRequest.getReorderThreshold() != null ? variantRequest.getReorderThreshold() : 0)
                        .weightGrams(variantRequest.getWeightGrams())
                        .dimensions(variantRequest.getDimensions())
                        .isActive(variantRequest.getIsActive() != null ? variantRequest.getIsActive() : true)
                        .build();
            }
            
            updatedVariants.add(variant);
        }

        // Replace old variants with updated ones
        // Note: JPA will handle deletion of removed variants due to cascade settings
        product.getVariants().clear();
        product.getVariants().addAll(updatedVariants);

        log.info("Updated {} variants for product {}", updatedVariants.size(), product.getName());
    }

    private void addNewProductImages(Product product, List<MultipartFile> newImages) {
        List<ProductImage> productImages = new ArrayList<>();
        
        final String productIdStr = product.getId().toString();
        newImages.forEach(imageFile -> {
            CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadProductImage(imageFile, productIdStr);
            log.info("Uploaded new image to Cloudinary with public_id: {}", uploadResult.publicId());

            ProductImage productImage = ProductImage.builder()
                    .targetId(product.getId())
                    .targetType(ImageTargetType.PRODUCT)
                    .cloudinaryPublicId(uploadResult.publicId())
                    .build();
            productImages.add(productImage);
        });
        
        productImageRepository.saveAll(productImages);
        log.info("Added {} new images to product {}", productImages.size(), product.getName());
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        log.info("Deleting product {} for seller: {}", productId, seller.getEmail());

        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for the current seller."));

        // Validate shop status for product deletion (less strict than creation)
        validateShopStatusForProductUpdate(shop);

        // Find product by ID and shop (authorization check)
        Product product = productRepository.findByIdAndShop(productId, shop)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId + " or you don't have permission to access it."));

        // Optional: Delete images from Cloudinary (uncomment if needed)
        // cleanupProductImages(product);

        // Use custom soft delete method to avoid @SQLDelete parameter binding issues
        productRepository.softDeleteProductByIdAndShop(productId, shop);
        
        log.info("Successfully deleted product: {} (ID: {}) for seller: {}", 
                product.getName(), productId, seller.getEmail());
    }

    @Override
    @Transactional
    public void deleteVariant(Long productId, Long variantId, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        log.info("Deleting variant {} from product {} for seller: {}", variantId, productId, seller.getEmail());

        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for the current seller."));

        // Find product by ID and shop (authorization check)
        Product product = productRepository.findByIdAndShop(productId, shop)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId + " or you don't have permission to access it."));

        // Find variant by ID
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variant not found with id: " + variantId + " in product " + productId));

        // Check if this is the only variant (product must have at least one variant)
        if (product.getVariants().size() <= 1) {
            throw new InvalidDataException("Cannot delete the only variant. Product must have at least one variant.");
        }

        // Clear the bidirectional relationship
        variant.setProduct(null);
        
        // Remove variant from product's variants collection
        // With CascadeType.REMOVE and orphanRemoval = true, this will automatically trigger deletion
        product.getVariants().remove(variant);
        
        // Save the updated product - JPA will handle variant deletion due to cascade and orphanRemoval
        productRepository.save(product);
        
        log.info("Successfully deleted variant: {} (ID: {}) from product: {} (ID: {}) for seller: {}", 
                variant.getSku(), variantId, product.getName(), productId, seller.getEmail());
    }

    @Override
    @Transactional
    public void deleteProductImages(Long productId, List<Long> imageIds, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        log.info("Deleting {} images from product {} for seller: {}", imageIds.size(), productId, seller.getEmail());

        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for the current seller."));

        // Auth check: product belongs to seller's shop
        Product product = productRepository.findByIdAndShop(productId, shop)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId + " or you don't have permission to access it."));

        // Load images by ids and product
        List<ProductImage> images = productImageRepository.findByIdInAndTargetIdAndTargetType(
                imageIds, product.getId(), ImageTargetType.PRODUCT);
        if (images.isEmpty()) {
            throw new ResourceNotFoundException("No product images found for given IDs");
        }

        // Delete from Cloudinary then DB
        for (ProductImage img : images) {
            try {
                cloudinaryService.deleteFile(img.getCloudinaryPublicId());
            } catch (Exception e) {
                log.warn("Failed to delete Cloudinary image {}: {}", img.getCloudinaryPublicId(), e.getMessage());
            }
        }
        productImageRepository.deleteAll(images);
        log.info("Deleted {} images from product {}", images.size(), product.getName());
    }

    // Optional method to cleanup Cloudinary images when deleting product
    private void cleanupProductImages(Product product) {
        List<ProductImage> productImages = productImageRepository.findByTargetIdAndTargetType(
                product.getId(), ImageTargetType.PRODUCT);
        
        for (ProductImage productImage : productImages) {
            try {
                cloudinaryService.deleteFile(productImage.getCloudinaryPublicId());
                log.info("Deleted image from Cloudinary: {}", productImage.getCloudinaryPublicId());
            } catch (Exception e) {
                log.warn("Failed to delete image from Cloudinary: {}, Error: {}", 
                        productImage.getCloudinaryPublicId(), e.getMessage());
            }
        }
    }

    private ProductResponse convertToProductResponse(Product product) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        response.setCategoryId(product.getCategory().getId());
        response.setShopId(product.getShop().getId());
        
        // Manually map variants to ensure proper ID mapping
        List<ProductResponse.ProductVariantResponse> variantResponses = product.getVariants().stream()
                .map(variant -> {
                    ProductResponse.ProductVariantResponse variantResponse = new ProductResponse.ProductVariantResponse();
                    variantResponse.setId(variant.getId());
                    variantResponse.setSku(variant.getSku());
                    variantResponse.setFinalPrice(variant.getFinalPrice());
                    variantResponse.setCompareAtPrice(variant.getCompareAtPrice());
                    variantResponse.setStockQuantity(variant.getStockQuantity());
                    variantResponse.setOptions(variant.getOptions());
                    variantResponse.setIsActive(variant.getIsActive());
                    return variantResponse;
                })
                .collect(Collectors.toList());
        response.setVariants(variantResponses);
        
        // Get image URLs for this product
        List<ProductImage> productImages = productImageRepository.findByTargetIdAndTargetType(
                product.getId(), ImageTargetType.PRODUCT);
        List<String> imageUrls = productImages.stream()
                .map(img -> cloudinaryService.getImageUrl(img.getCloudinaryPublicId()))
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);
        
        return response;
    }

    private String generateSlug(String input) {
        if (input == null) {
            return "";
        }
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
    
    /**
     * Validates shop status for product creation
     * Only allows product creation for ACTIVE and APPROVED shops
     */
    private void validateShopStatusForProductCreation(Shop shop) {
        if (!shop.getIsActive()) {
            throw new IllegalStateException("Cannot create new products. Shop is currently inactive or suspended.");
        }
        
        if (shop.getApprovedAt() == null) {
            throw new IllegalStateException("Cannot create new products. Shop is not yet approved by admin.");
        }
        
        log.info("Shop status validation passed for shop: {} (ID: {})", shop.getName(), shop.getId());
    }
    
    /**
     * Validates shop status for product updates
     * Allows updates for ACTIVE shops (even if not approved yet)
     * But blocks updates for SUSPENDED shops
     */
    private void validateShopStatusForProductUpdate(Shop shop) {
        if (!shop.getIsActive()) {
            throw new IllegalStateException("Cannot update products. Shop is currently suspended or inactive.");
        }
        
        log.info("Shop status validation for updates passed for shop: {} (ID: {})", shop.getName(), shop.getId());
    }

    // ============================ ADMIN METHODS ============================

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAdminResponse> getProductsForAdmin(ProductAdminSearchRequest searchRequest) {
        log.info("Getting products for admin with filters: status={}, shopId={}, category={}, search='{}'", 
                searchRequest.getStatus(), searchRequest.getShopId(), searchRequest.getCategoryId(), searchRequest.getSearchTerm());

        // Create Pageable object with sorting
        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(searchRequest.getSortDirection()) 
                    ? Sort.Direction.ASC 
                    : Sort.Direction.DESC,
                searchRequest.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(), 
                searchRequest.getSize(), 
                sort
        );

        Page<Product> productPage = productRepository.findProductsForAdmin(
                searchRequest.getStatus(),
                searchRequest.getShopId(),
                searchRequest.getCategoryId(),
                searchRequest.getSearchTerm(),
                pageable
        );

        // Filter by seller email if provided (post-query filtering)
        if (searchRequest.getSellerEmail() != null && !searchRequest.getSellerEmail().trim().isEmpty()) {
            List<Product> filteredProducts = productPage.getContent().stream()
                    .filter(product -> product.getShop() != null && 
                                    product.getShop().getUser() != null &&
                                    product.getShop().getUser().getEmail() != null &&
                                    product.getShop().getUser().getEmail().toLowerCase()
                                            .contains(searchRequest.getSellerEmail().toLowerCase()))
                    .collect(Collectors.toList());
            
            // Create new Page with filtered content
            Pageable filteredPageable = PageRequest.of(0, filteredProducts.size(), sort);
            productPage = new PageImpl<>(filteredProducts, filteredPageable, filteredProducts.size());
        }

        return productPage.map(this::convertToProductAdminResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAdminResponse getProductByIdForAdmin(Long productId) {
        log.info("Getting product {} for admin", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return convertToProductAdminResponse(product);
    }

    @Override
    @Transactional
    public ProductAdminResponse approveProduct(Long productId, CustomUserDetails adminDetails) {
        User admin = adminDetails.getUser();
        log.info("Admin {} approving product {}", admin.getEmail(), productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStatus() != ProductStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Product is not in PENDING_APPROVAL status. Current status: " + product.getStatus());
        }

        product.setStatus(ProductStatus.APPROVED);
        product.setApprovedAt(OffsetDateTime.now());
        product.setRejectionReason(null); // Clear any previous rejection reason
        
        Product savedProduct = productRepository.save(product);
        log.info("Product {} approved successfully by admin {}", productId, admin.getEmail());

        return convertToProductAdminResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductAdminResponse rejectProduct(Long productId, ProductApprovalRequest rejectionRequest, CustomUserDetails adminDetails) {
        User admin = adminDetails.getUser();
        log.info("Admin {} rejecting product {} with reason: {}", admin.getEmail(), productId, rejectionRequest.getRejectionReason());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStatus() != ProductStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Product is not in PENDING_APPROVAL status. Current status: " + product.getStatus());
        }

        product.setStatus(ProductStatus.REJECTED);
        product.setRejectionReason(rejectionRequest.getRejectionReason());
        product.setApprovedAt(null); // Clear any previous approval date
        
        Product savedProduct = productRepository.save(product);
        log.info("Product {} rejected successfully by admin {}", productId, admin.getEmail());

        return convertToProductAdminResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAdminResponse getProductDetailsForAdmin(Long productId) {
        return getProductByIdForAdmin(productId);
    }

    private ProductAdminResponse convertToProductAdminResponse(Product product) {
        ProductAdminResponse response = modelMapper.map(product, ProductAdminResponse.class);
        
        // Set basic IDs
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setShopId(product.getShop().getId());
        response.setShopName(product.getShop().getName());
        response.setSellerEmail(product.getShop().getUser().getEmail());
        
        // Map variants with more details for admin
        List<ProductAdminResponse.ProductVariantAdminResponse> variantResponses = product.getVariants().stream()
                .map(variant -> {
                    ProductAdminResponse.ProductVariantAdminResponse variantResponse = 
                            modelMapper.map(variant, ProductAdminResponse.ProductVariantAdminResponse.class);
                    return variantResponse;
                })
                .collect(Collectors.toList());
        response.setVariants(variantResponses);
        
        // Get image URLs for this product
        List<ProductImage> productImages = productImageRepository.findByTargetIdAndTargetType(
                product.getId(), ImageTargetType.PRODUCT);
        List<String> imageUrls = productImages.stream()
                .map(img -> cloudinaryService.getImageUrl(img.getCloudinaryPublicId()))
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);
        
        return response;
    }

    // ===== BUYER/PUBLIC METHODS =====

    @Override
    @Transactional(readOnly = true)
    public ProductBuyerListResponse getFeaturedProducts(Integer page, Integer size, String sortBy, String sortDirection) {
        log.info("Getting featured products: page={}, size={}, sortBy={}, sortDirection={}", 
                 page, size, sortBy, sortDirection);

        // Create pageable
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get featured products
        Page<Product> productPage = productRepository.findFeaturedProducts(pageable);

        // Convert to buyer response
        List<ProductBuyerResponse> buyerProducts = productPage.getContent().stream()
                .map(this::convertToProductBuyerResponse)
                .collect(Collectors.toList());

        // Build response
        ProductBuyerListResponse response = new ProductBuyerListResponse();
        response.setProducts(buyerProducts);
        response.setCurrentPage(productPage.getNumber());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalElements(productPage.getTotalElements());
        response.setPageSize(productPage.getSize());
        response.setHasNext(productPage.hasNext());
        response.setHasPrevious(productPage.hasPrevious());

        log.info("Found {} featured products", buyerProducts.size());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBuyerListResponse getProducts(String searchTerm, Integer page, Integer size, String sortBy, String sortDirection) {
        log.info("Getting products: searchTerm='{}', page={}, size={}, sortBy={}, sortDirection={}", 
                 searchTerm, page, size, sortBy, sortDirection);

        // Create pageable
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get approved products with search
        Page<Product> productPage = productRepository.findApprovedProducts(searchTerm, pageable);

        // Convert to buyer response
        List<ProductBuyerResponse> buyerProducts = productPage.getContent().stream()
                .map(this::convertToProductBuyerResponse)
                .collect(Collectors.toList());

        // Build response
        ProductBuyerListResponse response = new ProductBuyerListResponse();
        response.setProducts(buyerProducts);
        response.setCurrentPage(productPage.getNumber());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalElements(productPage.getTotalElements());
        response.setPageSize(productPage.getSize());
        response.setHasNext(productPage.hasNext());
        response.setHasPrevious(productPage.hasPrevious());

        log.info("Found {} products", buyerProducts.size());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBuyerResponse getProductById(Long productId) {
        log.info("Getting approved product by ID: {}", productId);

        Product product = productRepository.findApprovedProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found or not approved: " + productId));

        return convertToProductBuyerResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id, CustomUserDetails sellerDetails) {
        return getSellerProductById(id, sellerDetails);
    }

    // Helper method to convert Product to ProductBuyerResponse
    private ProductBuyerResponse convertToProductBuyerResponse(Product product) {
        ProductBuyerResponse response = modelMapper.map(product, ProductBuyerResponse.class);
        
        // Set category info
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        
        // Set shop info
        if (product.getShop() != null) {
            response.setShopId(product.getShop().getId());
            response.setShopName(product.getShop().getName());
            response.setShopSlug(product.getShop().getSlug());
            response.setShopRating(product.getShop().getRating());
        }

        // Map variants to buyer response
        List<ProductBuyerResponse.ProductVariantBuyerResponse> variantResponses = product.getVariants().stream()
                .filter(variant -> variant.getIsActive()) // Only show active variants
                .map(variant -> {
                    ProductBuyerResponse.ProductVariantBuyerResponse variantResponse = 
                        new ProductBuyerResponse.ProductVariantBuyerResponse();
                    variantResponse.setId(variant.getId());  // Add missing variant ID
                    variantResponse.setSku(variant.getSku());
                    variantResponse.setFinalPrice(variant.getFinalPrice());
                    variantResponse.setCompareAtPrice(variant.getCompareAtPrice());
                    variantResponse.setStockQuantity(variant.getStockQuantity());
                    variantResponse.setOptions(variant.getOptions());
                    variantResponse.setIsActive(variant.getIsActive());
                    return variantResponse;
                })
                .collect(Collectors.toList());
        response.setVariants(variantResponses);

        // Get and set image URLs
        List<ProductImage> images = productImageRepository.findByTargetIdAndTargetType(
                product.getId(), ImageTargetType.PRODUCT);
        List<String> imageUrls = images.stream()
                .map(img -> cloudinaryService.getImageUrl(img.getCloudinaryPublicId()))
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);
        
        return response;
    }

    // ===== ADDITIONAL BUYER METHODS =====

    @Override
    @Transactional(readOnly = true)
    public ProductBuyerListResponse getProductsByCategory(Integer categoryId, String searchTerm, Integer page, Integer size, String sortBy, String sortDirection) {
        log.info("Getting products by category: categoryId={}, searchTerm='{}', page={}, size={}", 
                 categoryId, searchTerm, page, size);

        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findApprovedProductsByCategory(categoryId, searchTerm, pageable);

        return buildProductBuyerListResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBuyerListResponse getProductsByShop(Long shopId, String searchTerm, Integer page, Integer size, String sortBy, String sortDirection) {
        log.info("Getting products by shop: shopId={}, searchTerm='{}', page={}, size={}", 
                 shopId, searchTerm, page, size);

        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findApprovedProductsByShop(shopId, searchTerm, pageable);

        return buildProductBuyerListResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBuyerListResponse getLatestProducts(Integer page, Integer size, String sortBy, String sortDirection) {
        log.info("Getting latest products: page={}, size={}, sortBy={}, sortDirection={}", 
                 page, size, sortBy, sortDirection);

        // For latest products, we always sort by createdAt DESC regardless of params
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Product> productPage = productRepository.findLatestApprovedProducts(pageable);

        return buildProductBuyerListResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBuyerListResponse getProductsByPriceRange(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, String searchTerm, Integer page, Integer size, String sortBy, String sortDirection) {
        log.info("Getting products by price range: minPrice={}, maxPrice={}, searchTerm='{}', page={}, size={}", 
                 minPrice, maxPrice, searchTerm, page, size);

        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findApprovedProductsByPriceRange(minPrice, maxPrice, searchTerm, pageable);

        return buildProductBuyerListResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBuyerListResponse getProductsWithFilters(ProductFilterRequest filterRequest) {
        log.info("Getting products with filters: {}", filterRequest);

        // Create sort object
        Sort sort = filterRequest.getSortDirection().equalsIgnoreCase("desc") 
                ? Sort.by(filterRequest.getSortBy()).descending() 
                : Sort.by(filterRequest.getSortBy()).ascending();

        // Get all filtered products
        List<Product> allProducts = productRepository.findApprovedProductsWithFiltersNative(
                filterRequest.getCategoryId(),
                filterRequest.getShopId(),
                filterRequest.getMinPrice(),
                filterRequest.getMaxPrice(),
                filterRequest.getSearchTerm()
        );

        // Apply sorting manually
        allProducts.sort((p1, p2) -> {
            int result = 0;
            switch (filterRequest.getSortBy().toLowerCase()) {
                case "createdat":
                    result = p1.getCreatedAt().compareTo(p2.getCreatedAt());
                    break;
                case "name":
                    result = p1.getName().compareTo(p2.getName());
                    break;
                case "baseprice":
                    result = p1.getBasePrice().compareTo(p2.getBasePrice());
                    break;
                case "sku":
                    result = p1.getSku().compareTo(p2.getSku());
                    break;
                default:
                    result = p1.getCreatedAt().compareTo(p2.getCreatedAt());
            }
            return filterRequest.getSortDirection().equalsIgnoreCase("desc") ? -result : result;
        });

        // Apply pagination manually
        int totalElements = allProducts.size();
        int pageSize = filterRequest.getSize();
        int currentPage = filterRequest.getPage();
        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalElements);

        List<Product> pageContent = allProducts.subList(startIndex, endIndex);

        // Convert to DTOs
        List<ProductBuyerResponse> productResponses = pageContent.stream()
                .map(this::convertToProductBuyerResponse)
                .collect(Collectors.toList());

        // Create response
        ProductBuyerListResponse response = new ProductBuyerListResponse();
        response.setProducts(productResponses);
        response.setTotalElements((long) totalElements);
        response.setTotalPages((int) Math.ceil((double) totalElements / pageSize));
        response.setCurrentPage(currentPage);
        response.setPageSize(pageSize);
        response.setHasNext(currentPage < response.getTotalPages() - 1);
        response.setHasPrevious(currentPage > 0);

        log.info("Returning {} products out of {} total", productResponses.size(), totalElements);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBuyerListResponse getRelatedProducts(Long productId, Integer page, Integer size) {
        log.info("Getting related products for productId: {}, page={}, size={}", productId, page, size);

        // First get the product to find its category
        Product product = productRepository.findApprovedProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (product.getCategory() == null) {
            log.warn("Product {} has no category, returning empty result", productId);
            return new ProductBuyerListResponse();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Product> productPage = productRepository.findRelatedProducts(
                product.getCategory().getId(), productId, pageable);

        return buildProductBuyerListResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PriceRangeResponse getPriceRange() {
        log.info("Getting price range for all approved products");

        // Use separate methods for better reliability
        BigDecimal minPrice = productRepository.findMinPriceNative();
        BigDecimal maxPrice = productRepository.findMaxPriceNative();
        
        log.info("Min price from repository: {}", minPrice);
        log.info("Max price from repository: {}", maxPrice);
        
        PriceRangeResponse response = new PriceRangeResponse();
        
        // Check if prices are valid (not null and > 0)
        if (minPrice != null && maxPrice != null && minPrice.compareTo(BigDecimal.ZERO) > 0) {
            response.setMinPrice(minPrice);
            response.setMaxPrice(maxPrice);
            log.info("Setting valid prices: min={}, max={}", minPrice, maxPrice);
        } else {
            log.warn("Invalid prices found: minPrice={}, maxPrice={}", minPrice, maxPrice);
            response.setMinPrice(BigDecimal.ZERO);
            response.setMaxPrice(BigDecimal.ZERO);
        }

        log.info("Final price range response: min={}, max={}", response.getMinPrice(), response.getMaxPrice());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImageListResponse listProductImages(Long productId, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for the current seller."));

        Product product = productRepository.findByIdAndShop(productId, shop)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId + " or you don't have permission to access it."));

        List<ProductImage> images = productImageRepository.findByTargetIdAndTargetType(product.getId(), ImageTargetType.PRODUCT);
        List<ProductImageItem> items = images.stream().map(img -> ProductImageItem.builder()
                        .id(img.getId())
                        .publicId(img.getCloudinaryPublicId())
                        .url(cloudinaryService.getImageUrl(img.getCloudinaryPublicId()))
                        .isPrimary(img.getIsPrimary())
                        .sortOrder(img.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return ProductImageListResponse.builder()
                .productId(product.getId())
                .count(items.size())
                .images(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListResponse getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        List<ProductResponse> products = page.getContent().stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
        ProductListResponse resp = new ProductListResponse();
        resp.setProducts(products);
        resp.setCurrentPage(page.getNumber());
        resp.setTotalPages(page.getTotalPages());
        resp.setTotalElements(page.getTotalElements());
        resp.setPageSize(page.getSize());
        resp.setHasNext(page.hasNext());
        resp.setHasPrevious(page.hasPrevious());
        return resp;
    }

    // Helper method to build ProductBuyerListResponse from Page<Product>
    private ProductBuyerListResponse buildProductBuyerListResponse(Page<Product> productPage) {
        List<ProductBuyerResponse> buyerProducts = productPage.getContent().stream()
                .map(this::convertToProductBuyerResponse)
                .collect(Collectors.toList());

        ProductBuyerListResponse response = new ProductBuyerListResponse();
        response.setProducts(buyerProducts);
        response.setCurrentPage(productPage.getNumber());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalElements(productPage.getTotalElements());
        response.setPageSize(productPage.getSize());
        response.setHasNext(productPage.hasNext());
        response.setHasPrevious(productPage.hasPrevious());

        log.info("Found {} products", buyerProducts.size());
        return response;
    }
}  