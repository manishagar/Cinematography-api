package com.cinematography.adminapi.exception;

public class PasswordExpiryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PasswordExpiryException(String message) {
        super(message);
    }
}
