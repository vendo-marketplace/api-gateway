package com.vendo.api_gateway.adapter.security.out.jwt.parser;

import com.vendo.api_gateway.domain.user.User;

public interface AuthenticationParser {

    User extract(String token);

}
