package com.vendo.api_gateway.adapter.security.in.filter.path;

import com.vendo.api_gateway.infrastructure.props.GatewayProps;
import com.vendo.security_lib.resolver.AntPathResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserVerificationPathResolver implements AntPathResolver {

    private final GatewayProps props;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean isPermittedPath(String path) {
        Set<String> excluded = props.getVerified().excluded();
        if (excluded != null && excluded.stream().anyMatch(pr -> antPathMatcher.match(pr, path))) {
            return true;
        }
        String[] paths = props.getVerified().paths().toArray(String[]::new);
        return Arrays.stream(paths).noneMatch(pr -> antPathMatcher.match(pr, path));
    }
}
