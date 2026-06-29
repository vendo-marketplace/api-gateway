package com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy;

import com.vendo.api_gateway.adapter.security.in.filter.exception.AccessDeniedException;
import com.vendo.security_lib.exception.response.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AccessDeniedExceptionResponseStrategy implements ExceptionResponseStrategy {

    @Override
    public Class<? extends Exception> getException() {
        return AccessDeniedException.class;
    }

    @Override
    public ExceptionResponse getResponse(String path, Throwable ex) {
        return ExceptionResponse.builder()
                .path(path)
                .code(HttpStatus.FORBIDDEN.value())
                .message("Forbidden.")
                .build();
    }
}