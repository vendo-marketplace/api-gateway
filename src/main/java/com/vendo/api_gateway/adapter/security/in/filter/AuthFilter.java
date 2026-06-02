package com.vendo.api_gateway.adapter.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.exception.AuthenticationServiceException;
import com.vendo.api_gateway.adapter.security.in.filter.exception.BadCredentialsException;
import com.vendo.api_gateway.adapter.security.out.jwt.parser.AuthenticationParser;
import com.vendo.api_gateway.domain.user.User;
import com.vendo.security_lib.resolver.AntPathResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.vendo.core_lib.constants.Delimiters.COMMA_DELIMITER;
import static com.vendo.security_lib.constants.AuthConstants.AUTHORIZATION_HEADER;
import static com.vendo.security_lib.constants.AuthConstants.BEARER_PREFIX;
import static com.vendo.security_lib.type.UserHeaders.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter {

    private final AuthenticationParser claimsParser;

    private final AntPathResolver antPathResolver;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (shouldNotFilter(path)) return chain.filter(exchange);

        try {
            String authorization = getTokenFromRequest(request.getHeaders().getFirst(AUTHORIZATION_HEADER));
            User authUser = claimsParser.extract(authorization);
            authUser.throwIfBlocked();

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

    private boolean shouldNotFilter(String path) {
        return antPathResolver.isPermittedPath(path);
    }

    private String getTokenFromRequest(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("Unauthorized.");
        }

        return authorization.substring(BEARER_PREFIX.length());
    }

    private ServerHttpRequest applyHeaders(User user, ServerHttpRequest request) {
        return request
                .mutate()
                .header(USER_ID.getHeader(), user.id())
                .header(USER_EMAIL.getHeader(), user.email())
                .header(STATUS.getHeader(), user.status().name())
                .header(ROLES.getHeader(), String.join(COMMA_DELIMITER, user.roles()))
                .header(EMAIL_VERIFIED.getHeader(), String.valueOf(user.emailVerified()))
                .build();
    }
}