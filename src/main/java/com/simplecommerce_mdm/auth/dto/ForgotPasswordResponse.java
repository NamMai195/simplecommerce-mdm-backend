package com.simplecommerce_mdm.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for forgot password")
public class ForgotPasswordResponse {

    @Schema(description = "Email address where OTP was sent", example = "user@example.com")
    private String email;

    @Schema(description = "Message indicating OTP was sent", example = "OTP code has been sent to your email")
    private String message;

    @Schema(description = "OTP expiration time", example = "2024-01-20T15:30:00")
    private LocalDateTime expiresAt;

    @Schema(description = "Time remaining in minutes", example = "10")
    private Integer expiresInMinutes;
}