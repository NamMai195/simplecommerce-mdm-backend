package com.simplecommerce_mdm.user.repository;

import com.simplecommerce_mdm.user.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find addresses by city
     */
    List<Address> findByCity(String city);

    /**
     * Find addresses by district
     */
    List<Address> findByDistrict(String district);

    /**
     * Find addresses by city and district
     */
    List<Address> findByCityAndDistrict(String city, String district);

    /**
     * Find addresses by country code
     */
    List<Address> findByCountryCode(String countryCode);

    /**
     * Search addresses by street address (partial match)
     */
    @Query("SELECT a FROM Address a WHERE LOWER(a.streetAddress1) LIKE LOWER(CONCAT('%', :streetAddress, '%'))")
    List<Address> findByStreetAddressContaining(@Param("streetAddress") String streetAddress);

    /**
     * Find addresses by postal code
     */
    List<Address> findByPostalCode(String postalCode);
}
