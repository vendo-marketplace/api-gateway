package com.vendo.api_gateway.adapter.security.out.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService {

    public Claims extractAllClaims(String token, String secret) {
        try {
            return parseSignedClaims(token, secret).getPayload();
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage());
            throw new BadCredentialsException("Token expired.");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadCredentialsException("Invalid token.");
        }
    }

    private Key getSignInKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NullPointerException e) {
            log.error("Error while signing secret key: {}.", e.getMessage());
            throw new BadCredentialsException("Invalid token.");
        }
    }

    private Jws<Claims> parseSignedClaims(String token, String secret) throws JwtException {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignInKey(secret))
                .build()
                .parseSignedClaims(token);
    }
}
