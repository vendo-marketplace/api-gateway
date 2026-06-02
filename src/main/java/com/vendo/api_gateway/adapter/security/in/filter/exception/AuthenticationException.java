package com.vendo.api_gateway.adapter.security.in.filter.exception;

public abstract class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
