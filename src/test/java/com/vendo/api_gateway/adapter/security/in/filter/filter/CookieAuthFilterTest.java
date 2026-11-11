package com.vendo.api_gateway.adapter.security.in.filter.filter;

import com.vendo.api_gateway.adapter.security.in.filter.CookieAuthFilter;
import com.vendo.api_gateway.adapter.security.in.filter.GlobalFilterUtils;
import com.vendo.api_gateway.adapter.security.in.filter.exception.BadCredentialsException;
import com.vendo.api_gateway.adapter.security.in.filter.path.SecuredAntPathResolver;
import com.vendo.api_gateway.adapter.security.out.jwt.parser.AuthenticationParser;
import com.vendo.api_gateway.domain.user.User;
import com.vendo.api_gateway.test_utils.builder.UserDataBuilder;
import com.vendo.core_lib.utils.AssertionUtils;
import com.vendo.security_lib.type.AuthHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.vendo.security_lib.http.HttpUtils.ACCESS_PREFIX;
import static com.vendo.security_lib.http.HttpUtils.AUTHORIZATION_HEADER;
import static com.vendo.security_lib.http.HttpUtils.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieAuthFilterTest {

    private static final String PATH = "/private";

    @Mock
    private AuthenticationParser claimsParser;
    @Mock
    private SecuredAntPathResolver antPathResolver;
    @Mock
    private GatewayFilterChain chain;
    @InjectMocks
    private CookieAuthFilter filter;

    @Test
    void filter_shouldAuthenticateUsingCookie() {
        String token = "token";
        User user = UserDataBuilder.withAllFields().build();
        MockServerWebExchange exchange = exchangeWithCookie(BEARER_PREFIX + token);
        when(antPathResolver.isPermittedPath(PATH)).thenReturn(false);
        when(claimsParser.extract(token)).thenReturn(user);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(claimsParser).extract(token);
        verify(chain).filter(captor.capture());
        assertThat(captor.getValue().getRequest().getHeaders().getFirst(AuthHeader.ID.getHeader()))
                .isEqualTo(user.id());
        AssertionUtils.assertFrom(Objects.requireNonNull(exchange.getAttribute(GlobalFilterUtils.CONTEXT_ATTRIBUTE)), user);
    }

    @Test
    void filter_shouldSkipAuthentication_whenCallingWhitelistedEndpoint() {
        MockServerWebExchange exchange = exchangeWithCookie(BEARER_PREFIX + "token");
        when(antPathResolver.isPermittedPath(PATH)).thenReturn(true);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        verifyNoInteractions(claimsParser);
        assertThat((Object) exchange.getAttribute(GlobalFilterUtils.CONTEXT_ATTRIBUTE)).isNull();
    }

    @Test
    void filter_shouldAuthenticateUsingRawTokenCookie() {
        User user = UserDataBuilder.withAllFields().build();
        MockServerWebExchange exchange = exchangeWithCookie("token");
        when(antPathResolver.isPermittedPath(PATH)).thenReturn(false);
        when(claimsParser.extract("token")).thenReturn(user);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(claimsParser).extract("token");
        verify(chain).filter(any());
    }

    @Test
    void filter_shouldAuthenticateUsingAuthorizationHeader_whenCookieIsMissing() {
        User user = UserDataBuilder.withAllFields().build();
        MockServerWebExchange exchange = exchangeWithHeader(BEARER_PREFIX + "header-token");
        when(antPathResolver.isPermittedPath(PATH)).thenReturn(false);
        when(claimsParser.extract("header-token")).thenReturn(user);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(claimsParser).extract("header-token");
        verify(chain).filter(any());
        AssertionUtils.assertFrom(Objects.requireNonNull(exchange.getAttribute(GlobalFilterUtils.CONTEXT_ATTRIBUTE)), user);
    }

    @Test
    void filter_shouldPreferCookieOverAuthorizationHeader() {
        User user = UserDataBuilder.withAllFields().build();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(PATH)
                        .cookie(new HttpCookie(ACCESS_PREFIX, "cookie-token"))
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "header-token")
                        .build()
        );
        when(antPathResolver.isPermittedPath(PATH)).thenReturn(false);
        when(claimsParser.extract("cookie-token")).thenReturn(user);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(claimsParser).extract("cookie-token");
    }

    @Test
    void filter_shouldRejectAuthorizationHeaderWithoutBearerPrefix() {
        MockServerWebExchange exchange = exchangeWithHeader("token");
        when(antPathResolver.isPermittedPath(PATH)).thenReturn(false);

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid or expired token.");

        verifyNoInteractions(claimsParser, chain);
    }

    @Test
    void filter_shouldRejectRequest_whenNoCookieAndNoHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get(PATH).build());
        when(antPathResolver.isPermittedPath(PATH)).thenReturn(false);

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid or expired token.");

        verifyNoInteractions(claimsParser, chain);
    }

    @Test
    void filter_shouldRejectExpiredTokenFromCookie() {
        MockServerWebExchange exchange = exchangeWithCookie(BEARER_PREFIX + "expired-token");
        when(antPathResolver.isPermittedPath(PATH)).thenReturn(false);
        when(claimsParser.extract("expired-token"))
                .thenThrow(new BadCredentialsException("Invalid or expired token."));

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid or expired token.");

        verify(claimsParser).extract("expired-token");
        verifyNoInteractions(chain);
    }

    private MockServerWebExchange exchangeWithHeader(String value) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get(PATH)
                        .header(AUTHORIZATION_HEADER, value)
                        .build()
        );
    }

    private MockServerWebExchange exchangeWithCookie(String value) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get(PATH)
                        .cookie(new HttpCookie(ACCESS_PREFIX, value))
                        .build()
        );
    }
}
