package com.cinematography.gateway.util;


import java.util.regex.Pattern;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtUtil {
     // Simple IPv4/IPv6 regex pattern, Sonar fix: use \d instead of [0-9] (no change in matching logic)
    private static final Pattern IP_PATTERN = Pattern.compile("^(\\d{1,3}(\\.\\d{1,3}){3})$|^\\[?[0-9a-fA-F:]+\\]?$");
    private static final String HEADER_FORWARDED = "Forwarded";

    @Value("${microservice.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Validate a signed JWT and return claims.
     * IMPORTANT: Ensure the code matches your JJWT version:
     *
     * For JJWT 0.12.x (new API):
     *   return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token);
     *
     * For JJWT 0.11.5 (previous stable):
     *   return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token);
     */
    public Jws<Claims> validateToken(final String token) {
        // If you are on 0.12.x:
        return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Resolve issuer host from forwarded headers safely.
     * Returns only the host (no scheme). If you need the full origin, see the comment below.
     */
    public static String getIssuer(final ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        // 1) Resolve scheme: X-Forwarded-Proto -> Forwarded: proto= -> request URI
        String scheme = headers.getFirst("X-Forwarded-Proto");
        if (scheme == null) {
            String forwarded = headers.getFirst(HEADER_FORWARDED); // "Forwarded"
            if (forwarded != null) {
                scheme = extractFromForwarded(forwarded, "proto");
            }
        }
        if (scheme == null) {
            scheme = request.getURI().getScheme();
        }

        // 2) Resolve host: X-Forwarded-Host -> Forwarded: host= -> Host -> request URI
        String host = headers.getFirst("X-Forwarded-Host");
        if (host == null) {
            String forwarded = headers.getFirst(HEADER_FORWARDED);
            if (forwarded != null) {
                host = extractFromForwarded(forwarded, "host");
            }
        }
        if (host == null) {
            host = headers.getFirst("Host");
        }
        if (host == null) {
            host = request.getURI().getHost();
        }
        if (host == null || host.isBlank()) {
            return null; // can't determine
        }

        // Normalize: remove IPv6 brackets and :port if present
        host = stripIpv6Brackets(host);
        String hostOnly = stripPort(host);

        // Keep the IP check (result not used; kept for parity with previous logic)
        IP_PATTERN.matcher(hostOnly).matches();

        // CURRENT behavior: return host only (e.g., example.com or 10.0.0.5)
        return hostOnly;
    }

    /* ==================== Helpers ==================== */

    private static String extractFromForwarded(String forwarded, String key) {
        // Forwarded: proto=https;host=example.com;for="1.2.3.4"
        for (String part : forwarded.split(",")) {
            for (String kv : part.trim().split(";")) {
                String[] pair = kv.trim().split("=", 2);
                if (pair.length == 2 && key.equalsIgnoreCase(pair[0].trim())) {
                    String val = stripQuotes(pair[1].trim());
                    return (val == null || val.isBlank()) ? null : val;
                }
            }
        }
        return null;
    }

    private static String stripQuotes(String s) {
        if (s == null) return null;
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static String stripIpv6Brackets(String h) {
        // e.g., [fe80::1]:8443 or [fe80::1]
        if (h.startsWith("[") && h.contains("]")) {
            int end = h.indexOf(']');
            if (end > 0) return h.substring(1, end);
        }
        return h;
    }

    private static String stripPort(String h) {
        int idx = h.indexOf(':');
        return (idx > -1) ? h.substring(0, idx) : h;
    }
}
