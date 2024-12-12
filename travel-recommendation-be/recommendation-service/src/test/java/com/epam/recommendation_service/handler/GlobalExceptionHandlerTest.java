package com.epam.recommendation_service.handler;

import com.epam.recommendation_service.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleBlogNotFoundException() {
        BlogNotFoundException exception = new BlogNotFoundException("Blog not found");
        ProblemDetail problemDetail = globalExceptionHandler.handleBlogNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Blog not found", problemDetail.getDetail());
        assertEquals("Blog Not Found", problemDetail.getProperties().get("error"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    void handleException() {
        Exception exception = new Exception("Unexpected error");
        ProblemDetail problemDetail = globalExceptionHandler.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Unexpected error", problemDetail.getDetail());
        assertEquals("An unexpected error occurred", problemDetail.getProperties().get("error"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    void handleUserNotFound() {
        UserNotFoundException exception = new UserNotFoundException("User not found");
        ProblemDetail problemDetail = globalExceptionHandler.handleUserNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("User not found", problemDetail.getDetail());
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    void handleBlogAlreadyPresentException() {
        BlogAlreadyPresentException exception = new BlogAlreadyPresentException("Blog already present");
        ProblemDetail problemDetail = globalExceptionHandler.handleUserAlreadyPresent(exception);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Blog already present", problemDetail.getDetail());
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    void handleFavouriteBlogNotFoundException() {
        FavouriteBlogNotFoundException exception = new FavouriteBlogNotFoundException("Favorite blog not found");
        ProblemDetail problemDetail = globalExceptionHandler.handleBlogNotFavoriteNotException(exception);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Favorite blog not found", problemDetail.getDetail());
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    void methodArgumentNotValidException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(new FieldError("object", "field", "default message")));

        ProblemDetail problemDetail = globalExceptionHandler.methodArgumentNotValidException(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Request is invalid", problemDetail.getDetail());
        assertNotNull(problemDetail.getProperties().get("timestamp"));
        assertTrue(problemDetail.getProperties().containsKey("field"));
    }

    @Test
    void handleUnauthorizedBlogAccessException() {
        UnauthorizedBlogAccessException exception = new UnauthorizedBlogAccessException("Unauthorized access");
        ProblemDetail problemDetail = globalExceptionHandler.handleUnauthorizedBlogAccessException(exception);

        assertEquals(HttpStatus.FORBIDDEN.value(), problemDetail.getStatus());
        assertEquals("Unauthorized access", problemDetail.getDetail());
        assertEquals("Unauthorized Access", problemDetail.getProperties().get("error"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));
    }

    @Test
    void handleCommentNotFoundException() {
        String message = "Comment not found";
        CommentNotFound exception = new CommentNotFound(message);

        ProblemDetail problemDetail = globalExceptionHandler.handleCommentNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals(message, problemDetail.getDetail());
        assertNotNull(problemDetail.getProperties().get("timestamp"));
        assertEquals("Comment not found", problemDetail.getProperties().get("error"));
    }

    @Test
    void handleBlogException() {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal server error";
        DuplicateReportException exception = new DuplicateReportException(status, message);

        ProblemDetail problemDetail = globalExceptionHandler.handleBlogException(exception);

        assertEquals(status.value(), problemDetail.getStatus());
        assertEquals(message, problemDetail.getDetail());
        assertNotNull(problemDetail.getProperties().get("timestamp"));
        assertEquals(message, problemDetail.getProperties().get("error"));
    }
}