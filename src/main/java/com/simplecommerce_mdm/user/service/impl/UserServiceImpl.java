package com.simplecommerce_mdm.user.service.impl;

import com.simplecommerce_mdm.cloudinary.service.CloudinaryService;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.exception.DuplicateResourceException;
import com.simplecommerce_mdm.exception.InvalidDataException;
import com.simplecommerce_mdm.user.dto.*;
import com.simplecommerce_mdm.user.model.Role;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.RoleRepository;
import com.simplecommerce_mdm.user.repository.UserRepository;
import com.simplecommerce_mdm.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER-SERVICE")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getLoggedInUserProfile(Long userId) {
        log.info("Fetching profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest updateRequest, MultipartFile avatarFile) {
        log.info("Updating profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Cập nhật các trường có thể thay đổi
        Optional.ofNullable(updateRequest.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(updateRequest.getLastName()).ifPresent(user::setLastName);

        // Kiểm tra số điện thoại
        if (updateRequest.getPhoneNumber() != null && !updateRequest.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(updateRequest.getPhoneNumber())) {
                throw new DuplicateResourceException("Phone number already in use");
            }
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }

        // Xử lý upload ảnh đại diện nếu có
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    cloudinaryService.deleteFile(user.getAvatarUrl());
                }
                CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadAvatar(avatarFile, userId);
                user.setAvatarUrl(uploadResult.url());
                log.info("Avatar updated for user {}. New URL: {}", userId, uploadResult.url());
            } catch (Exception e) {
                log.error("Failed to upload avatar for user {}: {}", userId, e.getMessage());
                throw new InvalidDataException("Failed to upload avatar: " + e.getMessage());
            }
        } else if (updateRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(updateRequest.getAvatarUrl());
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for ID: {}", userId);
        return mapToUserResponse(updatedUser);
    }

    @Override
    public Page<UserAdminResponse> getAllUsers(UserSearchRequest searchRequest) {
        log.info("Fetching users with search criteria: {}", searchRequest.getSearchTerm());

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(searchRequest.getSortDirection()), searchRequest.getSortBy());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        // Execute search with filters
        Page<User> users = userRepository.findUsersWithFilters(
                searchRequest.getSearchTerm(),
                searchRequest.getRoleName(),
                searchRequest.getIsActive(),
                searchRequest.getEmailVerified(),
                searchRequest.getPhoneVerified(),
                pageable);

        return users.map(this::mapToUserAdminResponse);
    }

    @Override
    public UserAdminResponse getUserById(Long userId) {
        log.info("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToUserAdminResponse(user);
    }

    @Override
    public UserAdminResponse getUserByUuid(UUID uuid) {
        log.info("Fetching user by UUID: {}", uuid);
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid));
        return mapToUserAdminResponse(user);
    }

    @Override
    @Transactional
    public UserAdminResponse createUser(UserCreateRequest createRequest) {
        log.info("Creating new user with email: {}", createRequest.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(createRequest.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + createRequest.getEmail());
        }

        // Check if phone number already exists
        if (createRequest.getPhoneNumber() != null
                && userRepository.existsByPhoneNumber(createRequest.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already in use: " + createRequest.getPhoneNumber());
        }

        // Create user entity
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .firstName(createRequest.getFirstName())
                .lastName(createRequest.getLastName())
                .email(createRequest.getEmail())
                .phoneNumber(createRequest.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(createRequest.getPassword()))
                .avatarUrl(createRequest.getAvatarUrl())
                .isActive(createRequest.getIsActive())
                .metadata(createRequest.getMetadata() != null ? createRequest.getMetadata() : "{}")
                .build();

        // Assign roles
        Set<Role> roles = new HashSet<>();
        if (createRequest.getRoleNames() != null && !createRequest.getRoleNames().isEmpty()) {
            for (String roleName : createRequest.getRoleNames()) {
                Role role = roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                roles.add(role);
            }
        } else {
            // Default to USER role
            Role userRole = roleRepository.findByRoleName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default USER role not found"));
            roles.add(userRole);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return mapToUserAdminResponse(savedUser);
    }

    @Override
    @Transactional
    public UserAdminResponse updateUser(Long userId, UserUpdateRequest updateRequest) {
        log.info("Updating user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Update basic fields
        Optional.ofNullable(updateRequest.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(updateRequest.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(updateRequest.getAvatarUrl()).ifPresent(user::setAvatarUrl);
        Optional.ofNullable(updateRequest.getMetadata()).ifPresent(user::setMetadata);

        // Check phone number uniqueness
        if (updateRequest.getPhoneNumber() != null && !updateRequest.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(updateRequest.getPhoneNumber())) {
                throw new DuplicateResourceException("Phone number already in use");
            }
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);
        return mapToUserAdminResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserAdminResponse updateUserStatus(Long userId, UserStatusUpdateRequest statusRequest) {
        log.info("Updating status for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Update active status
        if (statusRequest.getIsActive() != null) {
            user.setIsActive(statusRequest.getIsActive());
        }

        // Update roles
        if (statusRequest.getRoleNames() != null) {
            Set<Role> newRoles = new HashSet<>();
            for (String roleName : statusRequest.getRoleNames()) {
                Role role = roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                newRoles.add(role);
            }
            user.setRoles(newRoles);
        }

        User updatedUser = userRepository.save(user);
        log.info("User status updated successfully: {}", userId);
        return mapToUserAdminResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Soft deleting user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.markAsDeleted();
        userRepository.save(user);
        log.info("User soft deleted successfully: {}", userId);
    }

    @Override
    @Transactional
    public void restoreUser(Long userId) {
        log.info("Restoring user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setDeletedAt(null);
        userRepository.save(user);
        log.info("User restored successfully: {}", userId);
    }

    @Override
    @Transactional
    public void verifyUserEmail(Long userId) {
        log.info("Verifying email for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Email verified for user: {}", userId);
    }

    @Override
    @Transactional
    public void verifyUserPhone(Long userId) {
        log.info("Verifying phone for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setPhoneVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Phone verified for user: {}", userId);
    }

    @Override
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        log.info("Resetting password for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void changePasswordByUser(Long userId, ChangePasswordByUserRequest request) {
        log.info("User {} requests password change", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Check old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new InvalidDataException("Current password is incorrect");
        }
        // Check new password != old password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new InvalidDataException("New password must be different from the current password");
        }
        // Check confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("Confirm password does not match new password");
        }
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User {} changed password successfully", userId);
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        log.info("Fetching general user statistics");
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countActiveUsers());
        stats.put("emailVerifiedUsers", userRepository.countEmailVerifiedUsers());
        stats.put("newUsersLast30Days", userRepository.countNewUsersFromDate(thirtyDaysAgo));

        return stats;
    }

    @Override
    public Map<String, Object> getUserStatistics(Long userId) {
        log.info("Fetching statistics for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("userUuid", user.getUuid());
        stats.put("accountAge", user.getCreatedAt());
        stats.put("lastLogin", user.getLastLoginAt());
        stats.put("isActive", user.getIsActive());
        stats.put("emailVerified", user.getEmailVerifiedAt() != null);
        stats.put("phoneVerified", user.getPhoneVerifiedAt() != null);

        return stats;
    }

    // Helper methods for mapping
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = modelMapper.map(user, UserResponse.class);
        response.setFullName(user.getFullName());

        if (user.getRoles() != null) {
            Set<UserResponse.RoleResponse> roleResponses = user.getRoles().stream()
                    .map(role -> UserResponse.RoleResponse.builder()
                            .roleId(role.getRoleId())
                            .roleName(role.getRoleName())
                            .description(role.getDescription())
                            .build())
                    .collect(Collectors.toSet());
            response.setRoles(roleResponses);
        }

        return response;
    }

    private UserAdminResponse mapToUserAdminResponse(User user) {
        UserAdminResponse response = modelMapper.map(user, UserAdminResponse.class);
        response.setFullName(user.getFullName());

        if (user.getRoles() != null) {
            Set<UserAdminResponse.RoleResponse> roleResponses = user.getRoles().stream()
                    .map(role -> UserAdminResponse.RoleResponse.builder()
                            .roleId(role.getRoleId())
                            .roleName(role.getRoleName())
                            .description(role.getDescription())
                            .build())
                    .collect(Collectors.toSet());
            response.setRoles(roleResponses);
        }
        return response;
    }
}