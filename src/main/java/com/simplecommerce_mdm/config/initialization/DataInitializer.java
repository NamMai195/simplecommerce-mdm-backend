package com.simplecommerce_mdm.config.initialization;

import com.simplecommerce_mdm.user.model.Role;
import com.simplecommerce_mdm.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Data Initializer: Checking for default roles...");

        List<String> defaultRoles = Arrays.asList("ADMIN", "USER", "SELLER");

        for (String roleName : defaultRoles) {
            if (!roleRepository.existsByRoleName(roleName)) {
                Role newRole = new Role();
                newRole.setRoleName(roleName);
                roleRepository.save(newRole);
                log.info("Created default role: {}", roleName);
            } else {
                log.info("Role '{}' already exists. Skipping.", roleName);
            }
        }

        log.info("Data Initializer: Finished checking roles.");
    }
} 