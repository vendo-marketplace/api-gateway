package com.vendo.api_gateway.adapter.user.out.exception;

import com.vendo.core_lib.type.ServiceName;
import com.vendo.user_lib.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactivefeign.client.ReactiveHttpResponse;
import reactivefeign.client.statushandler.ReactiveStatusHandler;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserServiceStatusHandler implements ReactiveStatusHandler {

    @Override
    public boolean shouldHandle(int status) {
        return HttpStatus.valueOf(status).isError();
    }

    @Override
    public Mono<? extends Throwable> decode(String methodKey, ReactiveHttpResponse response) {
        int status = response.status();

        if (HttpStatus.valueOf(status).is5xxServerError()) {
            return Mono.just(new UserServiceUnavailableException(ServiceName.USER_SERVICE + " is unavailable."));
        }

        if (HttpStatus.NOT_FOUND.value() == status) {
            return Mono.just(new UserNotFoundException("User not found."));
        }

        log.error("Unhandled status: {}", status);
        return Mono.just(new IllegalArgumentException("Unhandled user exception."));
    }
}
