package com.vendo.api_gateway.adapter.security.in.filter.exception;

public class BadCredentialsException extends AuthenticationException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
