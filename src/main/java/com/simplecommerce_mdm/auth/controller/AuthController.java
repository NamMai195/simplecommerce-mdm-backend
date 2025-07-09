package com.simplecommerce_mdm.auth.controller;

import com.simplecommerce_mdm.auth.dto.GoogleAuthRequest;
import com.simplecommerce_mdm.auth.dto.LoginRequest;
import com.simplecommerce_mdm.auth.dto.RegisterRequest;
import com.simplecommerce_mdm.auth.dto.TokenResponse;
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

    @Operation(summary = "Register User", description = "API register new user ")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("User registered successfully!")
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
}