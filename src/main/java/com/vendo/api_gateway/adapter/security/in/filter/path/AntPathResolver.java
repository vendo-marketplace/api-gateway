package com.vendo.api_gateway.adapter.security.in.filter.path;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Component
public class AntPathResolver implements com.vendo.security_lib.resolver.AntPathResolver {

    public static final List<String> DEFAULT_PATHS = List.of(
            "/actuator/health",
            "/internal/**"
    );

    public static final List<String> PRODUCT_PATHS = List.of(
            "/categories/tree"
    );

    public static final List<String> AUTH_PATHS = List.of(
            "/auth/sign-in",
            "/auth/sign-up",
            "/auth/refresh",
            "/auth/google",
            "/password/**",
            "/verification/**"
    );

    public static final List<String> SEARCH_PATHS = List.of(
            "/categories/tree"
    );

    public static final String[] PERMITTED_PATHS = Stream.of(
            DEFAULT_PATHS,
            PRODUCT_PATHS,
            AUTH_PATHS,
            SEARCH_PATHS
    ).flatMap(Collection::stream).toArray(String[]::new);

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean isPermittedPath(String path) {
        return Arrays.stream(PERMITTED_PATHS).anyMatch(pr -> antPathMatcher.match(pr, path));
    }
}
