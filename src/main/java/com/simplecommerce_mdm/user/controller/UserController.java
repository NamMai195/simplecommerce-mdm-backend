package com.simplecommerce_mdm.user.controller;

import com.simplecommerce_mdm.user.dto.UserResponse;
import com.simplecommerce_mdm.user.dto.UserUpdateRequest;
import com.simplecommerce_mdm.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "APIs for user profile management")
@Slf4j(topic = "USER-CONTROLLER")
@SecurityRequirement(name = "bearerAuth") // Yêu cầu xác thực Bearer Token cho toàn bộ controller này
public class UserController {

    private final UserService userService;

    // Helper method to get authenticated user ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.simplecommerce_mdm.config.CustomUserDetails customUserDetails) {
            return customUserDetails.getUser().getId();
        }
        // Ném ngoại lệ nếu không tìm thấy người dùng đã xác thực
        throw new IllegalStateException("User not authenticated or user ID not found in security context.");
    }

    @Operation(summary = "Get current user profile", description = "Retrieve personal information of the authenticated user.")
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile() {
        Long userId = getCurrentUserId();
        log.info("Request to get profile for current user ID: {}", userId);
        UserResponse userProfile = userService.getLoggedInUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @Operation(summary = "Update current user profile", description = "Update personal information of the authenticated user, including avatar upload.")
    @PutMapping(value = "/profile", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<UserResponse> updateProfile(
            @RequestPart("profile") @Valid UserUpdateRequest updateRequest,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        Long userId = getCurrentUserId();
        log.info("Request to update profile for current user ID: {}", userId);
        UserResponse updatedProfile = userService.updateProfile(userId, updateRequest, avatarFile);
        return ResponseEntity.ok(updatedProfile);
    }
}