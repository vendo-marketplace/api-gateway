package com.vendo.api_gateway.adapter.security.in.filter.exception;

public class AuthNotVerifiedException extends AuthenticationException {
    public AuthNotVerifiedException(String message) {
        super(message);
    }
}
