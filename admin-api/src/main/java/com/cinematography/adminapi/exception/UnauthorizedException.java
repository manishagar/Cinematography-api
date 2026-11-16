package com.cinematography.adminapi.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends RuntimeException {

	private static final long serialVersionUID = 6266976627536233362L;
	private final HttpStatus status;

    public UnauthorizedException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
    }

    public UnauthorizedException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
