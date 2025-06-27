package com.simplecommerce_mdm.product.repository;

import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByUser(User user);
} 