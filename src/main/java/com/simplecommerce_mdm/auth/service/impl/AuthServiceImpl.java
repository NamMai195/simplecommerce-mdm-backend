package com.simplecommerce_mdm.auth.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.simplecommerce_mdm.auth.dto.LoginRequest;
import com.simplecommerce_mdm.auth.dto.RegisterRequest;
import com.simplecommerce_mdm.auth.dto.TokenResponse;
import com.simplecommerce_mdm.auth.service.AuthService;
import com.simplecommerce_mdm.auth.service.JwtService;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.exception.InvalidDataException;
import com.simplecommerce_mdm.user.model.Role;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.RoleRepository;
import com.simplecommerce_mdm.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTH-SERVICE")
public class AuthServiceImpl implements AuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already in use!");
        }

        User user = modelMapper.map(registerRequest, User.class);
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setUuid(UUID.randomUUID());

        Role userRole = roleRepository.findByRoleName("USER")
             .orElseThrow(() -> new RuntimeException("Error: Role 'USER' is not found."));
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);
    }

    @Override
    public TokenResponse login(LoginRequest loginrequest) {
        logger.info("Login request for email: {}", loginrequest.getEmail());
    
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginrequest.getEmail(), loginrequest.getPassword())
        );
    
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = customUserDetails.getUser();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    
        logger.info("User authenticated successfully: {}", user.getFullName());
        logger.info("User ID: {}", user.getId());

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getFullName(), authorities);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getFullName(), authorities);
    
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(String authorizationHeader) {
        // TODO: Implement token blacklisting mechanism
        logger.info("Logout requested. Token (potentially invalidated): {}", authorizationHeader);
    }

    @Override
    @Transactional
    public TokenResponse loginWithGoogle(String googleToken) {
        logger.info("Received Google Token in backend.");
        if (googleClientId == null || googleClientId.isEmpty() || "null".equalsIgnoreCase(googleClientId)) {
            logger.error("Google Client ID is not configured properly in the backend! Value is: '{}'", googleClientId);
            throw new AuthenticationServiceException("Google Client ID is missing or invalid in backend configuration.");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                logger.error("Google token verification failed. Token might be invalid, expired, or have an audience mismatch.");
                throw new InvalidDataException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createNewUserFromGoogle(payload));

            logger.info("User authenticated via Google: {}. User ID: {}", user.getEmail(), user.getId());

            UserDetails userDetails = new CustomUserDetails(user);
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            String accessToken = jwtService.generateAccessToken(user.getId(), user.getFullName(), authorities);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getFullName(), authorities);

            logger.info("Generated tokens for user ID: {}", user.getId());

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (InvalidDataException e) {
            logger.error("Invalid Google token provided: {}", e.getMessage());
            throw new AuthenticationServiceException("Invalid Google token", e);
        } catch (Exception e) {
            logger.error("Google authentication process failed: {}", e.getMessage(), e);
            throw new AuthenticationServiceException("Google authentication failed", e);
        }
    }

    private User createNewUserFromGoogle(GoogleIdToken.Payload payload) {
        logger.info("User with email {} not found. Creating new user.", payload.getEmail());
        User newUser = new User();
        newUser.setEmail(payload.getEmail());
        newUser.setFirstName((String) payload.get("given_name"));
        newUser.setLastName((String) payload.get("family_name"));
        newUser.setAvatarUrl((String) payload.get("picture"));
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setIsActive(true);
        if (Boolean.TRUE.equals(payload.getEmailVerified())) {
            newUser.setEmailVerifiedAt(LocalDateTime.now());
        }

        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new RuntimeException("Error: Role 'USER' not found."));
        newUser.setRoles(Collections.singleton(userRole));

        return userRepository.save(newUser);
    }
}
