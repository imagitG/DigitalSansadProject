package com.digitalSansad.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<?> handleUserExists(UserAlreadyExistsException ex) {
    return build(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(OtpExpiredException.class)
  public ResponseEntity<?> handleOtpExpired(OtpExpiredException ex) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(OtpInvalidException.class)
  public ResponseEntity<?> handleOtpInvalid(OtpInvalidException ex) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  private ResponseEntity<Map<String, Object>> build(
      HttpStatus status,
      String message) {
    return ResponseEntity.status(status).body(
        Map.of(
            "timestamp", Instant.now(),
            "status", status.value(),
            "error", message));
  }
}
