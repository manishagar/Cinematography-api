package com.cinematography.gateway.filter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;

/**
 * Runs before authentication (outermost)
 */
@Slf4j
@Component
public class ApiGatwayDurationGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant startTime = Instant.now();

        String path = exchange.getRequest().getPath().value();   
        String method = exchange.getRequest().getMethod().name(); // POST, GET, etc.
        String clientIp = extractClientIp(exchange);

        return chain.filter(exchange)
            .doOnSuccess(unused -> {
                long durationMs = Duration.between(startTime, Instant.now()).toMillis();
                HttpStatusCode statusCode = exchange.getResponse().getStatusCode();

                String targetService = extractServiceFromPath(path);  // Derive microservice name from path

                log.info(">>> [API-GATEWAY → {}: {} {}] | Status={} | Duration={} ms | ClientIP={} <<<",
                        targetService,
                        method,
                        path,
                        statusCode != null ? statusCode.value() : "UNKNOWN",
                        durationMs,
                        clientIp
                );//,  traceId, spanId, parentSpanId
            });
    }

    // Extracts IP address from headers or remote address
    private String extractClientIp(ServerWebExchange exchange) {
    	ServerHttpRequest request = exchange.getRequest();

    	String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
    	if (forwardedFor != null) {
    		return forwardedFor;
    	}

    	InetSocketAddress remoteAddress = request.getRemoteAddress();
    	if (remoteAddress != null && remoteAddress.getAddress() != null) {
    		return remoteAddress.getAddress().getHostAddress();
    	}

    	return "UNKNOWN";
    }

    // Parses the service name from the path
    private String extractServiceFromPath(String path) {
        if (path.startsWith("/admin")) return "ADMIN-API";
        return "UNKNOWN";
    }


    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // -100 or so; // Ensures this filter runs early
    }
}
