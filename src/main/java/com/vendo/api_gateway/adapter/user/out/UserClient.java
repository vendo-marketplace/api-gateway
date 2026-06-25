package com.vendo.api_gateway.adapter.user.out;

import com.vendo.api_gateway.adapter.user.out.config.UserFeignConfig;
import com.vendo.api_gateway.domain.user.User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@Component
@ReactiveFeignClient(
        name = "user-service",
        path = "/internal/users",
        configuration = UserFeignConfig.class)
public interface UserClient {

    @GetMapping(params = "id")
    Mono<User> getById(@RequestParam("id") String id);

}
