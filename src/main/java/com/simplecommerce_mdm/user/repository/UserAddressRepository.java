package com.simplecommerce_mdm.user.repository;

import com.simplecommerce_mdm.user.model.UserAddress;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.common.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    /**
     * Find all addresses for a specific user
     */
    List<UserAddress> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find all addresses for a specific user by user ID
     */
    List<UserAddress> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find address by ID and user ID (for validation)
     */
    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);

    /**
     * Find default shipping address for user
     */
    Optional<UserAddress> findByUserIdAndIsDefaultShippingTrue(Long userId);

    /**
     * Find default billing address for user
     */
    Optional<UserAddress> findByUserIdAndIsDefaultBillingTrue(Long userId);

    /**
     * Find addresses by type for user
     */
    List<UserAddress> findByUserIdAndAddressType(Long userId, AddressType addressType);

    /**
     * Count addresses for user
     */
    long countByUserId(Long userId);

    /**
     * Check if user has any addresses
     */
    boolean existsByUserId(Long userId);

    /**
     * Reset all default shipping addresses for user
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefaultShipping = false WHERE ua.user.id = :userId")
    void resetDefaultShippingAddresses(@Param("userId") Long userId);

    /**
     * Reset all default billing addresses for user
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefaultBilling = false WHERE ua.user.id = :userId")
    void resetDefaultBillingAddresses(@Param("userId") Long userId);

    /**
     * Find addresses by user and address (for duplicate check)
     */
    Optional<UserAddress> findByUserAndAddress(User user, com.simplecommerce_mdm.user.model.Address address);

    /**
     * Check if address exists by address entity
     */
    boolean existsByAddress(com.simplecommerce_mdm.user.model.Address address);
}
