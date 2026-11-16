package com.cinematography.web.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Validates JWTs signed with the shared secret ({@code microservice.security.jwt.secret-key}).
 * <p>
 * Registered automatically; a service that needs extra token checks extends this class and declares it as a
 * bean, which replaces the default one.
 */
public class JwtTokenValidator {

	@Value("${microservice.security.jwt.secret-key}")
	private String secretKey;

	/**
	 * Validates and parses the given JWT token.
	 *
	 * @param token the JWT token to validate
	 * @return the parsed and validated claims from the token
	 * @throws io.jsonwebtoken.JwtException if the token is invalid, expired or tampered
	 */
	public Jws<Claims> validateToken(final String token) {
		return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token);
	}

	private SecretKey getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
