package com.epam.recommendation_service.exception;

public class InvalidBlogStatusException extends Exception{
    public InvalidBlogStatusException(String message) {
        super(message);
    }
}
