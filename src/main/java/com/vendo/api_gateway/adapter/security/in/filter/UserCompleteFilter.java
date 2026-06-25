package com.vendo.api_gateway.adapter.security.in.filter;

import com.vendo.api_gateway.domain.user.User;
import com.vendo.api_gateway.infrastructure.props.GatewayProps;
import com.vendo.api_gateway.port.UserQueryPort;
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
@Order(2)
@Component
@RequiredArgsConstructor
public class UserCompleteFilter implements GlobalFilter {

    private final GatewayProps props;
    private final UserQueryPort userQueryPort;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!props.getVerified().getCompletePath().equals(path)) return chain.filter(exchange);

        User authUser = GlobalFilterUtils.getValueFromContext(User.class, exchange.getAttributes());
        return userQueryPort.findById(authUser.id())
                .map(user -> {
                    GlobalFilterUtils.addUserToContext(user, request.getAttributes());
                    return user;
                }).flatMap(user -> chain.filter(exchange));
    }
}
