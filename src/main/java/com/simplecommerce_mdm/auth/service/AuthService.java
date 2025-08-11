package com.simplecommerce_mdm.auth.service;

import com.simplecommerce_mdm.auth.dto.*;

public interface AuthService {
    void register(RegisterRequest registerRequest);

    TokenResponse login(LoginRequest loginrequest);

    void logout(String authorizationHeader);

    TokenResponse loginWithGoogle(String googleToken);

    // Password reset methods
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}
