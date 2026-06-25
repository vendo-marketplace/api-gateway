package com.vendo.api_gateway.adapter.user.out;

import com.vendo.api_gateway.domain.user.User;
import com.vendo.api_gateway.port.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserClient userClient;

    @Override
    public Mono<User> findById(String id) {
        return userClient.getById(id);
    }
}
