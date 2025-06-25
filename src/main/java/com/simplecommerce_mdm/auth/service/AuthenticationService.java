package com.simplecommerce_mdm.auth.service;


import com.simplecommerce_mdm.auth.dto.LoginRequest;
import com.simplecommerce_mdm.auth.dto.TokenResponse;

public interface AuthenticationService {
    TokenResponse getAccessToken(LoginRequest request);
    TokenResponse getRefreshToken(String request);
}

