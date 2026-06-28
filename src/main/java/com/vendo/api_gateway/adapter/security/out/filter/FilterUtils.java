package com.vendo.api_gateway.adapter.security.out.filter;

public final class FilterUtils {

    public static final String RESPONSE_MESSAGE_TEMPLATE = """
        {
            "message": "%s",
            "code": %d,
            "timestamp": "%s"
        }
        """;

}
