package com.epam.user_service.service;

import com.epam.user_service.dto.*;
import com.epam.user_service.entity.User;
import com.epam.user_service.exception.InvalidUserCredentials;

import java.io.IOException;
import java.util.Optional;


public interface UserService {
    String login(LoginRequest loginRequest)  throws InvalidUserCredentials;
    UserRegistrationResponse registerUser(UserRegistrationRequest userRegistrationRequest) throws IOException;
    void logoutUser(String username);
    Optional<User>findByEmail(String email);
    UserProfileResponse getUserById(String email);
    MessageResponse updateUserById(UserProfileRequest userProfileRequest, String email) throws IOException;
}
