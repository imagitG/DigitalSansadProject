package com.digitalSansad.sow.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /* ---------- 400 BAD REQUEST ---------- */

    @ExceptionHandler({
            IllegalArgumentException.class,
            InvalidStateException.class,
            FileValidationException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            Exception ex,
            HttpServletRequest request) {
        logger.warn("400 Bad Request - {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return build(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request);
    }

    /* ---------- 401 UNAUTHORIZED ---------- */

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            AuthenticationException ex,
            HttpServletRequest request) {
        logger.warn("401 Unauthorized - AuthenticationException: {}", ex.getMessage());
        return build(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed",
                request);
    }

    /* ---------- 403 FORBIDDEN ---------- */

    @ExceptionHandler({
            AccessDeniedException.class,
            AccessDeniedAppException.class
    })
    public ResponseEntity<ErrorResponse> handleForbidden(
            Exception ex,
            HttpServletRequest request) {
        logger.warn("403 Forbidden - {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return build(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request);
    }

    /* ---------- 404 NOT FOUND ---------- */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        logger.warn("404 Not Found - {}", ex.getMessage());
        return build(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request);
    }

    /* ---------- 500 FILE / IO ERRORS ---------- */

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleFileErrors(
            IOException ex,
            HttpServletRequest request) {
        logger.error("500 IO Error - IOException: {}", ex.getMessage(), ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "File processing error",
                request);
    }

    /*
     * ---------- 503 SERVICE UNAVAILABLE - EXTERNAL SERVICES / STORAGE ----------
     */

    @ExceptionHandler({
            R2StorageException.class,
            ExternalServiceException.class
    })
    public ResponseEntity<ErrorResponse> handleExternalServiceErrors(
            Exception ex,
            HttpServletRequest request) {
        logger.error("503 Service Unavailable - {}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return build(
                HttpStatus.SERVICE_UNAVAILABLE,
                "External service error: " + ex.getMessage(),
                request);
    }

    /* ---------- 500 FALLBACK (EXACTLY ONE) ---------- */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
            Exception ex,
            HttpServletRequest request) {
        logger.error("500 Internal Server Error - {}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected server error",
                request);
    }

    /* ---------- Helper ---------- */

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
