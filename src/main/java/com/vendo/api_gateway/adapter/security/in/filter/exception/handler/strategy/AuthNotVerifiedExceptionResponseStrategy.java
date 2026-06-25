package com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy;

import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthNotVerifiedException;
import com.vendo.security_lib.exception.response.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthNotVerifiedExceptionResponseStrategy implements ExceptionResponseStrategy {
    @Override
    public Class<? extends Exception> getException() {
        return AuthNotVerifiedException.class;
    }

    @Override
    public ExceptionResponse getResponse(String path) {
        return ExceptionResponse.builder()
                .path(path)
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("User email is not verified.")
                .build();
    }
}
