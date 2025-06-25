package com.simplecommerce_mdm.auth.service;

import com.simplecommerce_mdm.auth.dto.LoginRequest;
import com.simplecommerce_mdm.auth.dto.RegisterRequest;
import com.simplecommerce_mdm.auth.dto.TokenResponse;

public interface AuthService {
    void register(RegisterRequest registerRequest);
    TokenResponse login(LoginRequest loginrequest);
    void logout(String authorizationHeader);
    TokenResponse loginWithGoogle(String googleToken);
}
