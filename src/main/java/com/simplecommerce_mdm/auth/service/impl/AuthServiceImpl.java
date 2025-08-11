package com.simplecommerce_mdm.auth.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.simplecommerce_mdm.auth.dto.*;
import com.simplecommerce_mdm.auth.model.PasswordResetToken;
import com.simplecommerce_mdm.auth.repository.PasswordResetTokenRepository;
import com.simplecommerce_mdm.auth.service.AuthService;
import com.simplecommerce_mdm.auth.service.JwtService;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.email.service.EmailService;
import com.simplecommerce_mdm.exception.InvalidDataException;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.user.model.Role;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.RoleRepository;
import com.simplecommerce_mdm.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTH-SERVICE")
public class AuthServiceImpl implements AuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${jwt.password-reset.expiry-minutes:10}")
    private int passwordResetExpiryMinutes;

    @Value("${jwt.password-reset.max-attempts:3}")
    private int maxPasswordResetAttempts;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final HttpServletRequest httpServletRequest;

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
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        userRepository.save(user);
    }

    @Override
    public TokenResponse login(LoginRequest loginrequest) {
        logger.info("Login request for email: {}", loginrequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginrequest.getEmail(), loginrequest.getPassword()));

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = customUserDetails.getUser();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        logger.info("User authenticated successfully: {}", user.getFullName());
        logger.info("User ID: {}", user.getId());

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), authorities);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail(), authorities);

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
            throw new AuthenticationServiceException(
                    "Google Client ID is missing or invalid in backend configuration.");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                logger.error(
                        "Google token verification failed. Token might be invalid, expired, or have an audience mismatch.");
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

            String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), authorities);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail(), authorities);

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

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        logger.info("Forgot password request for email: {}", request.getEmail());

        // Check if user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new InvalidDataException("User account is deactivated");
        }

        // Check for existing active tokens
        LocalDateTime now = LocalDateTime.now();
        long activeTokens = passwordResetTokenRepository.countActiveTokensForEmail(request.getEmail(), now);

        if (activeTokens >= maxPasswordResetAttempts) {
            throw new InvalidDataException(
                    "Too many password reset attempts. Please wait before requesting another reset.");
        }

        // Invalidate any existing tokens for this email
        passwordResetTokenRepository.invalidateAllTokensForEmail(request.getEmail(), now);

        // Generate OTP code (6 digits)
        String otpCode = generateOtpCode();

        // Generate unique token
        String token = UUID.randomUUID().toString();

        // Set expiration time
        LocalDateTime expiresAt = now.plusMinutes(passwordResetExpiryMinutes);

        // Create password reset token
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(token)
                .userEmail(request.getEmail())
                .user(user)
                .otpCode(otpCode)
                .expiresAt(expiresAt)
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .build();

        passwordResetTokenRepository.save(passwordResetToken);

        // Send OTP email
        try {
            emailService.sendPasswordResetOtpEmail(
                    request.getEmail(),
                    otpCode,
                    passwordResetExpiryMinutes,
                    user.getFullName());
            logger.info("Password reset OTP sent to email: {}", request.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send password reset OTP email: {}", e.getMessage(), e);
            throw new InvalidDataException("Failed to send OTP email. Please try again later.");
        }

        return ForgotPasswordResponse.builder()
                .email(request.getEmail())
                .message("OTP code has been sent to your email address")
                .expiresAt(expiresAt)
                .expiresInMinutes(passwordResetExpiryMinutes)
                .build();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        logger.info("Change password request for email: {}", request.getEmail());

        // Check if user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new InvalidDataException("User account is deactivated");
        }

        // Find valid token by OTP code and email
        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken token = passwordResetTokenRepository.findByOtpCodeAndEmail(
                request.getOtpCode(),
                request.getEmail(),
                now).orElseThrow(() -> new InvalidDataException("Invalid or expired OTP code"));

        // Check if token is valid
        if (!token.isValid()) {
            throw new InvalidDataException("OTP code has expired or already been used");
        }

        // Update user password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        token.markAsUsed();
        passwordResetTokenRepository.save(token);

        // Invalidate all other tokens for this email
        passwordResetTokenRepository.invalidateAllTokensForEmail(request.getEmail(), now);

        logger.info("Password changed successfully for user: {}", request.getEmail());
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
        newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        return userRepository.save(newUser);
    }

    /**
     * Generate a 6-digit OTP code
     */
    private String generateOtpCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = httpServletRequest.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return httpServletRequest.getRemoteAddr();
    }

    /**
     * Get user agent from request
     */
    private String getUserAgent() {
        return httpServletRequest.getHeader("User-Agent");
    }
}
