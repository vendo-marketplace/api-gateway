package com.vendo.api_gateway.routes;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.vendo.common.type.Service.USER_SERVICE;


@Configuration
public class UserRoutes {

    @Bean
    public RouteLocator userRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(USER_SERVICE.getName(), r -> r
                        .path("/auth/**", "/password/**")
                        .uri("lb://%s".formatted(USER_SERVICE.getName())))
                .build();
    }
}
