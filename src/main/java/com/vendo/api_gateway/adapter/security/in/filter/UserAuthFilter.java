package com.vendo.api_gateway.adapter.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.header.UserHeadersExtractor;
import com.vendo.api_gateway.adapter.security.in.filter.path.SecuredAntPathResolver;
import com.vendo.api_gateway.adapter.security.out.jwt.parser.AuthenticationParser;
import com.vendo.api_gateway.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.vendo.security_lib.http.HttpUtils.AUTHORIZATION_HEADER;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class UserAuthFilter implements GlobalFilter {

    private final SecuredAntPathResolver antPathResolver;
    private final AuthenticationParser claimsParser;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        String path = request.getURI().getPath();
        if (antPathResolver.isPermittedPath(path)) return chain.filter(exchange);

        String authorization = GlobalFilterUtils.getTokenFromRequest(headers.getFirst(AUTHORIZATION_HEADER));
        User authUser = claimsParser.extract(authorization);
        GlobalFilterUtils.addUserToContext(authUser, exchange.getAttributes());

        ServerHttpRequest requestWithHeaders = applyHeaders(authUser, request);
        return chain.filter(exchange
                .mutate()
                .request(requestWithHeaders)
                .build()
        );
    }

    private ServerHttpRequest applyHeaders(User user, ServerHttpRequest request) {
        return request.mutate()
                .headers(headers -> headers.addAll(UserHeadersExtractor.from(user)))
                .build();
    }
}