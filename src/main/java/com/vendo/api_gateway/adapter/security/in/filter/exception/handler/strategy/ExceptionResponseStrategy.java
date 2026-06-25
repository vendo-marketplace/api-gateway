package com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy;

import com.vendo.security_lib.exception.response.ExceptionResponse;

public interface ExceptionResponseStrategy {

    Class<? extends Exception> getException();

    ExceptionResponse getResponse(String path);

}
