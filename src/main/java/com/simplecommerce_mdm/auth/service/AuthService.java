package com.simplecommerce_mdm.auth.service;

import com.simplecommerce_mdm.auth.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest registerRequest);
}
