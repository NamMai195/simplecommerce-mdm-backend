package com.simplecommerce_mdm.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating user status and roles")
public class UserStatusUpdateRequest {

    @Schema(description = "Whether the user is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Set of role names to assign to the user", example = "[\"USER\", \"SELLER\"]")
    private Set<String> roleNames;

    @Schema(description = "Reason for status change", example = "User requested account deactivation")
    private String reason;
}