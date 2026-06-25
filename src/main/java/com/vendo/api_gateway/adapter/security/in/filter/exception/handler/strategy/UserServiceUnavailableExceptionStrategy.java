package com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy;

import com.vendo.api_gateway.adapter.user.out.exception.UserServiceUnavailableException;
import com.vendo.security_lib.exception.response.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class UserServiceUnavailableExceptionStrategy implements ExceptionResponseStrategy {

    @Override
    public Class<? extends Exception> getException() {
        return UserServiceUnavailableException.class;
    }

    @Override
    public ExceptionResponse getResponse(String path) {
        return ExceptionResponse.builder()
                .path(path)
                .message("Service is unavailable.")
                .code(HttpStatus.SERVICE_UNAVAILABLE.value())
                .build();
    }
}
