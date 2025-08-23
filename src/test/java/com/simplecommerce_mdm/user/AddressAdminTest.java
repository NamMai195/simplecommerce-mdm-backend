package com.simplecommerce_mdm.user;

import com.simplecommerce_mdm.user.dto.AdminAddressSearchRequest;
import com.simplecommerce_mdm.user.dto.AdminAddressResponse;
import com.simplecommerce_mdm.user.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AddressAdminTest {

    @Autowired
    private AddressService addressService;

    @Test
    public void testAdminAddressSearchRequestCreation() {
        AdminAddressSearchRequest request = AdminAddressSearchRequest.builder()
                .userEmail("test@example.com")
                .city("Ho Chi Minh")
                .page(0)
                .size(10)
                .build();
        
        assertNotNull(request);
        assertEquals("test@example.com", request.getUserEmail());
        assertEquals("Ho Chi Minh", request.getCity());
        assertEquals(0, request.getPage());
        assertEquals(10, request.getSize());
    }

    @Test
    public void testAdminAddressResponseCreation() {
        AdminAddressResponse response = AdminAddressResponse.builder()
                .userAddressId(1L)
                .userId(1L)
                .userEmail("user@example.com")
                .city("Ho Chi Minh")
                .build();
        
        assertNotNull(response);
        assertEquals(1L, response.getUserAddressId());
        assertEquals(1L, response.getUserId());
        assertEquals("user@example.com", response.getUserEmail());
        assertEquals("Ho Chi Minh", response.getCity());
    }
}
