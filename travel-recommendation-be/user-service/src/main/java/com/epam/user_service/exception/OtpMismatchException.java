package com.epam.user_service.exception;

public class OtpMismatchException extends RuntimeException {
    public OtpMismatchException(String message) {
        super(message);
    }
}
