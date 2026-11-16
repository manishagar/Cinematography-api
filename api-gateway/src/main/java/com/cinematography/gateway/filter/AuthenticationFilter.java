package com.cinematography.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.cinematography.gateway.util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.TraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter, Ordered {
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final String PARENT_SPAN_ID_HEADER = "X-Parent-Span-Id";

    private final JwtUtil jwtUtil;
    private final ObservationRegistry observationRegistry;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route == null) {
            return chain.filter(exchange);
        }

        ServerWebExchange tracedExchange = injectTracingHeaders(exchange);
        ServerHttpRequest request = tracedExchange.getRequest();

        if (!RouteValidator.isSecured.test(request)) {
            return chain.filter(tracedExchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return writeUnauthorized(tracedExchange, "Authorization header is missing or invalid");
        }

        String token = authHeader.substring(7).trim();
        if (!StringUtils.hasText(token)) {
            return writeUnauthorized(tracedExchange, "Bearer token is missing");
        }

        try {
            Jws<Claims> claims = jwtUtil.validateToken(token);
            String tokenIssuer = claims.getPayload().get("iss", String.class);
            String resolvedIssuer = JwtUtil.getIssuer(request);

            if (StringUtils.hasText(resolvedIssuer)
                    && StringUtils.hasText(tokenIssuer)
                    && !resolvedIssuer.equalsIgnoreCase(tokenIssuer)) {
                log.warn("Issuer domain mismatch: expected={}, actual={}, method={}, path={}",
                        resolvedIssuer, tokenIssuer, request.getMethod(), request.getPath());
                return writeUnauthorized(tracedExchange, "Token issuer does not match the request host");
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Issuer-Name", resolvedIssuer)
                    .build();
            return chain.filter(tracedExchange.mutate().request(modifiedRequest).build());
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation failed: method={}, path={}, reason={}",
                    request.getMethod(), request.getPath(), ex.getMessage());
            return writeUnauthorized(tracedExchange, "Invalid or expired token");
        }
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, String message) {
        var response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.empty();
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"status\":\"ERROR\",\"statusCode\":401,\"messages\":[\"Unauthorized\",\""
                + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"]}";
        var buffer = response.bufferFactory().wrap(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public ServerWebExchange injectTracingHeaders(ServerWebExchange exchange) {
        Observation currentObservation = observationRegistry.getCurrentObservation();
        if (currentObservation != null) {
            TraceContext traceContext = currentObservation.getContext().get(TraceContext.class);
            if (traceContext != null) {
                String traceId = traceContext.traceId();
                String spanId = traceContext.spanId();
                String parentId = traceContext.parentId() != null ? traceContext.parentId() : "none";

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header(TRACE_ID_HEADER, traceId)
                        .header(SPAN_ID_HEADER, spanId)
                        .header(PARENT_SPAN_ID_HEADER, parentId)
                        .build();
                return exchange.mutate().request(mutatedRequest).build();
            }
        }
        return exchange;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}