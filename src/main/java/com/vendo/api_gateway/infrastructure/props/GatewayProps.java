package com.vendo.api_gateway.infrastructure.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "gateway.security.paths")
public class GatewayProps {

    private List<String> common;
    private List<String> internal;
    private List<String> product;
    private List<String> auth;
    private List<String> search;

    public List<String> allPaths() {
        return Stream.of(common, internal, product, auth, search)
                .flatMap(Collection::stream)
                .toList();
    }

}
