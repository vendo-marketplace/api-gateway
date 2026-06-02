package com.vendo.api_gateway.adapter.security.in.filter.path;

import com.vendo.api_gateway.infrastructure.props.GatewayProps;
import com.vendo.security_lib.resolver.AntPathResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DefaultAntPathResolver implements AntPathResolver {

    private final GatewayProps props;

    private final String[] PERMITTED_PATHS = props.allPaths().toArray(String[]::new);
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean isPermittedPath(String path) {
        return Arrays.stream(PERMITTED_PATHS).anyMatch(pr -> antPathMatcher.match(pr, path));
    }
}
