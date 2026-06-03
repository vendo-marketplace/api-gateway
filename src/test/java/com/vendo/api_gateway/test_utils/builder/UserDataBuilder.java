package com.vendo.api_gateway.test_utils.builder;

import com.vendo.api_gateway.domain.user.User;
import com.vendo.user_lib.type.UserRole;
import com.vendo.user_lib.type.UserStatus;

import java.util.List;

public class UserDataBuilder {

    public static User.UserBuilder withAllFields() {
        return User.builder()
                .id("id")
                .email("email")
                .emailVerified(true)
                .roles(List.of(UserRole.USER.name()))
                .status(UserStatus.ACTIVE);
    }

}
