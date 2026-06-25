package com.vendo.api_gateway.infrastructure.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "endpoints")
public class GatewayProps {

    private Verified verified;
    private Unauthenticated unauthenticated;

    private static final String AUTH_COMPLETE_PATH = "/auth/complete";

    public record Unauthenticated(

            Set<String> general,
            Set<String> internal,
            Set<String> product,
            Set<String> auth,
            Set<String> search

    ) {
        public Set<String> allPaths() {
            return flatLists(List.of(general, internal, product, auth, search));
        }
    }

    public record Verified(Set<String> paths) {

        public String getCompletePath() {
            if (paths.contains(AUTH_COMPLETE_PATH)) {
                return AUTH_COMPLETE_PATH;
            }

            throw new IllegalStateException("Complete path not found in configuration.");
        }

    }

    private static Set<String> flatLists(List<Set<String>> lists) {
        return lists.stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
