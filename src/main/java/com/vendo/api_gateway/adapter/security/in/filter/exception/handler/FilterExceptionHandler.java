package com.vendo.api_gateway.adapter.security.in.filter.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AccessDeniedException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthNotVerifiedException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthenticationException;
import com.vendo.security_lib.exception.response.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public final class FilterExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    private static final int INTERNAL_CODE = 500;

    private static final String RESPONSE_MESSAGE_TEMPLATE = """
        {
            "message": "%s",
            "code": %d,
            "timestamp": "%s"
        }
        """;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Handling filter exception: {}", ex.getMessage());
        ServerHttpResponse httpResponse = exchange.getResponse();
        httpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            ExceptionResponse exResponse = resolveResponse(exchange.getRequest().getURI().getPath(), ex);
            byte[] responseBytes = objectMapper.writeValueAsBytes(exResponse);

            httpResponse.setStatusCode(HttpStatusCode.valueOf(exResponse.getCode()));
            return httpResponse.writeWith(Mono.just(httpResponse.bufferFactory().wrap(responseBytes)));
        } catch (Exception e) {
            log.error("Internal error while handling exception: {}", e.getMessage());

            httpResponse.setStatusCode(HttpStatusCode.valueOf(INTERNAL_CODE));
            String response = RESPONSE_MESSAGE_TEMPLATE.formatted("Internal server error.", INTERNAL_CODE, Instant.now().toString());

            return httpResponse.writeWith(Mono.just(httpResponse.bufferFactory().wrap(response.getBytes(StandardCharsets.UTF_8))));
        }
    }


    private ExceptionResponse resolveResponse(String path, Throwable ex) {
        ExceptionResponse.Builder exBuilder = ExceptionResponse.builder().path(path);

        if (ex instanceof AuthNotVerifiedException) {
            return exBuilder
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("User email is not verified.")
                    .build();
        } else if (ex instanceof AuthenticationException) {
            return exBuilder
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized.")
                    .build();
        } else if (ex instanceof AccessDeniedException) {
            return exBuilder
                    .code(HttpStatus.FORBIDDEN.value())
                    .message("Forbidden.")
                    .build();
        } else {
            return exBuilder
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Internal server error.")
                    .build();
        }
    }
}
