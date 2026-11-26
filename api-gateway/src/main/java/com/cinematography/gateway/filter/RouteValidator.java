package com.cinematography.gateway.filter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import com.cinematography.gateway.config.GatewaySecurityProperties;

@Component 
public class RouteValidator {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final String ADMIN_PATH_PATTERN = "/admin/users/admin/**";

    public static final String EXPIRED_PASSWORD_PATH =
            "/admin/users/admin/user/changeExpiredPassword";

    public boolean isExpiredPasswordRequest(ServerHttpRequest request) {
        return HttpMethod.PUT.equals(request.getMethod())
                && EXPIRED_PASSWORD_PATH.equals(request.getURI().getRawPath());
    }

    // Admin public endpoints (no authentication needed)
    public static final String ADMIN_API_DOCS = "/admin/users/v3/api-docs";
    public static final String SWAGGER_UI_HTML = "/swagger-ui.html";
    public static final String API_DOCS_ALL = "/v3/api-docs/**";
    public static final String SWAGGER_UI_ALL = "/swagger-ui/**";

    private static final List<String> SWAGGER_ENDPOINTS = List.of(
            ADMIN_API_DOCS,
            SWAGGER_UI_HTML,
            API_DOCS_ALL,
            SWAGGER_UI_ALL);

    // List of endpoints that are publicly accessible (i.e., no authentication
    // required)
    private final List<String> publicAdminPaths;

    private final List<String> openApiEndpoints;

    public RouteValidator(GatewaySecurityProperties securityProperties) {
        this.publicAdminPaths = securityProperties.publicAdminPaths();

        List<String> tmp = new ArrayList<>();

        // Admin public endpoints
        tmp.addAll(publicAdminPaths);

        // Swagger / OpenAPI docs
        tmp.addAll(SWAGGER_ENDPOINTS);

        // Eureka client registration
        tmp.add("/eureka");

        this.openApiEndpoints = List.copyOf(tmp);
    }

    public boolean isSecuredAdminRequest(String path) {
        if (EXPIRED_PASSWORD_PATH.equals(path)) {
            return true; // Path-only checks cannot grant a method-specific exception.
        }
        return PATH_MATCHER.match(ADMIN_PATH_PATTERN, path)
                && publicAdminPaths.stream().noneMatch(publicPath -> PATH_MATCHER.match(publicPath, path));
    }

    /**
     * Checks if a request is secured (overall).
     * Returns true if:
     * - It is an admin secured endpoint (admin pattern but not excluded), OR
     * - It is not in the open endpoints list.
     */
    public boolean isSecured(ServerHttpRequest request) {
        if (isExpiredPasswordRequest(request)) {
            return false;
        }
        String path = request.getURI().getPath();

        // Admin security rule overrides open list logic if it is an admin secured
        // request
        if (isSecuredAdminRequest(path)) {
            return true;
        }

        // Otherwise, secured if NOT matching any open endpoint pattern
        return openApiEndpoints.stream()
                .noneMatch(publicPath -> PATH_MATCHER.match(publicPath, path));
    }

    /**
     * Admin-only check.
     */
    public boolean isSecuredAdmin(ServerHttpRequest request) {
        if (isExpiredPasswordRequest(request)) {
            return false;
        }
        return isSecuredAdminRequest(request.getURI().getPath());
    }

}
