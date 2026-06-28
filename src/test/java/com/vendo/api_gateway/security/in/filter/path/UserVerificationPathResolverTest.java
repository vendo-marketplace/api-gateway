package com.vendo.api_gateway.security.in.filter.path;

import com.vendo.api_gateway.adapter.security.in.filter.path.UserVerificationPathResolver;
import com.vendo.api_gateway.infrastructure.props.GatewayProps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserVerificationPathResolverTest {

    @Mock
    private GatewayProps props;

    @InjectMocks
    private UserVerificationPathResolver resolver;

    @Test
    void isPermittedPath_shouldReturnTrue_whenPathMatchesExcluded() {
        when(props.getVerified()).thenReturn(new GatewayProps.Verified(Set.of("/api/**"), Set.of("/api/complete/**")));

        assertThat(resolver.isPermittedPath("/api/complete/123")).isTrue();
    }

    @Test
    void isPermittedPath_shouldReturnTrue_whenExcludedPatternMatchesButPathAlsoInVerifiedPaths() {
        when(props.getVerified()).thenReturn(new GatewayProps.Verified(Set.of("/api/**"), Set.of("/api/complete/**")));

        assertThat(resolver.isPermittedPath("/api/complete")).isTrue();
    }

    @Test
    void isPermittedPath_shouldReturnFalse_whenPathInVerifiedAndNotExcluded() {
        when(props.getVerified()).thenReturn(new GatewayProps.Verified(Set.of("/api/**"), Set.of("/api/complete/**")));

        assertThat(resolver.isPermittedPath("/api/products")).isFalse();
    }

    @Test
    void isPermittedPath_shouldReturnTrue_whenPathNotInVerifiedPaths() {
        when(props.getVerified()).thenReturn(new GatewayProps.Verified(Set.of("/api/**"), Set.of()));

        assertThat(resolver.isPermittedPath("/public/endpoint")).isTrue();
    }

    @Test
    void isPermittedPath_shouldReturnFalse_whenExcludedIsNull_andPathInVerifiedPaths() {
        when(props.getVerified()).thenReturn(new GatewayProps.Verified(Set.of("/api/**"), null));

        assertThat(resolver.isPermittedPath("/api/products")).isFalse();
    }

    @Test
    void isPermittedPath_shouldReturnFalse_whenExcludedIsEmpty_andPathInVerifiedPaths() {
        when(props.getVerified()).thenReturn(new GatewayProps.Verified(Set.of("/api/**"), Set.of()));

        assertThat(resolver.isPermittedPath("/api/products")).isFalse();
    }
}
