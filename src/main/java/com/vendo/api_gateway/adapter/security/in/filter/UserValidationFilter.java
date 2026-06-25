package com.vendo.api_gateway.adapter.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.exception.AccessDeniedException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthNotVerifiedException;
import com.vendo.api_gateway.adapter.security.in.filter.path.SecuredAntPathResolver;
import com.vendo.api_gateway.adapter.security.in.filter.path.UserVerificationPathResolver;
import com.vendo.api_gateway.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Order(3)
@Component
@RequiredArgsConstructor
public class UserValidationFilter implements GlobalFilter {

    private final SecuredAntPathResolver securedAntPathResolver;
    private final UserVerificationPathResolver userVerificationPathResolver;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();
        if (securedAntPathResolver.isPermittedPath(path)) return chain.filter(exchange);

        User authUser = GlobalFilterUtils.getValueFromContext(User.class, exchange.getAttributes());
        if (authUser.isBlocked()) {
            throw new AccessDeniedException("User is blocked.");
        }

        if (!userVerificationPathResolver.isPermittedPath(path) && !authUser.emailVerified()) {
            throw new AuthNotVerifiedException("User email is not verified.");
        }

        return chain.filter(exchange);
    }
}
