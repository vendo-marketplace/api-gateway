package com.vendo.api_gateway.adapter.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthenticationServiceException;
import com.vendo.api_gateway.adapter.security.in.filter.header.UserHeadersExtractor;
import com.vendo.api_gateway.adapter.security.out.jwt.parser.AuthenticationParser;
import com.vendo.api_gateway.domain.user.User;
import com.vendo.security_lib.resolver.AntPathResolver;
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

import java.util.Map;

import static com.vendo.security_lib.constants.AuthConstants.AUTHORIZATION_HEADER;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
class UserAuthFilter implements GlobalFilter {

    private final AuthenticationParser claimsParser;
    private final AntPathResolver antPathResolver;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String path = request.getURI().getPath();

        if (antPathResolver.isPermittedPath(path)) return chain.filter(exchange);

        try {
            String authorization = FilterUtils.getTokenFromRequest(headers.getFirst(AUTHORIZATION_HEADER));
            User authUser = claimsParser.extract(authorization);
            addUserToContext(authUser, request.getAttributes());

            ServerHttpRequest requestWithHeaders = applyHeaders(authUser, request);
            return chain.filter(exchange
                    .mutate()
                    .request(requestWithHeaders)
                    .build()
            );

        } catch (Exception e) {
            log.error("Authentication exception occurred while filter: {}.", e.getMessage());
            throw new AuthenticationServiceException("Internal authentication error.");
        }
    }

    private ServerHttpRequest applyHeaders(User user, ServerHttpRequest request) {
        return request.mutate()
                .headers(headers -> headers.addAll(UserHeadersExtractor.from(user)))
                .build();
    }

    private void addUserToContext(User user, Map<String, Object> attributes) {
        attributes.put(FilterUtils.CONTEXT_ATTRIBUTE, user);
    }
}