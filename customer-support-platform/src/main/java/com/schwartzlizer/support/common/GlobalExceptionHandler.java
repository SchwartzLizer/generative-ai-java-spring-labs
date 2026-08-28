package com.schwartzlizer.support.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.schwartzlizer.support.response.AnalysisRequiredException;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Clock clock;
    public GlobalExceptionHandler(Clock clock) { this.clock = clock; }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", request, fields);
    }
    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest request) { return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage() == null ? "Invalid request" : ex.getMessage(), request, Map.of()); }
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest request) { return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request, Map.of()); }
    @ExceptionHandler(InvalidStateTransitionException.class)
    ResponseEntity<ApiError> conflict(InvalidStateTransitionException ex, HttpServletRequest request) { return response(HttpStatus.CONFLICT, "INVALID_STATE_TRANSITION", ex.getMessage(), request, Map.of()); }
    @ExceptionHandler(AnalysisRequiredException.class)
    ResponseEntity<ApiError> analysisRequired(AnalysisRequiredException ex, HttpServletRequest request) { return response(HttpStatus.CONFLICT, "ANALYSIS_REQUIRED", ex.getMessage(), request, Map.of()); }
    @ExceptionHandler(AiProviderException.class)
    ResponseEntity<ApiError> provider(AiProviderException ex, HttpServletRequest request) { return response(HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_UNAVAILABLE", "The AI provider is temporarily unavailable", request, Map.of()); }
    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message, HttpServletRequest request, Map<String,String> fields) { return ResponseEntity.status(status).body(new ApiError(code, message, Instant.now(clock), request.getRequestURI(), fields)); }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) { return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid identifier or parameter", request, Map.of()); }
}
