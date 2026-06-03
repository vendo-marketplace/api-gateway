package com.vendo.api_gateway.adapter.security.in.filter.exception.handler;

import com.vendo.api_gateway.adapter.security.in.filter.exception.AccessDeniedException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthenticationException;
import com.vendo.security_lib.exception.response.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
final class FilterExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Handling filter exception: {}.", ex.getMessage());

        String path = exchange.getRequest().getPath().contextPath().value();
        ServerHttpResponse httpResponse = exchange.getResponse();
        ExceptionResponse exResponse = resolve(path, ex);

        httpResponse.setStatusCode(HttpStatusCode.valueOf(exResponse.getCode()));
        httpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body = Objects.requireNonNull(SerializationUtils.serialize(exResponse));
        DataBuffer buffer = httpResponse.bufferFactory().wrap(body);
        return httpResponse.writeWith(Mono.just(buffer));
    }

    private ExceptionResponse resolve(String path, Throwable ex) {
        ExceptionResponse.Builder exBuilder = ExceptionResponse.builder().path(path);

        if (ex instanceof AuthenticationException) {
            return exBuilder
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized.")
                    .build();
        } else if (ex instanceof AccessDeniedException) {
            return exBuilder
                    .code(HttpStatus.FORBIDDEN.value())
                    .message("Forbidden.")
                    .build();
        }

        return exBuilder
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error.")
                .build();
    }
}
