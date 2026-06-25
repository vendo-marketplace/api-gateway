package com.vendo.api_gateway.adapter.user.out.exception;

public class UserServiceUnavailableException extends RuntimeException {

    public UserServiceUnavailableException(String message) {
        super(message);
    }

}
