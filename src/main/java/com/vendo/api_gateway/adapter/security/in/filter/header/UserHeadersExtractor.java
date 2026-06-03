package com.vendo.api_gateway.adapter.security.in.filter.header;

import com.vendo.api_gateway.domain.user.User;
import org.springframework.http.HttpHeaders;

import static com.vendo.core_lib.constants.Delimiters.COMMA_DELIMITER;
import static com.vendo.security_lib.type.UserHeaders.*;

public final class UserHeadersExtractor {

    public static HttpHeaders from(User user) {
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add(ID.getHeader(), user.id());
        httpHeaders.add(EMAIL.getHeader(), user.email());
        httpHeaders.add(STATUS.getHeader(), user.status().name());
        httpHeaders.add(ROLES.getHeader(), String.join(COMMA_DELIMITER, user.roles()));
        httpHeaders.add(EMAIL_VERIFIED.getHeader(), String.valueOf(user.emailVerified()));

        return httpHeaders;
    }

}
