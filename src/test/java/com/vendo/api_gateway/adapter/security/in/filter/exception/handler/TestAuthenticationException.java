package com.vendo.api_gateway.adapter.security.in.filter.exception.handler;

import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthenticationException;

public class TestAuthenticationException extends AuthenticationException {
    public TestAuthenticationException(String message) {
        super(message);
    }
}
