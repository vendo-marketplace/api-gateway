package com.vendo.api_gateway.adapter.security.in.filter.exception;

public class AuthNotCompletedException extends AuthenticationException {
    public AuthNotCompletedException(String message) {
        super(message);
    }
}
