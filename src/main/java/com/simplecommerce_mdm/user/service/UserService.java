package com.simplecommerce_mdm.user.service;

import com.simplecommerce_mdm.user.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

public interface UserService {

    // Existing methods
    UserResponse getLoggedInUserProfile(Long userId);

    UserResponse updateProfile(Long userId, UserUpdateRequest updateRequest, MultipartFile avatarFile);

    // Admin user management methods
    Page<UserAdminResponse> getAllUsers(UserSearchRequest searchRequest);

    UserAdminResponse getUserById(Long userId);

    UserAdminResponse getUserByUuid(UUID uuid);

    UserAdminResponse createUser(UserCreateRequest createRequest);

    UserAdminResponse updateUser(Long userId, UserUpdateRequest updateRequest);

    UserAdminResponse updateUserStatus(Long userId, UserStatusUpdateRequest statusRequest);

    void deleteUser(Long userId);

    void restoreUser(Long userId);

    // User verification methods
    void verifyUserEmail(Long userId);

    void verifyUserPhone(Long userId);

    void resetUserPassword(Long userId, String newPassword);

    // Statistics methods
    Map<String, Object> getUserStatistics();

    Map<String, Object> getUserStatistics(Long userId);
}