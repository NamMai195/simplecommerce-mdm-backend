package com.simplecommerce_mdm.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for user registration")
public class RegisterResponse {
    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @Schema(description = "Success message", example = "Registration successful. Please check your email for verification OTP.")
    private String message;

    @Schema(description = "OTP expiration time")
    private LocalDateTime expiresAt;

    @Schema(description = "OTP validity in minutes", example = "10")
    private Integer expiresInMinutes;

    @Schema(description = "User UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userUuid;
}
