package com.epam.recommendation_service.exception;

public class FavouriteBlogNotFoundException extends RuntimeException {
    public FavouriteBlogNotFoundException(String message) {
        super(message);
    }
}
