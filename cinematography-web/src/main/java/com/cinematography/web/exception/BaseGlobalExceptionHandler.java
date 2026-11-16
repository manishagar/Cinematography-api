package com.cinematography.web.exception;

import java.util.List;

import com.cinematography.core.dto.ApiResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.jspecify.annotations.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Shared REST error handling: every error is returned as an {@link ApiResponse}.
 * <p>
 * Registered automatically by {@code cinematographyWebAutoConfiguration} when the service has no
 * {@link ResponseEntityExceptionHandler} of its own. A service that needs different behaviour extends this
 * class, annotates it with {@code @RestControllerAdvice} and overrides only the handlers that differ
 * (keep the same method names so Spring does not see two handlers for one exception type).
 */
@RestControllerAdvice
@Slf4j
public class BaseGlobalExceptionHandler extends ResponseEntityExceptionHandler {

//  @Valid on @RequestBody
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			@NonNull MethodArgumentNotValidException ex,
			@NonNull HttpHeaders headers,
			@Nullable HttpStatusCode status,
			@NonNull WebRequest request) {

		List<String> details = getValidationErrorsDetails(ex);
		String path = extractPath(request);
		log.warn("400 Validation failed at {} -> {}", path, details);

		ApiResponse<?> body = ApiResponse.error(details, HttpStatus.BAD_REQUEST);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

//  @Validated on method params
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {

    	List<String> details = ex.getConstraintViolations()
    			.stream()
    			.map(this::formatConstraintViolation)
    			.toList(); // unmodifiable list (Java 16+)

    	String path = extractPath(request);
    	log.warn("400 Constraint violations at {} -> {}", path, details);

    	ApiResponse<?> body = ApiResponse.error(details, HttpStatus.BAD_REQUEST);
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

//  Malformed JSON
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @NonNull WebRequest request) {

        String path = extractPath(request);
        log.warn("400 Message not readable at {}: {}", path, ex.getMessage());

        ApiResponse<?> body = ApiResponse.error(
                List.of("Malformed request body or incorrect data types."),
                HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

//  Wrong type for query/path parameter
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {

        String detail = String.format("Parameter '%s' expects type '%s' but got value '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                ex.getValue());

        String path = extractPath(request);
        log.warn("400 Type mismatch at {}: {}", path, detail);

        ApiResponse<?> body = ApiResponse.error(List.of(detail), HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

//  Missing required query parameter
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @NonNull WebRequest request) {

        String detail = String.format("Missing required parameter '%s'", ex.getParameterName());
        String path = extractPath(request);
        log.warn("400 Missing parameter at {}: {}", path, detail);

        ApiResponse<?> body = ApiResponse.error(List.of(detail), HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

//  Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAll(Exception ex, WebRequest request) {
        String path = extractPath(request);
        log.error("500 Unhandled exception at {}: {}", path, ex.getMessage(), ex);

        ApiResponse<?> body = ApiResponse.error(
                List.of("Internal error while processing request"),
                HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

//  Helpers
    protected List<String> getValidationErrorsDetails(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    } else {
                        return error.getObjectName() + ": " + error.getDefaultMessage();
                    }
                })
                .toList(); // Java 16+: returns an unmodifiable List
    }

    protected String formatConstraintViolation(ConstraintViolation<?> v) {
        String path = (v.getPropertyPath() != null) ? v.getPropertyPath().toString() : "<unknown>";
        return path + ": " + v.getMessage();
    }

    protected String extractPath(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            // For ServletWebRequest, getRequest() is guaranteed non-null
            return swr.getRequest().getRequestURI();
        }
        return "";
    }
}