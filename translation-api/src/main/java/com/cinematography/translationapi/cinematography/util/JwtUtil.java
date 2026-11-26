package com.cinematography.translationapi.cinematography.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cinematography.translationapi.exception.UnauthorizedException;
import com.cinematography.translationapi.cinematography.dtos.ApiResponseCode;
import com.cinematography.web.security.JwtTokenValidator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * admin-api token checks on top of the shared {@link JwtTokenValidator}.
 */
@Service
public class JwtUtil extends JwtTokenValidator {

	public Jws<Claims> validateAdminToken(HttpServletRequest request) {
		String authHeader = request.getHeader(ApiResponseCode.AUTHORIZATION);

		if (!StringUtils.hasText(authHeader)) {
			throw new UnauthorizedException("Authorization header is missing", HttpStatus.UNAUTHORIZED);
		}
		if (!authHeader.startsWith(ApiResponseCode.BEARER)) {
			throw new UnauthorizedException("Authorization header must start with Bearer", HttpStatus.UNAUTHORIZED);
		}

		String token = authHeader.substring(7);
		try {
			return validateToken(token);
		} catch (ExpiredJwtException ex) {
			throw new UnauthorizedException("Token has expired. Please login again", HttpStatus.UNAUTHORIZED);
		} catch (JwtException ex) {
			throw new UnauthorizedException("Invalid authentication token", HttpStatus.UNAUTHORIZED);

		} catch (Exception ex) {
			throw new UnauthorizedException("Authentication processing failed", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}