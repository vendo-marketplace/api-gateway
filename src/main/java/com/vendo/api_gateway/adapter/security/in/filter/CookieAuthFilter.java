package com.vendo.api_gateway.adapter.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.path.SecuredAntPathResolver;
import com.vendo.core_lib.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.vendo.security_lib.http.HttpUtils.AUTHORIZATION_HEADER;
import static com.vendo.security_lib.http.HttpUtils.BEARER_PREFIX;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class CookieAuthFilter implements GlobalFilter {

    private final SecuredAntPathResolver antPathResolver;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

        System.out.println("Cookies " + cookies);

        String path = request.getURI().getPath();
        if (antPathResolver.isPermittedPath(path)) return chain.filter(exchange);

        HttpCookie authCookie = cookies.getFirst(AUTHORIZATION_HEADER);
        if (ObjectUtils.isNull(authCookie)) return chain.filter(exchange);

        String authorization = GlobalFilterUtils.getTokenFromRequest(authCookie.getValue());
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
