package com.simplecommerce_mdm.product.repository;

import com.simplecommerce_mdm.common.enums.ImageTargetType;
import com.simplecommerce_mdm.product.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByTargetIdAndTargetType(Long targetId, ImageTargetType targetType);
    List<ProductImage> findByIdInAndTargetIdAndTargetType(Collection<Long> ids, Long targetId, ImageTargetType targetType);
} 