package com.vendo.api_gateway.adapter.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.exception.BadCredentialsException;

import java.util.Map;

import static com.vendo.security_lib.constants.AuthConstants.BEARER_PREFIX;

public final class FilterUtils {

    public static final String CONTEXT_ATTRIBUTE = "context";

    static <T> T getValueFromContext(Class<T> type, Map<String, Object> attributes) {
        Object value = attributes.get(CONTEXT_ATTRIBUTE);

        if (value == null) throw new IllegalArgumentException("Context attribute is missing.");

        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Expected %s but got %s."
                            .formatted(
                                    type.getSimpleName(),
                                    value.getClass().getSimpleName()
                            )
            );
        }

        return type.cast(value);
    }

    static String getTokenFromRequest(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("Unauthorized.");
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

}
