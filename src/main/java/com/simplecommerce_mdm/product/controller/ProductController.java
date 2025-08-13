package com.simplecommerce_mdm.product.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.product.dto.PriceRangeResponse;
import com.simplecommerce_mdm.product.dto.ProductBuyerListResponse;
import com.simplecommerce_mdm.product.dto.ProductBuyerResponse;
import com.simplecommerce_mdm.product.dto.ProductFilterRequest;
import com.simplecommerce_mdm.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Controller", description = "Public APIs for browsing products")
@Slf4j(topic = "PRODUCT-CONTROLLER")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/featured")
    @Operation(summary = "Get Featured Products", 
               description = "Get list of featured products available for purchase")
    public ResponseEntity<ApiResponse<ProductBuyerListResponse>> getFeaturedProducts(
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 10)") 
            @RequestParam(defaultValue = "10") Integer size,
            
            @Parameter(description = "Sort field (default createdAt)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (default desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Getting featured products: page={}, size={}, sortBy={}, sortDirection={}", 
                 page, size, sortBy, sortDirection);

        ProductBuyerListResponse featuredProducts = productService.getFeaturedProducts(
                page, size, sortBy, sortDirection);

        ApiResponse<ProductBuyerListResponse> response = ApiResponse.<ProductBuyerListResponse>builder()
                .message("Featured products retrieved successfully")
                .data(featuredProducts)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Search Products", 
               description = "Search all approved products with pagination")
    public ResponseEntity<ApiResponse<ProductBuyerListResponse>> getProducts(
            @Parameter(description = "Search term for product name or SKU") 
            @RequestParam(defaultValue = "") String searchTerm,
            
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 20)") 
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field (default createdAt)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (default desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Searching products: searchTerm='{}', page={}, size={}, sortBy={}, sortDirection={}", 
                 searchTerm, page, size, sortBy, sortDirection);

        ProductBuyerListResponse products = productService.getProducts(
                searchTerm, page, size, sortBy, sortDirection);

        ApiResponse<ProductBuyerListResponse> response = ApiResponse.<ProductBuyerListResponse>builder()
                .message("Products retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Product Details", 
               description = "Get detailed information about a specific product")
    public ResponseEntity<ApiResponse<ProductBuyerResponse>> getProductById(
            @Parameter(description = "Product ID") 
            @PathVariable Long id) {

        log.info("Getting product details for ID: {}", id);

        ProductBuyerResponse product = productService.getProductById(id);

        ApiResponse<ProductBuyerResponse> response = ApiResponse.<ProductBuyerResponse>builder()
                .message("Product details retrieved successfully")
                .data(product)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories/{categoryId}")
    @Operation(summary = "Get Products by Category", 
               description = "Get products filtered by category with search and pagination")
    public ResponseEntity<ApiResponse<ProductBuyerListResponse>> getProductsByCategory(
            @Parameter(description = "Category ID") 
            @PathVariable Integer categoryId,
            
            @Parameter(description = "Search term for product name") 
            @RequestParam(defaultValue = "") String searchTerm,
            
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 20)") 
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field (default createdAt)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (default desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Getting products by category: categoryId={}, searchTerm='{}', page={}, size={}", 
                 categoryId, searchTerm, page, size);

        ProductBuyerListResponse products = productService.getProductsByCategory(
                categoryId, searchTerm, page, size, sortBy, sortDirection);

        ApiResponse<ProductBuyerListResponse> response = ApiResponse.<ProductBuyerListResponse>builder()
                .message("Products by category retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/shops/{shopId}")
    @Operation(summary = "Get Products by Shop", 
               description = "Get products from a specific shop with search and pagination")
    public ResponseEntity<ApiResponse<ProductBuyerListResponse>> getProductsByShop(
            @Parameter(description = "Shop ID") 
            @PathVariable Long shopId,
            
            @Parameter(description = "Search term for product name") 
            @RequestParam(defaultValue = "") String searchTerm,
            
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 20)") 
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field (default createdAt)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (default desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Getting products by shop: shopId={}, searchTerm='{}', page={}, size={}", 
                 shopId, searchTerm, page, size);

        ProductBuyerListResponse products = productService.getProductsByShop(
                shopId, searchTerm, page, size, sortBy, sortDirection);

        ApiResponse<ProductBuyerListResponse> response = ApiResponse.<ProductBuyerListResponse>builder()
                .message("Products by shop retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    @Operation(summary = "Get Latest Products", 
               description = "Get recently added products")
    public ResponseEntity<ApiResponse<ProductBuyerListResponse>> getLatestProducts(
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 20)") 
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field (default createdAt)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (default desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Getting latest products: page={}, size={}", page, size);

        ProductBuyerListResponse products = productService.getLatestProducts(
                page, size, sortBy, sortDirection);

        ApiResponse<ProductBuyerListResponse> response = ApiResponse.<ProductBuyerListResponse>builder()
                .message("Latest products retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get Price Range", 
               description = "Get minimum and maximum prices of all approved products")
    public ResponseEntity<ApiResponse<PriceRangeResponse>> getPriceRange() {

        log.info("Getting price range for products");

        PriceRangeResponse priceRange = productService.getPriceRange();

        ApiResponse<PriceRangeResponse> response = ApiResponse.<PriceRangeResponse>builder()
                .message("Price range retrieved successfully")
                .data(priceRange)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter-by-price")
    @Operation(summary = "Filter Products by Price Range", 
               description = "Get products within specified price range")
    public ResponseEntity<ApiResponse<ProductBuyerListResponse>> getProductsByPriceRange(
            @Parameter(description = "Minimum price") 
            @RequestParam(required = false) BigDecimal minPrice,
            
            @Parameter(description = "Maximum price") 
            @RequestParam(required = false) BigDecimal maxPrice,
            
            @Parameter(description = "Search term for product name") 
            @RequestParam(defaultValue = "") String searchTerm,
            
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 20)") 
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field (default createdAt)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (default desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Getting products by price range: minPrice={}, maxPrice={}, searchTerm='{}', page={}, size={}", 
                 minPrice, maxPrice, searchTerm, page, size);

        ProductBuyerListResponse products = productService.getProductsByPriceRange(
                minPrice, maxPrice, searchTerm, page, size, sortBy, sortDirection);

        ApiResponse<ProductBuyerListResponse> response = ApiResponse.<ProductBuyerListResponse>builder()
                .message("Products by price range retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    @Operation(summary = "Advanced Product Filtering", 
               description = "Filter products by multiple criteria (category, shop, price, search)")
    public ResponseEntity<ApiResponse<ProductBuyerListResponse>> getProductsWithFilters(
            @Parameter(description = "Category ID") 
            @RequestParam(required = false) Integer categoryId,
            
            @Parameter(description = "Shop ID") 
            @RequestParam(required = false) Long shopId,
            
            @Parameter(description = "Minimum price") 
            @RequestParam(required = false) BigDecimal minPrice,
            
            @Parameter(description = "Maximum price") 
            @RequestParam(required = false) BigDecimal maxPrice,
            
            @Parameter(description = "Search term") 
            @RequestParam(required = false) String searchTerm,
            
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 20)") 
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field (default createdAt)") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (default desc)") 
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Getting products with filters: categoryId={}, shopId={}, minPrice={}, maxPrice={}, searchTerm='{}', page={}, size={}", 
                 categoryId, shopId, minPrice, maxPrice, searchTerm, page, size);

        ProductFilterRequest filterRequest = new ProductFilterRequest();
        filterRequest.setCategoryId(categoryId);
        filterRequest.setShopId(shopId);
        filterRequest.setMinPrice(minPrice);
        filterRequest.setMaxPrice(maxPrice);
        filterRequest.setSearchTerm(searchTerm);
        filterRequest.setPage(page);
        filterRequest.setSize(size);
        filterRequest.setSortBy(sortBy);
        filterRequest.setSortDirection(sortDirection);

        ProductBuyerListResponse products = productService.getProductsWithFilters(filterRequest);

        ApiResponse<ProductBuyerListResponse> response = ApiResponse.<ProductBuyerListResponse>builder()
                .message("Filtered products retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Get Related Products", 
               description = "Get products related to a specific product (same category)")
    public ResponseEntity<ApiResponse<ProductBuyerListResponse>> getRelatedProducts(
            @Parameter(description = "Product ID") 
            @PathVariable Long id,
            
            @Parameter(description = "Page number (default 0)") 
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size (default 10)") 
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Getting related products for productId: {}, page={}, size={}", id, page, size);

        ProductBuyerListResponse products = productService.getRelatedProducts(id, page, size);

        ApiResponse<ProductBuyerListResponse> response = ApiResponse.<ProductBuyerListResponse>builder()
                .message("Related products retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }
} 