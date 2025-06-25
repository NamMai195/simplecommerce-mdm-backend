package com.simplecommerce_mdm.auth.controller;

import com.simplecommerce_mdm.auth.dto.GoogleAuthRequest;
import com.simplecommerce_mdm.auth.dto.LoginRequest;
import com.simplecommerce_mdm.auth.dto.RegisterRequest;
import com.simplecommerce_mdm.auth.dto.TokenResponse;
import com.simplecommerce_mdm.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j(topic = "AUTH-CONTROLLER")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register User", description = "API register new user to database")
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }

    @Operation(summary = "Login User", description = "API login user ")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        log.info("Login request: {}", loginRequest);
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "Logout User", description = "API logout user ")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("Logout request");
        authService.logout(authorizationHeader);
        return ResponseEntity.ok("Logout successful");
    }

    @Operation(summary = "Google Login User", description = "API google login user ")
    @PostMapping("/google")
    public ResponseEntity<TokenResponse> googleLogin(@RequestBody GoogleAuthRequest googleRequest) {
        log.info("Google login request");
        TokenResponse tokenResponse = authService.loginWithGoogle(googleRequest.getToken());
        return ResponseEntity.ok(tokenResponse);
    }
}
