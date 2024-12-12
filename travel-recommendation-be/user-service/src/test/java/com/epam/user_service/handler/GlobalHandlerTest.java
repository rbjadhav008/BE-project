package com.epam.user_service.handler;

import com.epam.user_service.dto.MessageResponse;
import com.epam.user_service.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.mockito.MockitoAnnotations;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


@Nested
class GlobalHandlerTest {

    private GlobalHandler exceptionHandler;
    private GlobalHandler globalHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;
    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    public void setUp() {
        exceptionHandler = new GlobalHandler();
        bindingResult = mock(BindingResult.class);
        MockitoAnnotations.openMocks(this);
        globalHandler = new GlobalHandler();
    }

    @Test
    void methodArgumentNotValidException_ShouldReturnBadRequest() {
        FieldError fieldError = new FieldError("user", "email", "Email must not be empty");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ProblemDetail> response = exceptionHandler.methodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();
        assertNotNull(problemDetail);
        assertEquals("Request is invalid", problemDetail.getDetail());
        assertTrue(problemDetail.getProperties().containsKey("email"));
        assertEquals("Email must not be empty", problemDetail.getProperties().get("email"));
    }

    @Test
    void handleDuplicateUserException_ShouldReturnConflict() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("Email is already in use.");

        ResponseEntity<Object> response = exceptionHandler.handleDuplicateUserException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("error"));
        assertEquals("Email is already in use.", body.get("error"));
    }

    @Test
    void handleUserNotFound_ShouldReturnBadRequest() {
        UserNotFound ex = new UserNotFound("User not found");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUserNotFound(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
        assertEquals("User not found", body.get("error"));
    }

    @Test
    void handleOtpMismatchException_ShouldReturnBadRequest() {
        OtpMismatchException ex = new OtpMismatchException("OTP mismatch");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleOtpMismatchException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
        assertEquals("OTP mismatch", body.get("error"));
    }

    @Test
    void handleRuntimeException_ShouldReturnBadRequest() {
        OtpMismatchException ex = new OtpMismatchException("Something went wrong");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
        assertEquals("Something went wrong", body.get("error"));
    }


    @Test
    void testMethodArgumentNotValidException_withFieldErrors() {
        FieldError fieldError = new FieldError("objectName", "field", "must not be null");
        List<FieldError> errors = Collections.singletonList(fieldError);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(errors);

        ProblemDetail problemDetail = globalHandler.methodArgumentNotValidException(methodArgumentNotValidException).getBody();

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Bad Request", problemDetail.getTitle());
        assertEquals("must not be null", problemDetail.getProperties().get("field"));
    }

    @Test
    void testMethodArgumentNotValidException_noFieldErrors() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

        ProblemDetail problemDetail = globalHandler.methodArgumentNotValidException(methodArgumentNotValidException).getBody();

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Bad Request", problemDetail.getTitle());
        assertEquals(Collections.emptyMap(), problemDetail.getProperties());
    }

    @Test
    void testUserException() {
        String errorMessage = "User not found";
        UserException userException = new UserException("User not found",HttpStatus.NOT_FOUND);

        ProblemDetail problemDetail = globalHandler.userException(userException);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Not Found", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
    }

    @Test
    void handleDuplicateUserException_ReturnsConflictResponse() {
        // Arrange
        String errorMessage = "User already exists";
        FieldAlreadyExistsException exception = new FieldAlreadyExistsException(errorMessage);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDuplicateUserException(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.get("timestamp") instanceof LocalDateTime);
        assertEquals(errorMessage, body.get("error"));
    }

    @Test
    void handleDuplicateUserException_ReturnsExpectedResponseBody() {
        // Arrange
        String errorMessage = "User already exists";
        FieldAlreadyExistsException exception = new FieldAlreadyExistsException(errorMessage);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDuplicateUserException(exception);

        // Assert
        Map<String, Object> expectedBody = new LinkedHashMap<>();
        expectedBody.put("error", errorMessage);

        Map<String, Object> actualBody = (Map<String, Object>) response.getBody();
        assertNotNull(actualBody);
        assertTrue(actualBody.containsKey("timestamp"));
        actualBody.remove("timestamp");

        assertEquals(expectedBody, actualBody);
    }

    @Test
    void handleInvalidUserCredentials_ReturnsUnauthorizedResponse() {
        // Arrange
        String errorMessage = "Invalid username or password";
        InvalidUserCredentials exception = new InvalidUserCredentials(errorMessage);

        // Act
        ResponseEntity<MessageResponse> response = exceptionHandler.handleInvalidUserCredentials(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        MessageResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getMessage());
    }
}