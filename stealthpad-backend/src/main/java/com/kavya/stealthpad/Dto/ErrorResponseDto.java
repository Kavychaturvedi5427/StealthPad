package com.kavya.stealthpad.Dto;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> errors;

    private ErrorResponseDto(int status, String error, String message, String path,
            Map<String, String> errors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public static ErrorResponseDto of(int status, String error, String message, String path) {
        return new ErrorResponseDto(status, error, message, path, null);
    }

    public static ErrorResponseDto validation(String path, Map<String, String> errors) {
        return new ErrorResponseDto(400, "Validation Error", "Validation failed", path, errors);
    }
}