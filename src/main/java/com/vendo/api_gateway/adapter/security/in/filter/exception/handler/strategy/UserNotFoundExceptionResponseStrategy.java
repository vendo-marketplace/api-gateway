package com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy;

import com.vendo.security_lib.exception.response.ExceptionResponse;
import com.vendo.user_lib.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
class UserNotFoundExceptionResponseStrategy implements ExceptionResponseStrategy {

    @Override
    public Class<? extends Exception> getException() {
        return UserNotFoundException.class;
    }

    @Override
    public ExceptionResponse getResponse(String path, Throwable ex) {
        return ExceptionResponse.builder()
                .path(path)
                .message("User not found.")
                .code(HttpStatus.NOT_FOUND.value())
                .build();
    }
}