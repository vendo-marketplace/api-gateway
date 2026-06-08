package com.vendo.api_gateway.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.GlobalFilterUtils;
import com.vendo.api_gateway.adapter.security.in.filter.UserAuthFilter;
import com.vendo.api_gateway.adapter.security.in.filter.exception.BadCredentialsException;
import com.vendo.api_gateway.adapter.security.in.filter.path.SecuredAntPathResolver;
import com.vendo.api_gateway.adapter.security.out.jwt.parser.AuthenticationParser;
import com.vendo.api_gateway.domain.user.User;
import com.vendo.api_gateway.test_utils.builder.UserDataBuilder;
import com.vendo.core_lib.utils.AssertionUtils;
import com.vendo.security_lib.type.UserHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

import static com.vendo.core_lib.constants.Delimiters.COMMA_DELIMITER;
import static com.vendo.security_lib.http.HttpUtils.AUTHORIZATION_HEADER;
import static com.vendo.security_lib.http.HttpUtils.BEARER_PREFIX;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAuthFilterTest {

    @Mock
    private AuthenticationParser claimsParser;

    @Mock
    private SecuredAntPathResolver antPathResolver;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private UserAuthFilter filter;

    @Test
    void filter_shouldSuccessfullyFilter() {
        String token = "token";
        User user = UserDataBuilder.withAllFields().build();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(antPathResolver.isPermittedPath("/auth")).thenReturn(false);
        when(claimsParser.extract("token")).thenReturn(user);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(claimsParser).extract("token");
        verify(chain).filter(captor.capture());

        Object contextUser = exchange.getAttribute(GlobalFilterUtils.CONTEXT_ATTRIBUTE);
        AssertionUtils.assertFrom(Objects.requireNonNull(contextUser), user);

        ServerWebExchange captorValue = captor.getValue();
        assertThat(captorValue).isNotNull();
        Map<String, String> headers = captorValue.getRequest().getHeaders().asSingleValueMap();
        assertThat(headers).isNotNull();
        assertThat(headers.get(UserHeader.ID.getHeader())).isEqualTo(user.id());
        assertThat(headers.get(UserHeader.EMAIL.getHeader())).isEqualTo(user.email());
        assertThat(headers.get(UserHeader.STATUS.getHeader())).isEqualTo(user.status().name());
        assertThat(headers.get(UserHeader.EMAIL_VERIFIED.getHeader())).isEqualTo(String.valueOf(user.emailVerified()));
        assertThat(headers.get(UserHeader.ROLES.getHeader())).isEqualTo(String.join(COMMA_DELIMITER, user.roles()));
    }

    @Test
    void filter_shouldSkipFiltering_whenCallingWhitelistedEndpoint() {
        String token = "token";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(antPathResolver.isPermittedPath("/auth")).thenReturn(true);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        verifyNoInteractions(claimsParser);
    }

    @Test
    void filter_shouldThrowBadCredentialsException_whenNoTokenInRequest() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(antPathResolver.isPermittedPath("/auth")).thenReturn(false);

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Unauthorized.");

        verifyNoInteractions(claimsParser, chain);
    }

    @Test
    void filter_shouldThrowBadCredentials_whenTokenWithoutBearerPrefix() {
        String token = "token";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, token)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(antPathResolver.isPermittedPath("/auth")).thenReturn(false);

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Unauthorized.");

        verifyNoInteractions(claimsParser, chain);
    }

    @Test
    void filter_shouldThrowBadCredentials_whenTokenExpired() {
        String expiredToken = "token";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth")
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + expiredToken)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(antPathResolver.isPermittedPath("/auth")).thenReturn(false);
        when(claimsParser.extract("token")).thenThrow(new BadCredentialsException("Token expired."));

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Token expired.");

        verify(claimsParser).extract(expiredToken);
        verifyNoInteractions(chain);
    }
}
