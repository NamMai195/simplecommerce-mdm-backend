package com.simplecommerce_mdm.auth.service;

import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
@Slf4j
public class UserServiceDetail implements UserDetailsService {

    private final UserRepository userRepository;

    public UserServiceDetail(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        log.info("Found user: {}. Number of roles: {}", user.getFullName(), user.getRoles().size());
        
        return new CustomUserDetails(user);
    }

    public UserDetailsService userDetailsService() {
        return this;
    }
}
