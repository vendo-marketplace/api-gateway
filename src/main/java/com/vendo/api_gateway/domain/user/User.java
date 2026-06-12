package com.vendo.api_gateway.domain.user;

import com.vendo.user_lib.type.UserRole;
import com.vendo.user_lib.type.UserStatus;
import lombok.Builder;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record User(
        String id,
        String email,
        UserStatus status,
        Set<UserRole> roles,
        boolean emailVerified
) {

    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }

    public Set<String> toRoleNames() {
        return roles.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }


}
