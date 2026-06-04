package com.vendo.api_gateway.security.in.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AccessDeniedException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthenticationServiceException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.handler.FilterExceptionHandler;
import com.vendo.security_lib.exception.response.ExceptionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static com.vendo.security_lib.constants.AuthConstants.AUTHORIZATION_HEADER;
import static com.vendo.security_lib.constants.AuthConstants.BEARER_PREFIX;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class FilterExceptionHandlerTest {

    @InjectMocks
    private FilterExceptionHandler filterExceptionHandler;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @Test
    void handle_shouldReturnUnauthorized_whenAuthenticationExceptionThrown() throws JsonProcessingException {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filterExceptionHandler
                .handle(exchange, new AuthenticationServiceException("Unauthorized."))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = exchange.getResponse()
                .getBodyAsString()
                .block();

        assertThat(body).isNotBlank();
        ExceptionResponse exceptionResponse = objectMapper.readValue(body, ExceptionResponse.class);
        assertThat(exceptionResponse.getMessage()).isEqualTo("Unauthorized.");
        assertThat(exceptionResponse.getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(exceptionResponse.getPath()).isEqualTo("/auth");
    }

    @Test
    void handle_shouldReturnForbidden_whenAccessDeniedExceptionThrown() throws JsonProcessingException {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filterExceptionHandler
                .handle(exchange, new AccessDeniedException("Forbidden."))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String response = exchange.getResponse()
                .getBodyAsString()
                .block();
        assertThat(response).isNotBlank();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getMessage()).isEqualTo("Forbidden.");
        assertThat(exceptionResponse.getCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(exceptionResponse.getPath()).isEqualTo("/auth");
    }

    @Test
    void handle_shouldReturnInternalServerError_whenNotAuthExceptionThrown() throws JsonProcessingException {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filterExceptionHandler
                .handle(exchange, new Exception("Something went wrong."))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String response = exchange.getResponse().getBodyAsString().block();
        assertThat(response).isNotBlank();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getMessage()).isEqualTo("Internal server error.");
        assertThat(exceptionResponse.getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exceptionResponse.getPath()).isEqualTo("/auth");
    }
}
