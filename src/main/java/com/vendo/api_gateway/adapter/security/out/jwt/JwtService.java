package com.vendo.api_gateway.adapter.security.out.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;

public class JwtService {

    public static Claims extractAllClaims(String token, String secret) {
        return parseSignedClaims(token, secret).getPayload();
    }

    private static Jws<Claims> parseSignedClaims(String token, String secret) throws JwtException {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignInKey(secret))
                .build()
                .parseSignedClaims(token);
    }

    private static Key getSignInKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Invalid token.");
        }
    }
}
