package com.vendo.api_gateway.adapter.security.out.jwt.parser;

import com.vendo.api_gateway.adapter.security.out.jwt.JwtService;
import com.vendo.api_gateway.adapter.security.out.props.JwtProperties;
import com.vendo.api_gateway.domain.user.user.User;
import com.vendo.security_lib.type.UserTokenClaim;
import com.vendo.user_lib.type.UserStatus;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthenticationParser implements AuthenticationParser {

    private final JwtService jwtService;

    private final JwtProperties jwtProperties;

    @Override
    public User extract(String token) {
        Claims claims = jwtService.extractAllClaims(token, jwtProperties.getSecret().key());

        String id = extractId(claims);
        List<String> roles = extractRoles(claims, UserTokenClaim.ROLES.getClaim());
        Boolean verified = extractEmailVerified(claims);
        UserStatus status = extractStatus(claims);

        return new User(id, status, User.toRoles(roles), verified);
    }

    private String extractId(Claims claims) {
        String id = claims.get(UserTokenClaim.ID.getClaim(), String.class);

        if (id == null || id.isBlank()) {
            log.error("Id claim is not present.");
            throw new BadCredentialsException("Invalid token.");
        }

        return id;
    }

    private List<String> extractRoles(Claims claims, String rolesClaim) {
        Object rawRoles = claims.get(rolesClaim);
        AuthenticationException e = new BadCredentialsException("Invalid token.");

        if (rawRoles instanceof List<?> list && !list.isEmpty()) {
            if (list.stream().allMatch(String.class::isInstance)) {

                return list.stream()
                        .map(String.class::cast)
                        .toList();
            }
        }

        log.error("Invalid roles claim.");
        throw e;
    }

    private Boolean extractEmailVerified(Claims claims) {
        return claims.get(UserTokenClaim.VERIFIED.getClaim(), Boolean.class);
    }

    private UserStatus extractStatus(Claims claims) {
        String status = claims.get(UserTokenClaim.STATUS.getClaim(), String.class);

        try {
            return UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status claim, " + status + " is not type of UserStatus.");
            throw new BadCredentialsException("Invalid token.");
        }
    }
}
