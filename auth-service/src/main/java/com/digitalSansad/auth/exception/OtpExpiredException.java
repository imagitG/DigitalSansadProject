package com.digitalSansad.auth.exception;

public class OtpExpiredException extends RuntimeException {
  public OtpExpiredException(String message) {
    super(message);
  }
}

// not being used anywhere
