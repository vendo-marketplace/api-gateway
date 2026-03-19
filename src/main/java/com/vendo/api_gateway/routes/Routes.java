package com.vendo.api_gateway.routes;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.vendo.core_lib.type.ServiceName.AUTH_SERVICE;
import static com.vendo.core_lib.type.ServiceName.PRODUCT_SERVICE;

@Configuration
public class Routes {

    @Bean
    public RouteLocator authRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(AUTH_SERVICE.getServiceName(), r -> r
                        .path("/auth/**", "/password/**", "/verification/**")
                        .uri("lb://%s".formatted(AUTH_SERVICE.getServiceName())))
                .route(PRODUCT_SERVICE.getServiceName(), r -> r
                        .path("/categories/**", "/products/**")
                        .uri("lb://%s".formatted(PRODUCT_SERVICE.getServiceName())))
                .build();
    }

}
