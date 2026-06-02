package com.vendo.api_gateway.domain.user;

import com.vendo.user_lib.type.UserStatus;

import java.util.List;

public record User(
        String id,
        String email,
        UserStatus status,
        List<String> roles,
        boolean emailVerified
) {

    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }

}
