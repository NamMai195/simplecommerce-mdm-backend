package com.simplecommerce_mdm.auth.service;


import com.simplecommerce_mdm.auth.dto.UserRegistrationDto;

public interface AuthService {
    void register(UserRegistrationDto userRegistrationDto);
}
