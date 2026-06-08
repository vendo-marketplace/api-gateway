package com.vendo.api_gateway.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.UserValidationFilter;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AccessDeniedException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthNotVerifiedException;
import com.vendo.api_gateway.adapter.security.in.filter.path.SecuredAntPathResolver;
import com.vendo.api_gateway.adapter.security.in.filter.path.UserVerificationPathResolver;
import com.vendo.api_gateway.domain.user.User;
import com.vendo.api_gateway.test_utils.builder.UserDataBuilder;
import com.vendo.user_lib.type.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static com.vendo.api_gateway.adapter.security.in.filter.GlobalFilterUtils.CONTEXT_ATTRIBUTE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserValidationTest {

    private static final String path = "/auth";
    @Mock
    private GatewayFilterChain chain;
    @Mock
    private SecuredAntPathResolver securedAntPathResolver;
    @Mock
    private UserVerificationPathResolver userVerificationPathResolver;
    @InjectMocks
    private UserValidationFilter filter;

    @Test
    void filter_shouldSuccessfullyFilter() {
        User user = UserDataBuilder.withAllFields().build();
        MockServerHttpRequest request = MockServerHttpRequest
                .get(path)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put(CONTEXT_ATTRIBUTE, user);

        when(securedAntPathResolver.isPermittedPath(path)).thenReturn(false);
        when(userVerificationPathResolver.isPermittedPath(path)).thenReturn(false);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(securedAntPathResolver).isPermittedPath(path);
        verify(userVerificationPathResolver).isPermittedPath(path);
        verify(chain).filter(any());

    }

    @Test
    void filter_shouldThrowAccessDenied_whenUserBlocked() {
        User user = UserDataBuilder.withAllFields().status(UserStatus.BLOCKED).build();
        MockServerHttpRequest request = MockServerHttpRequest
                .get(path)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put(CONTEXT_ATTRIBUTE, user);

        when(securedAntPathResolver.isPermittedPath(path)).thenReturn(false);

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("User is blocked.");

        verify(securedAntPathResolver).isPermittedPath(path);
        verifyNoInteractions(chain, userVerificationPathResolver);
    }

    @Test
    void filter_shouldThrowAccessDenied_whenPathNotPermittedAndEmailNotVerified() {
        User user = UserDataBuilder.withAllFields().emailVerified(false).build();
        MockServerHttpRequest request = MockServerHttpRequest
                .get(path)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put(CONTEXT_ATTRIBUTE, user);

        when(securedAntPathResolver.isPermittedPath(path)).thenReturn(false);
        when(userVerificationPathResolver.isPermittedPath(path)).thenReturn(false);

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(AuthNotVerifiedException.class)
                .hasMessage("User email is not verified.");

        verify(securedAntPathResolver).isPermittedPath(path);
        verify(userVerificationPathResolver).isPermittedPath(path);
        verifyNoInteractions(chain);
    }

    @Test
    void filter_shouldSuccessfullyFilter_whenPathPermittedAndEmailNotVerified() {
        User user = UserDataBuilder.withAllFields().emailVerified(false).build();
        MockServerHttpRequest request = MockServerHttpRequest
                .get(path)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put(CONTEXT_ATTRIBUTE, user);

        when(securedAntPathResolver.isPermittedPath(path)).thenReturn(false);
        when(userVerificationPathResolver.isPermittedPath(path)).thenReturn(true);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(securedAntPathResolver).isPermittedPath(path);
        verify(userVerificationPathResolver).isPermittedPath(path);
        verify(chain).filter(any());
    }


    @Test
    void filter_shouldSkipFiltering_whenCallingWhitelistedEndpoint() {
        User user = UserDataBuilder.withAllFields().build();
        MockServerHttpRequest request = MockServerHttpRequest
                .get(path)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put(CONTEXT_ATTRIBUTE, user);

        when(securedAntPathResolver.isPermittedPath(path)).thenReturn(true);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(securedAntPathResolver).isPermittedPath(path);
        verify(chain).filter(any());
    }
}
