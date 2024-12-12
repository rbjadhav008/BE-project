package com.epam.recommendation_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DuplicateReportException extends RuntimeException {
  private HttpStatus httpStatus;
  public DuplicateReportException(HttpStatus httpStatus, String message) {
    super(message);
    this.httpStatus = httpStatus;
  }
}
