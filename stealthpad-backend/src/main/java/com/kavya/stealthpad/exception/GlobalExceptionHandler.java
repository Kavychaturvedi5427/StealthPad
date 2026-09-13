package com.kavya.stealthpad.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import com.kavya.stealthpad.Dto.ErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return response(HttpStatus.BAD_REQUEST, ErrorResponseDto.validation(request.getRequestURI(), errors));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodValidation(HandlerMethodValidationException exception,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ParameterValidationResult result : exception.getParameterValidationResults()) {
            String field = result.getMethodParameter().getParameterName();
            if (field == null) {
                field = "parameter";
            }
            if (!result.getResolvableErrors().isEmpty()) {
                errors.putIfAbsent(field, result.getResolvableErrors().getFirst().getDefaultMessage());
            }
        }
        return response(HttpStatus.BAD_REQUEST, ErrorResponseDto.validation(request.getRequestURI(), errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMalformedJson(HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed JSON request", request);
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            BadRequestException.class })
    public ResponseEntity<ErrorResponseDto> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message = exception instanceof BadRequestException
                ? exception.getMessage()
                : "The request could not be processed";
        return error(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(ConflictException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Conflict", exception.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthentication(AuthenticationException exception, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to access this resource", request);
    }

    @ExceptionHandler(TransientAiException.class)
    public ResponseEntity<ErrorResponseDto> handleTransientAiFailure(TransientAiException exception, HttpServletRequest request) {
        log.warn("AI service failure method={} path={} exceptionType={}", request.getMethod(),
                request.getRequestURI(), exception.getClass().getName());
        return error(HttpStatus.SERVICE_UNAVAILABLE, "AI Service Error",
                "AI service is temporarily unavailable", request);
    }

    @ExceptionHandler(NonTransientAiException.class)
    public ResponseEntity<ErrorResponseDto> handleNonTransientAiFailure(NonTransientAiException exception,
            HttpServletRequest request) {
        log.warn("AI provider failure method={} path={} exceptionType={}", request.getMethod(),
                request.getRequestURI(), exception.getClass().getName());
        return error(HttpStatus.BAD_GATEWAY, "AI Service Error", "AI service could not process the request", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Conflict", "The request conflicts with existing data", request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpectedRuntimeException(RuntimeException exception,
            HttpServletRequest request) {
        return handleUnexpected(exception, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        return handleUnexpected(exception, request);
    }

    private ResponseEntity<ErrorResponseDto> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception method={} path={} exceptionType={}", request.getMethod(),
                request.getRequestURI(), exception.getClass().getName(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponseDto> error(HttpStatus status, String error, String message,
            HttpServletRequest request) {
        return response(status, ErrorResponseDto.of(status.value(), error, message, request.getRequestURI()));
    }

    private ResponseEntity<ErrorResponseDto> response(HttpStatus status, ErrorResponseDto body) {
        return ResponseEntity.status(status).body(body);
    }
}