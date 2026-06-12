package com.vendo.api_gateway.adapter.security.in.filter.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
