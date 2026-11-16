package com.cinematography.gateway.config;
import com.cinematography.core.constants.CommonResponseCodes;


public class ApiResponseCode extends CommonResponseCodes {
		private ApiResponseCode() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
	
	public static final String SUCCESS_CODE = "2XX";
	public static final String WARNING_CODE = "1XX";
	public static final String ERROR_CODE = "400";
	public static final String ERROR_MSG_AUTH_HEADER = "Authorization Header is not provided!";
	public static final String ERROR_MSG_TOKEN_EXPIRED = "Token has expired ";
	public static final String ERROR_MSG_CONTRACT_API = "This API is not accessible for this customer’s contract API ";
	public static final String ERROR_MSG_TOKEN_DOMAIN = "Token is not generated for this domain ";
	public static final String ERROR_MSG_TOKEN_VALIDATION = "Token validation failed ";

	public static final String UNKNOWN = "UNKNOWN";
	
	public static final String RESPONSE_ALREADY_COMMITED = "Response already committed, cannot write error response";
	
	

}
