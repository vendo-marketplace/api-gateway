package com.vendo.api_gateway.adapter.security.in.filter.exception.handler;

import com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy.ExceptionResponseStrategy;
import com.vendo.api_gateway.adapter.security.in.filter.exception.handler.strategy.InternalExceptionResponseStrategy;
import com.vendo.security_lib.exception.response.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public final class ExceptionResponseFactory {

    private final List<ExceptionResponseStrategy> strategies;

    ExceptionResponse getExceptionResponse(String path, Throwable exception) {
        Optional<ExceptionResponseStrategy> exactStrategy = strategies.stream()
                .filter(strategy -> strategy.getException().equals(exception.getClass()))
                .findFirst();

        if (exactStrategy.isPresent()) {
            return exactStrategy.get().getResponse(path);
        }

        return getFallbackStrategy(exception).getResponse(path);
    }

    private ExceptionResponseStrategy getFallbackStrategy(Throwable exception) {
        return strategies.stream()
                .filter(strategy -> strategy.getException().isInstance(exception.getClass()))
                .findFirst()
                .orElse(new InternalExceptionResponseStrategy());
    }
}
