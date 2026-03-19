package com.vendo.api_gateway.routes;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.vendo.core_lib.type.ServiceName.PRODUCT_SERVICE;

@Configuration
public class ProductRoutes {

    @Bean
    public RouteLocator authRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(PRODUCT_SERVICE.getServiceName(), r -> r
                        .path("/categories/**", "/products/**")
                        .uri("lb://%s".formatted(PRODUCT_SERVICE.getServiceName())))
                .build();
    }

}
