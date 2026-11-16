package com.cinematography.gateway.exception;

import org.springframework.http.HttpStatus;
import com.cinematography.core.dto.ApiResponse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom exception to carry an ApiResponse object. Used to stop a reactive
 * chain and propagate validation errors.
 */
@Slf4j
@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ApiResponseException extends RuntimeException {

    private static final long serialVersionUID = -6876645545628151965L;

    private final transient ApiResponse<?> apiResponse;
    private final HttpStatus httpStatus;

    public ApiResponseException(ApiResponse<?> apiResponse,
                                HttpStatus httpStatus,
                                String message) {
        super(message);
        this.apiResponse = apiResponse;
        this.httpStatus = httpStatus;
    }
}
