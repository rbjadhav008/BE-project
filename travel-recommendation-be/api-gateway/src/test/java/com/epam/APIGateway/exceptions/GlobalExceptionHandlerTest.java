package com.epam.APIGateway.exceptions;

import com.epam.APIGateway.dto.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleInvalidTokenException_ReturnsUnauthorizedResponse() {
        // Arrange
        String errorMessage = "Invalid token";
        InvalidTokenException exception = new InvalidTokenException(errorMessage);

        // Act
        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleInvalidTokenException(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(errorMessage, response.getBody().getMessage());
    }
}