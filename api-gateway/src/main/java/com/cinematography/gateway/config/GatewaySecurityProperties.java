package com.cinematography.gateway.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gateway security settings ({@code cinematography.gateway.security.*}).
 * <p>
 * {@code public-admin-paths} lists the admin endpoints that are reachable without a JWT (login and MDLP sign-in).
 * Because this setting opens endpoints, it is validated at startup: the list must not be empty and every entry must be
 * one exact path under {@value #ADMIN_PREFIX}, without wildcards or URI variables, so a configuration mistake cannot
 * open a whole group of admin endpoints.
 */
@ConfigurationProperties(prefix = "cinematography.gateway.security")
public record GatewaySecurityProperties(List<String> publicAdminPaths) {

	static final String ADMIN_PREFIX = "/admin/users/admin/";

	public GatewaySecurityProperties {
		if (publicAdminPaths == null || publicAdminPaths.isEmpty()) {
			throw new IllegalStateException(
					"cinematography.gateway.security.public-admin-paths must list the admin endpoints that need no JWT");
		}
		for (String path : publicAdminPaths) {
			if (path == null || !path.startsWith(ADMIN_PREFIX) || path.length() == ADMIN_PREFIX.length()
					|| path.contains("*") || path.contains("?") || path.contains("{")) {
				throw new IllegalStateException("Invalid cinematography.gateway.security.public-admin-paths entry '" + path
						+ "': it must be one exact path under " + ADMIN_PREFIX + " without wildcards");
			}
		}
		publicAdminPaths = List.copyOf(publicAdminPaths);
	}
}
