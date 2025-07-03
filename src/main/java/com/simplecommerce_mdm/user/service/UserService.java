package com.simplecommerce_mdm.user.service;

import com.simplecommerce_mdm.user.dto.UserResponse;
import com.simplecommerce_mdm.user.dto.UserUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse getLoggedInUserProfile(Long userId);
    UserResponse updateProfile(Long userId, UserUpdateRequest updateRequest, MultipartFile avatarFile);
}