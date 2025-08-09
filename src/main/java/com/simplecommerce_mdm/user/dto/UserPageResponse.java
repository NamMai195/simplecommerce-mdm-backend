package com.simplecommerce_mdm.user.dto;

import com.simplecommerce_mdm.common.dto.PageResponseAbstract;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageResponse extends PageResponseAbstract implements Serializable {
    private List<UserResponse> users;
    private UserStatistics statistics;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatistics {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
        private Long emailVerifiedUsers;
        private Long phoneVerifiedUsers;
        private Long newUsersThisMonth;
    }
}
