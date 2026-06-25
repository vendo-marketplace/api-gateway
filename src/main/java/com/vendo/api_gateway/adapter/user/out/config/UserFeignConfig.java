package com.vendo.api_gateway.adapter.user.out.config;

import com.vendo.api_gateway.adapter.user.out.exception.UserServiceStatusHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactivefeign.client.statushandler.ReactiveStatusHandler;

@Configuration
public class UserFeignConfig {

    @Bean
    public ReactiveStatusHandler statusHandler(UserServiceStatusHandler errorDecoder) {
        return errorDecoder;
    }
}
