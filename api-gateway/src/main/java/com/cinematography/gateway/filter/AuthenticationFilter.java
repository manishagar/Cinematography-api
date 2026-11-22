package com.cinematography.gateway.filter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.HttpHeaders;

import com.cinematography.core.dto.ApiResponse;
import com.cinematography.gateway.config.ApiResponseCode;
import com.cinematography.gateway.exception.ApiResponseException;
import com.cinematography.gateway.util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.TraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import com.cinematography.core.constants.CommonResponseCodes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter, Ordered {
    private final JwtUtil jwtUtil;
    private final ObservationRegistry observationRegistry;
    private final RouteValidator routeValidator;

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final String PARENT_SPAN_ID_HEADER = "X-Parent-Span-Id";

    /**
     * Filter method for intercepting HTTP requests in a Spring Cloud Gateway.
     * Performs authentication and validation based on the request path and JWT
     * token.
     *
     * @param exchange the current server exchange
     * @param chain    the gateway filter chain
     * @return Mono<Void> that signals when request processing is complete
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // ✅ FIRST: let Gateway decide if a route exists
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

        if (route == null) {
            // ❌ No route matched → DO NOT enforce auth
            // ✅ Let GatewayGlobalErrorHandler return 404
            return chain.filter(exchange);
        }

        // Inject trace headers
        ServerWebExchange mutatedExchange = injectTracingHeaders(exchange);
        ServerHttpRequest serverHttpRequest = mutatedExchange.getRequest();

        log.debug(
                "Trace info in AuthenticationFilter: traceId={}, spanId={}, parentSpanId={}",
                serverHttpRequest.getHeaders().getFirst(TRACE_ID_HEADER),
                serverHttpRequest.getHeaders().getFirst(SPAN_ID_HEADER),
                serverHttpRequest.getHeaders().getFirst(PARENT_SPAN_ID_HEADER));

        ApiResponse<?> apiResponse = new ApiResponse<>();
        List<String> messages = new ArrayList<>();

        String issuerName = JwtUtil.getIssuer(serverHttpRequest);
        log.debug("Resolved issuerName={}", issuerName);

        ServerHttpRequest modifiedRequest = serverHttpRequest.mutate()
                .header("X-Issuer-Name", issuerName)
                .build();

        ServerWebExchange finalExchange = mutatedExchange.mutate()
                .request(modifiedRequest)
                .build();

        boolean isAdminSecured = routeValidator.isSecuredAdmin(serverHttpRequest);
        boolean isSecured = routeValidator.isSecured(serverHttpRequest);

        if (!isAdminSecured && !isSecured) {
            return handlePublic(finalExchange, chain);
        }

        return isAdminSecured
                ? handleAdmin(finalExchange, chain, issuerName, apiResponse, messages)
                : null;
    }

    /*
     * =========================
     * Helper methods below
     * =========================
     */

    private Mono<Void> handlePublic(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange);
    }

    private Mono<Void> handleAdmin(ServerWebExchange exchange,
            GatewayFilterChain chain,
            String resolvedIssuer,
            ApiResponse<?> apiResponse,
            List<String> messages) {

        String authHeader = getCustomHeaderValue(exchange, HttpHeaders.AUTHORIZATION);

        Jws<Claims> claims;
        String tokenIssuer;
        try {
            claims = processAuthToken(authHeader);
            tokenIssuer = claims.getPayload().get("iss", String.class);
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: method={}, path={}",
                    exchange.getRequest().getMethod(), exchange.getRequest().getPath());
            return handleTokenValidationError(
                    apiResponse, messages, exchange, ApiResponseCode.ERROR_MSG_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: method={}, path={}, failureType={}",
                    exchange.getRequest().getMethod(), exchange.getRequest().getPath(),
                    e.getClass().getSimpleName());
            return handleTokenValidationError(
                    apiResponse, messages, exchange, ApiResponseCode.ERROR_MSG_TOKEN_VALIDATION);
        }

        Mono<Void> flow = validateIssuerDomain(resolvedIssuer, tokenIssuer, apiResponse, messages, exchange)
                .filter(Boolean::booleanValue)
                .flatMap(valid -> chain.filter(exchange));

        return withAuthErrorHandling(flow, exchange, messages);
    }

    /**
     * Centralized reactive error handling for secured flows (admin and non-admin).
     * - Properly handles ApiResponseException - Maps Service Unavailable correctly
     * to 503 - Keeps Unauthorized only for auth failures
     */
    private <T> Mono<T> withAuthErrorHandling(
            Mono<T> publisher,
            ServerWebExchange exchange,
            List<String> messages) {

        return publisher

                // ✅ Auth / validation errors BEFORE routing only
                .onErrorResume(ApiResponseException.class, ex -> {

                    if (exchange.getResponse().isCommitted()) {
                        // ✅ Downstream already handled it
                        log.debug("Response already committed, skipping ApiResponseException handling");
                        return Mono.empty();
                    }

                    ApiResponse<?> apiResponse = ex.getApiResponse();
                    if (apiResponse != null && apiResponse.getMessages() != null) {
                        messages.clear();
                        messages.addAll(apiResponse.getMessages());
                    }

                    return onErrorTyped(
                            exchange,
                            ex.getHttpStatus(),
                            apiResponse);
                })

                // ✅ Gateway infrastructure errors BEFORE routing only
                .onErrorResume(org.springframework.cloud.gateway.support.NotFoundException.class, ex -> {

                    if (exchange.getResponse().isCommitted()) {
                        log.debug("Response already committed, skipping NotFound handling");
                        return Mono.empty();
                    }

                    messages.clear();
                    messages.add(CommonResponseCodes.ERROR_MSG);
                    messages.add("Service temporarily unavailable. Please try again later.");

                    ApiResponse<Object> errorResponse = new ApiResponse<>();
                    errorResponse.setStatus(CommonResponseCodes.ERROR);
                    errorResponse.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                    errorResponse.setMessages(messages);

                    return onErrorTyped(
                            exchange,
                            HttpStatus.SERVICE_UNAVAILABLE,
                            errorResponse);
                })

                // ✅ FINAL fallback — LOG ONLY, NEVER WRITE
                .onErrorResume(ex -> {

                    if (exchange.getResponse().isCommitted()) {
                        // 🔑 THIS LINE STOPS THE WARNING
                        log.debug("Downstream response already committed; gateway will not rewrite", ex);
                        return Mono.empty();
                    }

                    // ✅ Only true pre-routing auth failures reach here
                    messages.clear();
                    messages.add(CommonResponseCodes.ERROR_MSG);
                    messages.add("Unauthorized");

                    ApiResponse<Object> errorResponse = new ApiResponse<>();
                    errorResponse.setStatus(CommonResponseCodes.ERROR);
                    errorResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                    errorResponse.setMessages(messages);

                    return onErrorTyped(
                            exchange,
                            HttpStatus.UNAUTHORIZED,
                            errorResponse);
                });
    }

    /**
     * Extracts and validates a JWT token from the Authorization header.
     *
     * This method removes the "Bearer " prefix from the Authorization header (if
     * present),
     * and then uses the jwtUtil to validate the token and parse the claims.
     *
     * @param authHeader The Authorization header from the incoming HTTP request
     * @return A Jws<Claims> object containing the parsed claims from the JWT token
     * @throws JwtException If the token is invalid, expired, or malformed
     */
    private Jws<Claims> processAuthToken(String authHeader) {

        // 1️. Header missing or blank
        if (!StringUtils.hasText(authHeader)) {
            throw new JwtException("Authorization header is missing");
        }

        // 2️. Wrong scheme (not Bearer)
        if (!authHeader.startsWith("Bearer ")) {
            throw new JwtException("Invalid Authorization scheme");
        }

        // 3️. Extract token
        String token = authHeader.substring(7).trim();

        // 4️. Token missing after Bearer
        if (!StringUtils.hasText(token)) {
            throw new JwtException("Bearer token is missing");
        }

        // 5️. Validate token
        return jwtUtil.validateToken(token);
    }

    /**
     * Validates that the issuer domain in the JWT token matches the domain of the
     * request host.
     *
     * This ensures the JWT token was issued by the same domain making the API
     * request.
     * If they do not match, the request is rejected with an UNAUTHORIZED error.
     *
     * @param issuerName  The hostname from the incoming HTTP request (e.g.,
     *                    example.com)
     * @param issuer      The issuer (iss) claim extracted from the JWT token
     * @param apiResponse The response object used to send error details to the
     *                    client
     * @param messages    A list of messages to accumulate error reasons
     * @param exchange    The current server web exchange object
     * @return Mono.empty() if issuer is valid, or error Mono if not
     */
    private Mono<Boolean> validateIssuerDomain(
            String issuerName,
            String issuer,
            ApiResponse<?> apiResponse,
            List<String> messages,
            ServerWebExchange exchange) {

        // ✅ Validate issuer vs request domain
        if (StringUtils.hasText(issuerName)
                && StringUtils.hasText(issuer)
                && !issuerName.equalsIgnoreCase(issuer)) {

            // ✅ CLEAR previous messages (IMPORTANT)
            messages.clear();

            // ✅ Populate error response
            messages.add(CommonResponseCodes.ERROR_MSG);
            messages.add(ApiResponseCode.ERROR_MSG_TOKEN_DOMAIN);

            apiResponse.setMessages(messages);
            apiResponse.setStatus(CommonResponseCodes.ERROR);
            apiResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());

            // ✅ Security‑relevant client error → WARN (correct)
            log.warn(
                    "Issuer domain mismatch: expected={}, actual={}, method={}, path={}",
                    issuerName,
                    issuer,
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getPath());

            // ✅ Write response AND signal validation failure
            return onError(exchange, HttpStatus.UNAUTHORIZED, apiResponse)
                    .thenReturn(false);
        }

        // ✅ Validation passed → allow next step
        return Mono.just(true);
    }

    /**
     * Handles token validation errors by populating the ApiResponse with
     * appropriate error messages,
     * setting the HTTP status to 401 Unauthorized, and returning the error response
     * via the exchange.
     *
     * @param apiResponse the response object to populate with error info
     * @param messages    the list of error messages to include
     * @param exchange    the current server exchange context
     * @return a Mono<Void> indicating completion of the error handling flow
     */
    private Mono<Void> handleTokenValidationError(
            ApiResponse<?> apiResponse,
            List<String> messages,
            ServerWebExchange exchange,
            String errorMessage) {

        // ✅ CLEAR previous messages
        messages.clear();

        messages.add(CommonResponseCodes.ERROR_MSG);
        messages.add(errorMessage);

        apiResponse.setMessages(messages);
        apiResponse.setStatus(CommonResponseCodes.ERROR);
        apiResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        log.warn(
                "Token validation failed: method={}, path={}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath());

        return onError(exchange, HttpStatus.UNAUTHORIZED, apiResponse);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus statusCode, ApiResponse<?> apiResponse) {

        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            log.debug("Response already committed, skipping error write");
            return Mono.empty();
        }

        response.setStatusCode(statusCode);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = (apiResponse != null) ? ApiResponse.toByteArray(apiResponse) : new byte[0];
        } catch (Exception e) {
            log.error("Failed to serialize ApiResponse, returning empty body", e);
            bytes = new byte[0];
        }

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes)))
                .doOnError(err -> log.error("Error writing response to client", err))
                .then();
    }

    /**
     * Retrieves the first value of a custom header from the incoming HTTP request.
     *
     * @param exchange   the current ServerWebExchange containing the request
     * @param headerName the name of the header to retrieve
     * @return the first value of the specified header, or null if not present
     */
    public String getCustomHeaderValue(ServerWebExchange exchange, String headerName) {
        // Extract the ServerHttpRequest object from the exchange
        ServerHttpRequest request = exchange.getRequest();

        // Return the first occurrence of the specified header
        return request.getHeaders().getFirst(headerName);
    }

    /**
     * Specifies the execution order of the filter.
     * Filters with lower order values are executed earlier.
     *
     * @return -1 to indicate high precedence (executed before filters with higher
     *         order values)
     */
    @Override
    public int getOrder() {
        return -1; // slightly later, but still early
    }

    /**
     * Injects tracing headers (traceId, spanId, parentSpanId) into the
     * ServerWebExchange request.
     * This enables downstream microservices to log and propagate tracing context
     * consistently.
     *
     * @param exchange the current ServerWebExchange from Spring WebFlux pipeline
     * @return modified ServerWebExchange with tracing headers, or the original if
     *         tracing context is missing
     */
    public ServerWebExchange injectTracingHeaders(ServerWebExchange exchange) {
        // Extract the current observation from the ObservationRegistry
        Observation currentObservation = observationRegistry.getCurrentObservation();

        if (currentObservation != null) {
            TraceContext traceContext = currentObservation.getContext().get(TraceContext.class);

            if (traceContext != null) {
                String traceId = traceContext.traceId();
                String spanId = traceContext.spanId();
                String parentId = traceContext.parentId() != null ? traceContext.parentId() : "none";

                log.debug("🔍 Injecting tracing headers: traceId={}, spanId={}, parentSpanId={}", traceId, spanId,
                        parentId);

                // Mutate the request with tracing headers
                ServerHttpRequest mutatedRequest = exchange.getRequest()
                        .mutate()
                        .header(TRACE_ID_HEADER, traceId)
                        .header(SPAN_ID_HEADER, spanId)
                        .header(PARENT_SPAN_ID_HEADER, parentId)
                        .build();

                return exchange.mutate().request(mutatedRequest).build();
            }
        }

        // Fallback: return the original exchange if no trace context is found
        return exchange;
    }

    /**
     * Generic bridge that writes the API error response and returns a typed error
     * Mono.
     */
    private <T> Mono<T> onErrorTyped(
            ServerWebExchange exchange,
            HttpStatus statusCode,
            ApiResponse<?> apiResponse) {

        return onError(exchange, statusCode, apiResponse)
                .then(Mono.empty());
    }

}