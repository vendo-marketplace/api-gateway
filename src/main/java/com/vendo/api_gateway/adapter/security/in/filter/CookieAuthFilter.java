package com.vendo.api_gateway.adapter.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.path.SecuredAntPathResolver;
import com.vendo.api_gateway.adapter.security.out.jwt.parser.AuthenticationParser;
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

import static com.vendo.security_lib.http.HttpUtils.*;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class CookieAuthFilter implements GlobalFilter {

    private final AuthenticationParser claimsParser;
    private final SecuredAntPathResolver antPathResolver;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        antPathResolver.isPermittedPath(path);
        if (antPathResolver.isPermittedPath(path)) return chain.filter(exchange);

        String authorization = GlobalFilterUtils.getValueFromCookies(ACCESS_PREFIX, request.getCookies());
        System.out.println("auth " + authorization);
        User authUser = claimsParser.extract(authorization);
        GlobalFilterUtils.addUserToContext(authUser, exchange.getAttributes());

        ServerHttpRequest requestWithHeader = applyHeader(authorization, request);
        return chain.filter(exchange
                .mutate()
                .request(requestWithHeader)
                .build()
        );
    }

    private ServerHttpRequest applyHeader(String authorization, ServerHttpRequest request) {
        return request.mutate()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX.concat(authorization))
                .build();
    }
}
