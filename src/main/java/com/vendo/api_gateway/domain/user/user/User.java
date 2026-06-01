package com.vendo.api_gateway.domain.user.user;

import com.vendo.user_lib.type.UserRole;
import com.vendo.user_lib.type.UserStatus;

import java.util.List;

public record User(
        String id,
        UserStatus status,
        List<UserRole> roles,
        boolean emailVerified
) {

    public static List<UserRole> toRoles(List<String> roles) {
        return roles.stream()
                .map(UserRole::valueOf)
                .toList();
    }

    public static List<String> toNames(List<UserRole> roles) {
        return roles.stream()
                .map(Enum::name)
                .toList();
    }

}
