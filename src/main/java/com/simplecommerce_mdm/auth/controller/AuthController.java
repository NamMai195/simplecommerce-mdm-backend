package com.simplecommerce_mdm.auth.controller;

import com.simplecommerce_mdm.auth.dto.*;
import com.simplecommerce_mdm.auth.service.AuthService;
import com.simplecommerce_mdm.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller")
@Slf4j(topic = "AUTH-CONTROLLER")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register User", description = "API register new user with email verification required")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> registerUser(
            @Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration request for email: {}", registerRequest.getEmail());
        RegisterResponse response = authService.register(registerRequest);
        ApiResponse<RegisterResponse> apiResponse = ApiResponse.<RegisterResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("User registered successfully! Please check your email for verification OTP.")
                .data(response)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Login User", description = "API login user ")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> loginUser(@RequestBody LoginRequest loginRequest) {
        log.info("Login request: {}", loginRequest);
        TokenResponse tokenResponse = authService.login(loginRequest);
        ApiResponse<TokenResponse> response = ApiResponse.<TokenResponse>builder()
                .message("Login successful")
                .data(tokenResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout User", description = "API logout user ")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("Logout request");
        authService.logout(authorizationHeader);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Logout successful")
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Google Login User", description = "API google login user ")
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<TokenResponse>> googleLogin(@RequestBody GoogleAuthRequest googleRequest) {
        log.info("Google login request");
        TokenResponse tokenResponse = authService.loginWithGoogle(googleRequest.getToken());
        ApiResponse<TokenResponse> response = ApiResponse.<TokenResponse>builder()
                .message("Google login successful")
                .data(tokenResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Forgot Password", description = "Request password reset OTP via email")
    @PostMapping("/forgotpassword")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        ForgotPasswordResponse response = authService.forgotPassword(request);
        ApiResponse<ForgotPasswordResponse> apiResponse = ApiResponse.<ForgotPasswordResponse>builder()
                .message("Password reset OTP sent successfully")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Change Password", description = "Change password using OTP verification")
    @PostMapping("/changepassword")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request for email: {}", request.getEmail());
        authService.changePassword(request);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Password changed successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify Email", description = "Verify user email using OTP code")
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        log.info("Email verification request for email: {}", request.getEmail());
        authService.verifyEmail(request);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Email verified successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}