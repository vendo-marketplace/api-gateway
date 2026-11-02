package com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy;

import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthNotCompletedException;
import com.vendo.security_lib.exception.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthNotCompletedExceptionResponseStrategy implements ExceptionResponseStrategy {

    @Override
    public Class<? extends Exception> getException() {
        return AuthNotCompletedException.class;
    }

    @Override
    public ExceptionResponse getResponse(String path, Throwable ex) {
        return ExceptionResponse.builder()
                .path(path)
                .code(HttpStatus.FORBIDDEN.value())
                .message("User profile is not completed.")
                .build();
    }
}
