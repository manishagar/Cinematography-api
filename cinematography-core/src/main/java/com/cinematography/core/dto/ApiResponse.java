package com.cinematography.core.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.cinematography.core.constants.CommonResponseCodes;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response envelope returned by every service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(value = Include.NON_EMPTY)
@XmlRootElement(name = "apiResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApiResponse<T> {

    // success / warning / error
    @NotBlank
    @Size(max = 7)
    private String status;

    // 2XX, 1XX, 4XX, 5XX
    @NotNull
    private Integer statusCode;

    // Message list (may be empty). Do not use @NotBlank on collections.
    @NotNull
    private List<String> messages;

    private T data;

    public static <T> ApiResponse<T> empty() {
        // Historically you returned BAD_REQUEST here; adjust if you want OK
        return success(null, new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    public static <T> ApiResponse<T> success(T data, List<String> success, HttpStatus httpStatus) {
        List<String> out = copyAndPrepend(success, CommonResponseCodes.SUCCESS_MSG);
        return ApiResponse.<T>builder()
                .status(CommonResponseCodes.SUCCESS)
                .statusCode(httpStatus.value())
                .messages(out)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> warning(T data, List<String> warning, HttpStatus httpStatus) {
        List<String> out = copyAndPrepend(warning, CommonResponseCodes.WARNING_MSG);
        return ApiResponse.<T>builder()
                .status(CommonResponseCodes.WARNING)
                .statusCode(httpStatus.value())
                .messages(out)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(List<String> error, HttpStatus httpStatus) {
        List<String> out = copyAndPrepend(error, CommonResponseCodes.ERROR_MSG);
        return ApiResponse.<T>builder()
                .status(CommonResponseCodes.ERROR)
                .statusCode(httpStatus.value())
                .messages(out)
                .data(null)
                .build();
    }

    // ---------- Optional convenience overloads ----------
    public static <T> ApiResponse<T> success(T data) {
        return success(data, new ArrayList<>(), HttpStatus.OK);
    }

    // ---------- Internal helper ----------
    private static List<String> copyAndPrepend(List<String> messages, String first) {
        // Create a mutable copy (handles null and unmodifiable lists)
        List<String> out = (messages == null) ? new ArrayList<>() : new ArrayList<>(messages);
        out.add(0, first);
        return out;
    }

    // Method to convert ApiResponse to byte array (used by api-gateway to write error bodies)
    public static <T> byte[] toByteArray(ApiResponse<T> apiResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsBytes(apiResponse);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize ApiResponse", e);
        }
    }
}
