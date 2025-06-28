package com.simplecommerce_mdm.auth.service.impl;

import com.simplecommerce_mdm.auth.dto.LoginRequest;
import com.simplecommerce_mdm.auth.dto.TokenResponse;
import com.simplecommerce_mdm.auth.service.TokenService;
import com.simplecommerce_mdm.auth.service.JwtService;
import com.simplecommerce_mdm.exception.ForBiddenException;
import com.simplecommerce_mdm.exception.InvalidDataException;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

import static com.simplecommerce_mdm.common.enums.TokenType.REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TOKEN-SERVICE")
public class TokenServiceImpl implements TokenService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    @Override
    public TokenResponse getAccessToken(LoginRequest request) {
        log.info("Get access token for email: {}", request.getEmail());
    
        Authentication authenticate;
        try {
            // Thực hiện xác thực với email và password
            authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    
            log.info("isAuthenticated = {}", authenticate.isAuthenticated());
            log.info("Authorities: {}", authenticate.getAuthorities().toString());
    
            // Nếu xác thực thành công, lưu thông tin vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authenticate);
        } catch (BadCredentialsException | DisabledException e) {
            log.error("errorMessage: {}", e.getMessage());
            throw new AccessDeniedException(e.getMessage());
        }
    
        // Lấy người dùng từ cơ sở dữ liệu để lấy ID
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));
    
        // Lấy thông tin vai trò của người dùng từ đối tượng Authentication
        var authorities = authenticate.getAuthorities();
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        log.info("User roles: {}", roles);
    
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getFullName(), authorities);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getFullName(), authorities);
    
        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    @Override
    public TokenResponse getRefreshToken(String refreshToken) {
        log.info("Get refresh token");

        if (!StringUtils.hasLength(refreshToken)) {
            throw new InvalidDataException("Token must be not blank");
        }

        try {
            // Xác minh mã thông báo
            String userName = jwtService.extractUsername(refreshToken, REFRESH_TOKEN);

            // Kiểm tra người dùng đang hoạt động hay không hoạt động
            User user = userRepository.findByFullName(userName)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + userName));

            // Tạo mã thông báo truy cập mới
            UserDetails userDetails = new com.simplecommerce_mdm.config.CustomUserDetails(user);
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getFullName(), userDetails.getAuthorities());

            return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
        } catch (Exception e) {
            log.error("Access denied! errorMessage: {}", e.getMessage());
            throw new ForBiddenException(e.getMessage());
        }
    }
}
