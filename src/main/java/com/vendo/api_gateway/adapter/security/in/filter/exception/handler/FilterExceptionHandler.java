package com.vendo.api_gateway.adapter.security.in.filter.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendo.api_gateway.adapter.security.out.filter.FilterUtils;
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
    private final ExceptionResponseFactory exceptionResponseFactory;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Handling filter exception: {}", ex.getMessage());
        ServerHttpResponse httpResponse = exchange.getResponse();
        httpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            ExceptionResponse exResponse = exceptionResponseFactory.getExceptionResponse(exchange.getRequest().getURI().getPath(), ex);
            byte[] responseBytes = objectMapper.writeValueAsBytes(exResponse);

            httpResponse.setStatusCode(HttpStatusCode.valueOf(exResponse.getCode()));
            return httpResponse.writeWith(Mono.just(httpResponse.bufferFactory().wrap(responseBytes)));
        } catch (Exception e) {
            log.error("Internal error while handling exception: {}", e.getMessage());
            return writeInternalException(httpResponse);
        }
    }

    private Mono<Void> writeInternalException(ServerHttpResponse httpResponse) {
        httpResponse.setStatusCode(HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        String response = FilterUtils.RESPONSE_MESSAGE_TEMPLATE.formatted(
                "Internal server error.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                Instant.now().toString()
        );

        return httpResponse.writeWith(Mono.just(httpResponse.bufferFactory().wrap(response.getBytes(StandardCharsets.UTF_8))));
    }
}
