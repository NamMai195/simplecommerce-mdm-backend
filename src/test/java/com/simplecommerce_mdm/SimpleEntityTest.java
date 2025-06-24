package com.simplecommerce_mdm;

import com.simplecommerce_mdm.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class SimpleEntityTest {

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully with all entities
        User user = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .passwordHash("hashed")
                .build();
        
        // If we reach here, entities are properly configured
        assert user.getEmail().equals("test@example.com");
    }
} 