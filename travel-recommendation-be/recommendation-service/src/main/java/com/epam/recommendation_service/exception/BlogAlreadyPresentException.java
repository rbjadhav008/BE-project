package com.epam.recommendation_service.exception;

public class BlogAlreadyPresentException extends RuntimeException {
  public BlogAlreadyPresentException(String message) {
    super(message);
  }
}
