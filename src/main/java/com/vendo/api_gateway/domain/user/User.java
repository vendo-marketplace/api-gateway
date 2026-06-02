package com.vendo.api_gateway.domain.user;

import com.vendo.user_lib.exception.UserBlockedException;
import com.vendo.user_lib.type.UserStatus;

import java.util.List;

public record User(
        String id,
        String email,
        UserStatus status,
        List<String> roles,
        boolean emailVerified
) {

    public void throwIfBlocked() {
        if (status == UserStatus.BLOCKED) throw new UserBlockedException("User is blocked.");
    }

}
