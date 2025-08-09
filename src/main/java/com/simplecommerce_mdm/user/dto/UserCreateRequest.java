package com.simplecommerce_mdm.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new user")
public class UserCreateRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    @Schema(description = "User's phone number", example = "0123456789")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character")
    @Schema(description = "User's password", example = "StrongPass123!")
    private String password;

    @Schema(description = "User's avatar URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Builder.Default
    @Schema(description = "Whether the user is active", example = "true")
    private Boolean isActive = true;

    @Schema(description = "Set of role names to assign to the user", example = "[\"USER\"]")
    private Set<String> roleNames;

    @Schema(description = "Additional metadata in JSON format", example = "{\"preferences\": \"dark_mode\"}")
    private String metadata;
}