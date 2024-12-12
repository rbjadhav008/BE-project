package com.epam.recommendation_service.exception;

public class UnauthorizedBlogAccessException extends Exception {
    public UnauthorizedBlogAccessException(String message) {
        super(message);
    }
}
