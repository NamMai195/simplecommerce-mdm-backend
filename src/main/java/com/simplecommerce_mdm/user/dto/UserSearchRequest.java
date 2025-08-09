package com.simplecommerce_mdm.user.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {

    @Parameter(description = "Search term for user name, email or phone")
    private String searchTerm;

    @Parameter(description = "Filter by role name (USER, SELLER, ADMIN)")
    private String roleName;

    @Parameter(description = "Filter by active status")
    private Boolean isActive;

    @Parameter(description = "Filter by email verification status")
    private Boolean emailVerified;

    @Parameter(description = "Filter by phone verification status")
    private Boolean phoneVerified;

    @Parameter(description = "Page number (default 0)")
    @Min(value = 0, message = "Page number must be >= 0")
    @Builder.Default
    private Integer page = 0;

    @Parameter(description = "Page size (default 10, max 100)")
    @Min(value = 1, message = "Page size must be >= 1")
    @Max(value = 100, message = "Page size must be <= 100")
    @Builder.Default
    private Integer size = 10;

    @Parameter(description = "Sort field (default createdAt)")
    @Builder.Default
    private String sortBy = "createdAt";

    @Parameter(description = "Sort direction (asc/desc, default desc)")
    @Builder.Default
    private String sortDirection = "desc";
}
