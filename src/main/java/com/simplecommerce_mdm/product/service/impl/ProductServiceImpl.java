package com.simplecommerce_mdm.product.service.impl;

import com.simplecommerce_mdm.category.model.Category;
import com.simplecommerce_mdm.category.repository.CategoryRepository;
import com.simplecommerce_mdm.cloudinary.service.CloudinaryService;
import com.simplecommerce_mdm.common.enums.ImageTargetType;
import com.simplecommerce_mdm.common.enums.ProductStatus;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.product.dto.ProductCreateRequest;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest productRequest, List<MultipartFile> images, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        log.info("Starting product creation process for seller: {}", seller.getEmail());

        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for the current seller."));
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
        
        return convertToProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest updateRequest, List<MultipartFile> newImages, CustomUserDetails sellerDetails) {
        User seller = sellerDetails.getUser();
        log.info("Updating product {} for seller: {}", productId, seller.getEmail());

        Shop shop = shopRepository.findByUser(seller)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for the current seller."));

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

        return convertToProductResponse(savedProduct);
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

        // Find product by ID and shop (authorization check)
        Product product = productRepository.findByIdAndShop(productId, shop)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId + " or you don't have permission to access it."));

        // Optional: Delete images from Cloudinary (uncomment if needed)
        // cleanupProductImages(product);

        // Soft delete the product (thanks to @SQLDelete annotation)
        productRepository.delete(product);
        
        log.info("Successfully deleted product: {} (ID: {}) for seller: {}", 
                product.getName(), productId, seller.getEmail());
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
} 