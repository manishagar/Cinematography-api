package com.cinematography.gateway.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;

public class RouteValidator {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final String ADMIN_PATH_PATTERN = "/admin/users/admin/**";

    // Admin public endpoints (no authentication needed)
    private static final String ADMIN_LOGIN_PATH = "/admin/users/admin/login";
    private static final String ADMIN_FORGOT_PASSWORD = "/admin/users/admin/forgot-password";
    private static final String ADMIN_RESET_PASSWORD = "/admin/users/admin/reset-password";
    public static final String ADMIN_API_DOCS = "/admin/users/v3/api-docs";
    public static final String SWAGGER_UI_HTML = "/swagger-ui.html";
    public static final String API_DOCS_ALL = "/v3/api-docs/**";
    public static final String SWAGGER_UI_ALL = "/swagger-ui/**";


    private static final List<String> SWAGGER_ENDPOINTS = List.of(
            ADMIN_API_DOCS,
            SWAGGER_UI_HTML,
            API_DOCS_ALL,
            SWAGGER_UI_ALL
    );

    // List of endpoints that are publicly accessible (i.e., no authentication required)
    private static final List<String> OPEN_API_ENDPOINTS;

    static {
        List<String> tmp = new ArrayList<>();



        // Admin public endpoints
        tmp.add(ADMIN_LOGIN_PATH);
        tmp.add(ADMIN_FORGOT_PASSWORD);
        tmp.add(ADMIN_RESET_PASSWORD);

        // Swagger / OpenAPI docs
        tmp.addAll(SWAGGER_ENDPOINTS);

        // Eureka client registration
        tmp.add("/eureka");

        OPEN_API_ENDPOINTS = List.copyOf(tmp);
    }

    /**
     * Admin-specific check:
     * - Secured if request matches /auth/users/admin/** AND is NOT one of the excluded public admin endpoints.
     */
    public static boolean isSecuredAdminRequest(String path) {
        return PATH_MATCHER.match(ADMIN_PATH_PATTERN, path)
                && !PATH_MATCHER.match(ADMIN_LOGIN_PATH, path)
                && !PATH_MATCHER.match(ADMIN_FORGOT_PASSWORD, path)
                && !PATH_MATCHER.match(ADMIN_RESET_PASSWORD, path);
    }

    /**
     * Predicate to check if a request is secured (overall).
     * Returns true if:
     * - It is an admin secured endpoint (admin pattern but not excluded), OR
     * - It is not in the OPEN_API_ENDPOINTS list.
     */
    public static final Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();

        // Admin security rule overrides open list logic if it is an admin secured request
        if (isSecuredAdminRequest(path)) {
            return true;
        }

        // Otherwise, secured if NOT matching any open endpoint pattern
        return OPEN_API_ENDPOINTS.stream()
                .noneMatch(publicPath -> PATH_MATCHER.match(publicPath, path));
    };

    /**
     * Optional: Expose admin-only predicate if you want to use it directly.
     */
    public static final Predicate<ServerHttpRequest> isSecuredAdmin =
            request -> isSecuredAdminRequest(request.getURI().getPath());

    private RouteValidator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
