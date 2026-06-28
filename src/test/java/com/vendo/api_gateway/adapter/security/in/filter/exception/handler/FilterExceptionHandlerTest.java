package com.vendo.api_gateway.adapter.security.in.filter.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AccessDeniedException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthenticationServiceException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.BadCredentialsException;
import com.vendo.security_lib.exception.response.ExceptionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static com.vendo.security_lib.http.HttpUtils.AUTHORIZATION_HEADER;
import static com.vendo.security_lib.http.HttpUtils.BEARER_PREFIX;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FilterExceptionHandlerTest {

    @InjectMocks
    private FilterExceptionHandler filterExceptionHandler;

    @Mock
    private ExceptionResponseFactory exceptionResponseFactory;

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

        AuthenticationServiceException authenticationServiceException = new AuthenticationServiceException("Unauthorized.");
        ExceptionResponse er = ExceptionResponse.builder()
                .path("/auth")
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("Unauthorized.")
                .build();

        when(exceptionResponseFactory.getExceptionResponse("/auth", authenticationServiceException)).thenReturn(er);

        filterExceptionHandler.handle(exchange, authenticationServiceException).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = exchange.getResponse()
                .getBodyAsString()
                .block();

        assertThat(body).isNotBlank();
        ExceptionResponse exceptionResponse = objectMapper.readValue(body, ExceptionResponse.class);
        assertThat(exceptionResponse.getMessage()).isEqualTo(er.getMessage());
        assertThat(exceptionResponse.getCode()).isEqualTo(er.getCode());
        assertThat(exceptionResponse.getPath()).isEqualTo(er.getPath());
    }

    @Test
    void handle_shouldReturnForbidden_whenAccessDeniedExceptionThrown() throws JsonProcessingException {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ExceptionResponse er = ExceptionResponse.builder()
                .path("/auth")
                .code(HttpStatus.FORBIDDEN.value())
                .message("Forbidden.")
                .build();
        AccessDeniedException accessDeniedException = new AccessDeniedException("Forbidden.");

        when(exceptionResponseFactory.getExceptionResponse("/auth", accessDeniedException)).thenReturn(er);

        filterExceptionHandler.handle(exchange, accessDeniedException).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String response = exchange.getResponse()
                .getBodyAsString()
                .block();
        assertThat(response).isNotBlank();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getMessage()).isEqualTo(er.getMessage());
        assertThat(exceptionResponse.getCode()).isEqualTo(er.getCode());
        assertThat(exceptionResponse.getPath()).isEqualTo(er.getPath());
    }

    @Test
    void handle_shouldReturnInternalServerError_whenNotAuthExceptionThrown() throws JsonProcessingException {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ExceptionResponse er = ExceptionResponse.builder()
                .path("/auth")
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error.")
                .build();
        Exception exception = new Exception("Internal server error.");

        when(exceptionResponseFactory.getExceptionResponse("/auth", exception)).thenReturn(er);

        filterExceptionHandler.handle(exchange, exception).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String response = exchange.getResponse().getBodyAsString().block();
        assertThat(response).isNotBlank();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getMessage()).isEqualTo(er.getMessage());
        assertThat(exceptionResponse.getCode()).isEqualTo(er.getCode());
        assertThat(exceptionResponse.getPath()).isEqualTo(er.getPath());
    }

    @Test
    void handle_shouldReturnUnauthorized_whenBadCredentialsExceptionThrown() throws JsonProcessingException {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ExceptionResponse er = ExceptionResponse.builder()
                .path("/auth")
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid or expired token.")
                .build();
        BadCredentialsException exception = new BadCredentialsException("Invalid or expired token.");

        when(exceptionResponseFactory.getExceptionResponse("/auth", exception)).thenReturn(er);

        filterExceptionHandler.handle(exchange, exception).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String response = exchange.getResponse().getBodyAsString().block();
        assertThat(response).isNotBlank();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getMessage()).isEqualTo(er.getMessage());
        assertThat(exceptionResponse.getCode()).isEqualTo(er.getCode());
        assertThat(exceptionResponse.getPath()).isEqualTo(er.getPath());
    }

    @Test
    void handle_shouldReturnFallbackResponse_whenJsonProcessingExceptionThrown() throws JsonProcessingException {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsBytes(any());

        filterExceptionHandler
                .handle(exchange, new AccessDeniedException("Forbidden."))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String response = exchange.getResponse().getBodyAsString().block();
        assertThat(response).isNotBlank();

        assertThat(response).contains("Internal server error.");
        assertThat(response).contains("500");
    }
}
