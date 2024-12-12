package com.epam.user_service.controller;

import com.epam.user_service.dto.LoginRequest;
import com.epam.user_service.exception.InvalidUserCredentials;
import com.epam.user_service.repository.UserRepository;
import com.epam.user_service.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import com.epam.user_service.dto.*;
import com.epam.user_service.dto.UserProfileRequest;
import com.epam.user_service.dto.UserProfileResponse;
import com.epam.user_service.dto.MessageResponse;
import com.epam.user_service.exception.UserException;
import com.epam.user_service.handler.GlobalHandler;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.epam.user_service.dto.OtpRequest;
import com.epam.user_service.dto.UpdatePasswordRequest;
import com.epam.user_service.service.PasswordServiceImpl;

import com.epam.user_service.service.PasswordService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {


    @InjectMocks
    private UserController userController;
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private GlobalHandler globalHandler;

    private MockMvc mockMvc;
    private UserProfileResponse testUserProfileResponse;
    private UserProfileRequest testUserProfileRequest;
    @Mock
    private PasswordServiceImpl passwordServiceImpl;

    @Mock
    private PasswordService passwordService;

    private UserRegistrationRequest validRequest;
    private UserRegistrationResponse validResponse;

       @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        validRequest = new UserRegistrationRequest(
                "John", "Doe", "john@example.com", "New York", "USA", "Password123", null);
        validResponse = new UserRegistrationResponse(
                "John", "Doe", "john@example.com", "New York", "USA", "You haven't uploaded image");

        userController = new UserController(userService, passwordService);

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalHandler)
                .build();

        testUserProfileResponse = new UserProfileResponse();
        testUserProfileResponse.setEmail("test@example.com");
        testUserProfileResponse.setFirstName("John");
        testUserProfileResponse.setLastName("Doe");
        testUserProfileResponse.setCity("New York");
        testUserProfileResponse.setCountry("USA");
        testUserProfileResponse.setImageURL("data:image/jpeg;base64,/9j/4AAQSkZJRg==");

        testUserProfileRequest = new UserProfileRequest();
        testUserProfileRequest.setFirstName("Jane");
        testUserProfileRequest.setLastName("Smith");
        testUserProfileRequest.setCity("Los Angeles");
        testUserProfileRequest.setCountry("USA");

    }

    @Test
    void testLogin_Success() throws InvalidUserCredentials {

        LoginRequest loginRequest = new LoginRequest("username", "password");
        String expectedToken = "token123";

        when(userService.login(any(LoginRequest.class))).thenReturn(expectedToken);

        ResponseEntity<LoginResponse> response = userController.login(loginRequest);

        assertEquals(expectedToken, response.getBody().getToken());
    }


    @Test
    void testLogin_InvalidUserCredentials() throws InvalidUserCredentials {

        LoginRequest loginRequest = new LoginRequest("invalidUser", "invalidPassword");

        doThrow(new InvalidUserCredentials("Invalid credentials")).when(userService).login(loginRequest);

        try {
            userController.login(loginRequest);
        } catch (InvalidUserCredentials e) {
            assertEquals("Invalid credentials", e.getMessage());
        }
    }

    @Test
     void testLogout_Success() {
        String userId = "userId123";
        ResponseEntity<String> response = userController.logout(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Successfully logged out.", response.getBody());
    }

    

    @Test
    void testRegisterUser_ValidRequest_ReturnsCreated() throws IOException {
        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(validResponse);

        ResponseEntity<UserRegistrationResponse> responseEntity = userController.registerUser(validRequest);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(validResponse, responseEntity.getBody());
    }

    @Test
    void testRegisterUser_WithImage_ReturnsCreated() throws IOException {
        MockMultipartFile image = new MockMultipartFile("image", "test.png", "image/png", "test image content".getBytes());
        UserRegistrationRequest requestWithImage = new UserRegistrationRequest(
                "John", "Doe", "john@example.com", "New York", "USA", "Password123", image);
        UserRegistrationResponse responseWithImage = new UserRegistrationResponse(
                "John", "Doe", "john@example.com", "New York", "USA", "Image Uploaded");

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(responseWithImage);

        ResponseEntity<UserRegistrationResponse> responseEntity = userController.registerUser(requestWithImage);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(responseWithImage, responseEntity.getBody());
    }

    @Test
    void testGetOtp_ValidEmail_ReturnsOkResponse() {
        String email = "test@example.com";
        String expectedMessage = "OTP sent successfully";
        when(passwordService.creatingOtp(email)).thenReturn(expectedMessage);

        OtpRequest otpRequest = new OtpRequest(email);
        ResponseEntity<MessageResponse> responseEntity = userController.getOtp(otpRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedMessage, responseEntity.getBody().getMessage());
    }

    @Test
    void testUpdatePassword_ValidInput_ReturnsOkResponse() {
        String email = "test@example.com";
        String password = "NewPassword123";
        String otp = "123456";
        String expectedMessage = "Password updated successfully";
        when(passwordService.updatePassword(email, password, otp)).thenReturn(expectedMessage);


        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest(email, password, otp);
        ResponseEntity<MessageResponse> responseEntity = userController.updatePassword(updatePasswordRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    void testRegisterUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
                "", "", "invalidemail", "", "", "", null);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .flashAttr("userRegistrationRequest", invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserProfile_Success() {
        // Arrange
        String email = "test@example.com";
        when(userService.getUserById(email)).thenReturn(testUserProfileResponse);

        // Act
        UserProfileResponse response = userController.getUserProfile(email).getBody();

        // Assert
        assertEquals(testUserProfileResponse.getEmail(), response.getEmail());
        assertEquals(testUserProfileResponse.getFirstName(), response.getFirstName());
        assertEquals(testUserProfileResponse.getLastName(), response.getLastName());
        assertEquals(testUserProfileResponse.getCity(), response.getCity());
        assertEquals(testUserProfileResponse.getCountry(), response.getCountry());
        assertEquals(testUserProfileResponse.getImageURL(), response.getImageURL());

        verify(userService, times(1)).getUserById(email);
    }

    @Test
    void getUserProfile_UserNotFound() {
        // Arrange
        String email = "test@example.com";
        when(userService.getUserById(email)).thenThrow(new UserException("User not found with email " + email, HttpStatus.NOT_FOUND));

        // Act & Assert
        Exception exception = assertThrows(UserException.class, () -> {
            userController.getUserProfile(email);
        }, "User not found with email " + email);

        verify(userService, times(1)).getUserById(email);
    }

    @Test
    void updateUserProfile_Success() throws IOException {
        // Arrange
        String email = "test@example.com";
        MessageResponse expectedResponse = new MessageResponse("User updated.");
        when(userService.updateUserById(any(UserProfileRequest.class), anyString())).thenReturn(expectedResponse);

        // Act
        MessageResponse response = userController.updateUserProfile(testUserProfileRequest, email).getBody();

        // Assert
        assertEquals(expectedResponse.getMessage(), response.getMessage());

        verify(userService, times(1)).updateUserById(any(UserProfileRequest.class), anyString());
    }

    @Test
    void updateUserProfile_UserNotFound() throws IOException {
        // Arrange
        String email = "test@example.com";
        when(userService.updateUserById(any(UserProfileRequest.class), anyString()))
                .thenThrow(new UserException("User not found with email " + email, HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(UserException.class, () -> {
            userController.updateUserProfile(testUserProfileRequest, email);
        }, "User not found with email " + email);

        verify(userService, times(1)).updateUserById(any(UserProfileRequest.class), anyString());
    }


}

