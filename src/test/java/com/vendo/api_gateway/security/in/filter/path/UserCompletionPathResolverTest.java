package com.vendo.api_gateway.security.in.filter.path;

import com.vendo.api_gateway.adapter.security.in.filter.path.UserCompletionPathResolver;
import com.vendo.api_gateway.infrastructure.props.GatewayProps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCompletionPathResolverTest {

    @Mock
    private GatewayProps props;

    @InjectMocks
    private UserCompletionPathResolver resolver;

    @Test
    void isPermittedPath_shouldReturnFalse_whenPathRequiresCompletion() {
        when(props.getCompleted()).thenReturn(new GatewayProps.Completed(Set.of("/products", "/conversations/**")));

        assertThat(resolver.isPermittedPath("/conversations/123")).isFalse();

        verify(props).getCompleted();
    }

    @Test
    void isPermittedPath_shouldReturnTrue_whenPathDoesNotRequireCompletion() {
        when(props.getCompleted()).thenReturn(new GatewayProps.Completed(Set.of("/products", "/conversations/**")));

        assertThat(resolver.isPermittedPath("/products/123")).isTrue();

        verify(props).getCompleted();
    }
}
