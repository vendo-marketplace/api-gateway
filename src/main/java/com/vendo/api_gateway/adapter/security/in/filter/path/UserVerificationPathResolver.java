package com.vendo.api_gateway.adapter.security.in.filter.path;

import com.vendo.api_gateway.infrastructure.props.GatewayProps;
import com.vendo.security_lib.resolver.AntPathResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class UserVerificationPathResolver implements AntPathResolver {

    private final GatewayProps props;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean isPermittedPath(String path) {
        String[] paths = props.getVerified().allPaths().toArray(String[]::new);
        return Arrays.stream(paths).noneMatch(pr -> antPathMatcher.match(pr, path));
    }
}
