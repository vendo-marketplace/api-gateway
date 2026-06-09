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

    private Unauthenticated unauthenticated;

    private Verified verified;

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

    }

    private static Set<String> flatLists(List<Set<String>> lists) {
        return lists.stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
