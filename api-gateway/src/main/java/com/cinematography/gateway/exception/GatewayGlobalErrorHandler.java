package com.cinematography.gateway.exception;

import java.util.List;

import org.springframework.web.server.WebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.cinematography.core.dto.ApiResponse;
import com.cinematography.gateway.config.ApiResponseCode;

import reactor.core.publisher.Mono;

@Component
@Order(-2) // ✅ MUST be before DefaultErrorWebExceptionHandler (-1)
public class GatewayGlobalErrorHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            // ✅ allow framework to continue
            return Mono.error(ex);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // ✅ NO ROUTE FOUND (Gateway 404)
        if (ex instanceof NotFoundException
                || ex instanceof NoResourceFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        // ✅ Status-based exceptions
        else if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.resolve(rse.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(ApiResponseCode.ERROR);
        apiResponse.setStatusCode(status.value());

        if (status == HttpStatus.NOT_FOUND) {
            apiResponse.setMessages(List.of(
                ApiResponseCode.ERROR_MSG,
                "Resource Not Found"
            ));
        } else if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            apiResponse.setMessages(List.of(
                ApiResponseCode.ERROR_MSG,
                "Service temporarily unavailable. Please try again later."
            ));
        } else {
            apiResponse.setMessages(List.of(
                ApiResponseCode.ERROR_MSG,
                "Unexpected gateway error"
            ));
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body = ApiResponse.toByteArray(apiResponse);
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(body))
        );
    }
}