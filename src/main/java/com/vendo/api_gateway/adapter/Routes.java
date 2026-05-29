package com.vendo.api_gateway.adapter;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.vendo.core_lib.type.ServiceName.*;

@Configuration
public class Routes {

    @Bean
    public RouteLocator authRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(AUTH_SERVICE.getServiceName(), r -> r
                        .path("/auth/**", "/password/**", "/verification/**")
                        .uri("lb://%s".formatted(AUTH_SERVICE.getServiceName())))
                .route(PRODUCT_SERVICE.getServiceName(), r -> r
                        .path("/categories/**", "/products/**", "/attributes/**")
                        .uri("lb://%s".formatted(PRODUCT_SERVICE.getServiceName())))
                .route(AWS_SERVICE.getServiceName(), r -> r
                        .path("/storage/**")
                        .uri("lb://%s".formatted(AWS_SERVICE.getServiceName())))
                .build();
    }

}
