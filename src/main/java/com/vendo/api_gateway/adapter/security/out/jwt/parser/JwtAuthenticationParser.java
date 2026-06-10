package com.vendo.api_gateway.adapter.security.out.jwt.parser;

import com.vendo.api_gateway.adapter.security.in.filter.exception.BadCredentialsException;
import com.vendo.api_gateway.adapter.security.out.jwt.JwtService;
import com.vendo.api_gateway.adapter.security.out.props.JwtProperties;
import com.vendo.api_gateway.domain.user.User;
import com.vendo.security_lib.type.UserClaim;
import com.vendo.user_lib.type.UserRole;
import com.vendo.user_lib.type.UserStatus;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vendo.core_lib.constants.Delimiters.COMMA_DELIMITER;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthenticationParser implements AuthenticationParser {

    private final JwtProperties jwtProperties;

    @Override
    public User extract(String token) {
        try {
            Claims claims = JwtService.extractAllClaims(token, jwtProperties.getSecret().key());

            String id = extractId(claims);
            Set<UserRole> roles = extractRoles(claims);
            String email = extractEmail(claims);
            Boolean verified = extractEmailVerified(claims);
            UserStatus status = extractStatus(claims);

            return new User(id, email, status, roles, verified);
        } catch (Exception e) {
            log.error("Token extraction error: {}.", e.getMessage());
            throw new BadCredentialsException("Invalid token.");
        }
    }

    private String extractId(Claims claims) {
        String id = claims.get(UserClaim.ID.getClaim(), String.class);

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id is required.");
        }

        return id;
    }

    private String extractEmail(Claims claims) {
        return claims.get(UserClaim.EMAIL.getClaim(), String.class);
    }

    private Set<UserRole> extractRoles(Claims claims) {
        String rawRoles = claims.get(UserClaim.ROLES.getClaim(), String.class);
        return Arrays.stream(rawRoles.split(COMMA_DELIMITER))
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
    }

    private Boolean extractEmailVerified(Claims claims) {
        return claims.get(UserClaim.VERIFIED.getClaim(), Boolean.class);
    }

    private UserStatus extractStatus(Claims claims) {
        String status = claims.get(UserClaim.STATUS.getClaim(), String.class);
        return UserStatus.valueOf(status);
    }
}
