package com.vendo.api_gateway.adapter.security.in.filter;

import com.vendo.api_gateway.adapter.security.in.filter.exception.BadCredentialsException;
import com.vendo.api_gateway.domain.user.User;
import com.vendo.core_lib.utils.CollectionUtils;
import com.vendo.core_lib.utils.ObjectUtils;
import com.vendo.core_lib.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

import static com.vendo.security_lib.http.HttpUtils.BEARER_PREFIX;

@Slf4j
public final class GlobalFilterUtils {

    public static final String CONTEXT_ATTRIBUTE = "context";

    static <T> T getValueFromContext(Class<T> type, Map<String, Object> attributes) {
        Object value = attributes.get(CONTEXT_ATTRIBUTE);

        if (ObjectUtils.isNull(value)) {
            log.error("The value is null.");
            throw new IllegalArgumentException("Context attribute is missing.");
        }

        if (!type.isInstance(value)) {
            log.error("Value is not instance of type: %s.".formatted(type.getSimpleName()));
            throw new IllegalArgumentException(
                    "Expected %s but got %s."
                            .formatted(
                                    type.getSimpleName(),
                                    value.getClass().getSimpleName()
                            )
            );
        }

        return type.cast(value);
    }

    static void addUserToContext(User user, Map<String, Object> attributes) {
        attributes.put(GlobalFilterUtils.CONTEXT_ATTRIBUTE, user);
    }

    static String getValueFromCookies(@NonNull String name, MultiValueMap<String, HttpCookie> cookies) {
        if (ObjectUtils.isNull(cookies) || cookies.isEmpty() ) {
            log.error("Cookies are empty.");
            throw new BadCredentialsException("Invalid or expired token.");
        }

        return cookies.entrySet().stream()
                .filter(entry -> name.equals(entry.getKey()) && !CollectionUtils.isEmpty(entry.getValue()))
                .map(entry -> findFirstValidTokenCookie(entry.getValue()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No valid token in cookies found.");
                    return new BadCredentialsException("Invalid or expired token.");
                });
    }

    private static String findFirstValidTokenCookie(List<HttpCookie> cookies) {
        return cookies.stream()
                .filter(httpCookie -> ObjectUtils.isNotNull(httpCookie) && isValidToken(httpCookie.getValue()))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private static boolean isValidToken(String authorization) {
        return StringUtils.isNotEmpty(authorization) && authorization.startsWith(BEARER_PREFIX);
    }

}
