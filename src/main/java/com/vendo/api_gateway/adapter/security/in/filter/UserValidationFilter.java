package com.vendo.api_gateway.adapter.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.exception.AccessDeniedException;
import com.vendo.api_gateway.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class UserValidationFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        User authUser = FilterUtils.getValueFromContext(User.class, exchange.getAttributes());
        if (authUser.isBlocked()) throw new AccessDeniedException("User is blocked.");

        return chain.filter(exchange);
    }
}
