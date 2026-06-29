package com.vendo.api_gateway.adapter.security.in.filter.exception.handler;

import com.vendo.api_gateway.adapter.security.in.filter.exception.BadCredentialsException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy.AuthenticationExceptionResponseStrategy;
import com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy.BadCredentialsExceptionResponseStrategy;
import com.vendo.security_lib.exception.response.ExceptionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ExceptionResponseFactoryTest {

    @Test
    void getExceptionResponse_shouldReturnBadCredentialsStrategy() {
        ExceptionResponseFactory factory = new ExceptionResponseFactory(List.of(new BadCredentialsExceptionResponseStrategy()));

        ExceptionResponse exception = factory.getExceptionResponse("/api", new BadCredentialsException("Invalid or expired token."));

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Invalid or expired token.");
        assertThat(exception.getPath()).isEqualTo("/api");
        assertThat(exception.getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(exception.getErrors()).isNull();
    }

    @Test
    void getExceptionResponse_shouldReturnInternalStrategy_whenNoHandler() {
        ExceptionResponseFactory factory = new ExceptionResponseFactory(List.of());

        ExceptionResponse exception = factory.getExceptionResponse("/api", new BadCredentialsException("Invalid or expired token."));

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Internal server error.");
        assertThat(exception.getPath()).isEqualTo("/api");
        assertThat(exception.getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getErrors()).isNull();
    }

    @Test
    void getExceptionResponse_shouldReturnAuthenticationStrategyByInstance_whenNoExactHandler() {
        ExceptionResponseFactory factory = new ExceptionResponseFactory(List.of(new AuthenticationExceptionResponseStrategy()));

        ExceptionResponse exception = factory.getExceptionResponse("/api", new TestAuthenticationException("Unauthorized."));

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Unauthorized.");
        assertThat(exception.getPath()).isEqualTo("/api");
        assertThat(exception.getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(exception.getErrors()).isNull();
    }

}
