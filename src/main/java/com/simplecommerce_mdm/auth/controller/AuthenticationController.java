package com.simplecommerce_mdm.auth.controller;


import com.simplecommerce_mdm.auth.dto.LoginRequest;
import com.simplecommerce_mdm.auth.dto.TokenResponse;
import com.simplecommerce_mdm.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Controller")
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Access token", description = "Get access token and refresh token by email and password")
    @PostMapping("/access-token")
    public TokenResponse accessToken(@RequestBody LoginRequest request) {
        log.info("Access token request");
        return authenticationService.getAccessToken(request);
    }

    @Operation(summary = "Refresh token", description = "Get access token by refresh token")
    @PostMapping("/refresh-token")
    public TokenResponse refreshToken(@RequestBody String refreshToken) {
        log.info("Refresh token request");
        return authenticationService.getRefreshToken(refreshToken);
    }
}
