package com.simplecommerce_mdm.product.service.impl;

import com.simplecommerce_mdm.category.model.Category;
import com.simplecommerce_mdm.category.repository.CategoryRepository;
import com.simplecommerce_mdm.cloudinary.service.CloudinaryService;
import com.simplecommerce_mdm.common.enums.ImageTargetType;
import com.simplecommerce_mdm.common.enums.ProductStatus;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.product.dto.ProductCreateRequest;
import com.simplecommerce_mdm.product.dto.ProductResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.ArrayList;
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