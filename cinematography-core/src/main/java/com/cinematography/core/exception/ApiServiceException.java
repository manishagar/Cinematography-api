package com.cinematography.core.exception;

/**
 * Service-level failure carrying an optional business error code and the HTTP status to report.
 * <p>
 * Constructors cover every existing call site: {@code (message[, statusCode][, cause])} as used by
 * hs-api and admin-api, and {@code (errorCode, message[, cause])} as used by cg-api.
 */
public class ApiServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final int statusCode;

    public ApiServiceException(String message) {
        super(message);
        this.errorCode = null;
        this.statusCode = 400;
    }

    public ApiServiceException(String message, int statusCode) {
        super(message);
        this.errorCode = null;
        this.statusCode = statusCode;
    }

    public ApiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.statusCode = 500;
    }

    public ApiServiceException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.statusCode = statusCode;
    }

    public ApiServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 500;
    }

    public ApiServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = 500;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
