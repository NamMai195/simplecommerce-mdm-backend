package com.simplecommerce_mdm.auth.service;

import com.simplecommerce_mdm.auth.dto.*;

public interface AuthService {
    RegisterResponse register(RegisterRequest registerRequest);

    TokenResponse login(LoginRequest loginrequest);

    void logout(String authorizationHeader);

    TokenResponse loginWithGoogle(String googleToken);

    // Email verification methods
    void verifyEmail(EmailVerificationRequest request);

    // Password reset methods
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}
