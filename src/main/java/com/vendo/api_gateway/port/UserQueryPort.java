package com.vendo.api_gateway.port;

import com.vendo.api_gateway.domain.user.User;
import reactor.core.publisher.Mono;

public interface UserQueryPort {

    Mono<User> findById(String id);

}
