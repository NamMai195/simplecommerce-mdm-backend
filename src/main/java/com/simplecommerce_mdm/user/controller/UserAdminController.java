package com.simplecommerce_mdm.user.controller;

import com.simplecommerce_mdm.common.dto.ApiResponse;
import com.simplecommerce_mdm.config.CustomUserDetails;
import com.simplecommerce_mdm.user.dto.*;
import com.simplecommerce_mdm.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Admin Controller", description = "Admin APIs for user management")
@Slf4j(topic = "USER-ADMIN-CONTROLLER")
@SecurityRequirement(name = "bearerAuth")
public class UserAdminController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get All Users", description = "Get all users with filtering, sorting and pagination for admin management")
    public ResponseEntity<ApiResponse<Page<UserAdminResponse>>> getAllUsers(
            @Parameter(description = "Search term for user name, email or phone") @RequestParam(defaultValue = "") String searchTerm,

            @Parameter(description = "Filter by role name (USER, SELLER, ADMIN)") @RequestParam(required = false) String roleName,

            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,

            @Parameter(description = "Filter by email verification status") @RequestParam(required = false) Boolean emailVerified,

            @Parameter(description = "Filter by phone verification status") @RequestParam(required = false) Boolean phoneVerified,

            @Parameter(description = "Page number (default 0)") @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Page size (default 20, max 100)") @RequestParam(defaultValue = "20") Integer size,

            @Parameter(description = "Sort field (default createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc/desc, default desc)") @RequestParam(defaultValue = "desc") String sortDirection,

            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} fetching users with filters: search='{}', role={}, active={}",
                userDetails.getUser().getEmail(), searchTerm, roleName, isActive);

        UserSearchRequest searchRequest = UserSearchRequest.builder()
                .searchTerm(searchTerm)
                .roleName(roleName)
                .isActive(isActive)
                .emailVerified(emailVerified)
                .phoneVerified(phoneVerified)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<UserAdminResponse> users = userService.getAllUsers(searchRequest);

        ApiResponse<Page<UserAdminResponse>> response = ApiResponse.<Page<UserAdminResponse>>builder()
                .message("Users retrieved successfully")
                .data(users)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get User by ID", description = "Get detailed user information by user ID")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} fetching user by ID: {}", userDetails.getUser().getEmail(), userId);

        UserAdminResponse user = userService.getUserById(userId);

        ApiResponse<UserAdminResponse> response = ApiResponse.<UserAdminResponse>builder()
                .message("User retrieved successfully")
                .data(user)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Get User by UUID", description = "Get detailed user information by user UUID")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUserByUuid(
            @Parameter(description = "User UUID") @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} fetching user by UUID: {}", userDetails.getUser().getEmail(), uuid);

        UserAdminResponse user = userService.getUserByUuid(uuid);

        ApiResponse<UserAdminResponse> response = ApiResponse.<UserAdminResponse>builder()
                .message("User retrieved successfully")
                .data(user)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create New User", description = "Create a new user account with specified roles")
    public ResponseEntity<ApiResponse<UserAdminResponse>> createUser(
            @Valid @RequestBody UserCreateRequest createRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} creating new user with email: {}",
                userDetails.getUser().getEmail(), createRequest.getEmail());

        UserAdminResponse createdUser = userService.createUser(createRequest);

        ApiResponse<UserAdminResponse> response = ApiResponse.<UserAdminResponse>builder()
                .message("User created successfully")
                .data(createdUser)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update User", description = "Update user information")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest updateRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} updating user ID: {}", userDetails.getUser().getEmail(), userId);

        UserAdminResponse updatedUser = userService.updateUser(userId, updateRequest);

        ApiResponse<UserAdminResponse> response = ApiResponse.<UserAdminResponse>builder()
                .message("User updated successfully")
                .data(updatedUser)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Update User Status", description = "Update user active status and roles")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequest statusRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} updating status for user ID: {}", userDetails.getUser().getEmail(), userId);

        UserAdminResponse updatedUser = userService.updateUserStatus(userId, statusRequest);

        ApiResponse<UserAdminResponse> response = ApiResponse.<UserAdminResponse>builder()
                .message("User status updated successfully")
                .data(updatedUser)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete User", description = "Soft delete a user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} deleting user ID: {}", userDetails.getUser().getEmail(), userId);

        userService.deleteUser(userId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("User deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/restore")
    @Operation(summary = "Restore User", description = "Restore a soft-deleted user account")
    public ResponseEntity<ApiResponse<Void>> restoreUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} restoring user ID: {}", userDetails.getUser().getEmail(), userId);

        userService.restoreUser(userId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("User restored successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/verify-email")
    @Operation(summary = "Verify User Email", description = "Mark user email as verified")
    public ResponseEntity<ApiResponse<Void>> verifyUserEmail(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} verifying email for user ID: {}", userDetails.getUser().getEmail(), userId);

        userService.verifyUserEmail(userId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("User email verified successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/verify-phone")
    @Operation(summary = "Verify User Phone", description = "Mark user phone as verified")
    public ResponseEntity<ApiResponse<Void>> verifyUserPhone(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} verifying phone for user ID: {}", userDetails.getUser().getEmail(), userId);

        userService.verifyUserPhone(userId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("User phone verified successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/reset-password")
    @Operation(summary = "Reset User Password", description = "Reset user password to a new value")
    public ResponseEntity<ApiResponse<Void>> resetUserPassword(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "New password") @RequestParam String newPassword,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} resetting password for user ID: {}", userDetails.getUser().getEmail(), userId);

        userService.resetUserPassword(userId, newPassword);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("User password reset successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get User Statistics", description = "Get general user statistics for admin dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} fetching user statistics", userDetails.getUser().getEmail());

        Map<String, Object> statistics = userService.getUserStatistics();

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .message("User statistics retrieved successfully")
                .data(statistics)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/statistics")
    @Operation(summary = "Get User Statistics by ID", description = "Get specific user statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatisticsById(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} fetching statistics for user ID: {}", userDetails.getUser().getEmail(), userId);

        Map<String, Object> statistics = userService.getUserStatistics(userId);

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .message("User statistics retrieved successfully")
                .data(statistics)
                .build();

        return ResponseEntity.ok(response);
    }
}