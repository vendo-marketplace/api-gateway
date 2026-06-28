package com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy;

import com.vendo.security_lib.exception.response.ExceptionResponse;
import org.springframework.http.HttpStatus;

public class InternalExceptionResponseStrategy implements ExceptionResponseStrategy {

    @Override
    public Class<? extends Exception> getException() {
        return Exception.class;
    }

    @Override
    public ExceptionResponse getResponse(String path, Throwable ex) {
        return ExceptionResponse.builder()
                .path(path)
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error.")
                .build();
    }
}
