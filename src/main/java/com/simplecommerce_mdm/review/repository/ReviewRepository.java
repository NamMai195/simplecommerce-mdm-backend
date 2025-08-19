package com.simplecommerce_mdm.review.repository;

import com.simplecommerce_mdm.product.model.Product;
import com.simplecommerce_mdm.review.model.Review;
import com.simplecommerce_mdm.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Tìm reviews theo product với pagination
    Page<Review> findByProductOrderByCreatedAtDesc(Product product, Pageable pageable);
    
    // Tìm reviews theo user với pagination
    Page<Review> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Tìm review theo user và product (1 user chỉ review 1 lần)
    Optional<Review> findByUserAndProduct(User user, Product product);
    
    // Tìm review theo order
    Optional<Review> findByOrderId(Long orderId);
    
    // Đếm reviews theo product
    Long countByProduct(Product product);
    
    // Tính rating trung bình theo product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product AND r.isApproved = true")
    Double getAverageRatingByProduct(@Param("product") Product product);
    
    // Tìm reviews theo rating range
    List<Review> findByProductAndRatingBetweenAndIsApprovedTrue(Product product, Integer minRating, Integer maxRating);
    
    // Tìm reviews theo product và rating cụ thể
    Long countByProductAndRatingAndIsApprovedTrue(Product product, Integer rating);
    
    // Tìm reviews chưa được approved
    Page<Review> findByIsApprovedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    // Tìm reviews bị reported
    Page<Review> findByIsReportedTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // Tìm reviews theo shop (thông qua product)
    @Query("SELECT r FROM Review r JOIN r.product p WHERE p.shop.id = :shopId AND r.isApproved = true ORDER BY r.createdAt DESC")
    Page<Review> findByShopId(@Param("shopId") Long shopId, Pageable pageable);
    
    // Đếm reviews theo shop
    @Query("SELECT COUNT(r) FROM Review r JOIN r.product p WHERE p.shop.id = :shopId AND r.isApproved = true")
    Long countByShopId(@Param("shopId") Long shopId);
    
    // Tính rating trung bình theo shop
    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.product p WHERE p.shop.id = :shopId AND r.isApproved = true")
    Double getAverageRatingByShopId(@Param("shopId") Long shopId);
}
