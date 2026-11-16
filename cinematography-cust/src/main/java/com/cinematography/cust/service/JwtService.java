package com.cinematography.cust.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	@Value("${microservice.security.jwt.secret-key}")
    private String secretKey;

    @Value("${microservice.security.jwt.expiration-time}")
    private long jwtExpiration;
	public Jws<Claims> validateToken(final String token) {
		return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token);
		 
	}

	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, username);
	}

	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().claims().add(claims).subject(subject).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + jwtExpiration)).and()
				.signWith(getSignInKey(), Jwts.SIG.HS512).compact();
	}

	private SecretKey getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	
	public String generateToken(String username, Map<String, Object> claims ) {
		if (claims == null ) {
			claims = new HashMap<>();
		} 
		return createToken(claims, username);
	}
	
}
