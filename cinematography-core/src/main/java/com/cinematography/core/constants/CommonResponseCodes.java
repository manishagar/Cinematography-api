package com.cinematography.core.constants;

import java.util.List;

/**
 * Response codes and messages shared by every service.
 * <p>
 * Each service keeps its own {@code ApiResponseCode} for service-specific constants and extends this
 * class, so existing references such as {@code ApiResponseCode.SUCCESS} keep compiling.
 */
public class CommonResponseCodes {

	protected CommonResponseCodes() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static final int SUCCESS_CODE = 200;
	public static final String SUCCESS = "success";
	public static final String SUCCESS_MSG = "API executed successfully!";

	public static final int WARNING_CODE = 100;
	public static final String WARNING = "warning";
	public static final String WARNING_MSG = "API executed with warnings!";

	public static final int ERROR_CODE = 400;
	public static final String ERROR = "error";
	public static final String ERROR_MSG = "API executed with errors!";

	public static final List<String> MESSAGES = List.of();

	public static final String CUSTOMER_NOT_EXISTS_CODE = "404";
	public static final String CUSTOMER_NOT_EXISTS = "error";

	public static final String TOKEN_EXPIRED = "Token has expired";

	/** Eureka service id of token-api, which receives audit records. */
	public static final String TOKEN_API = "token-api";
	/** token-api endpoint that stores audit records ({@code AuditLogDetails}). */
	public static final String AUDIT_URL = "/tokenapi/customer/audit/log";
}
