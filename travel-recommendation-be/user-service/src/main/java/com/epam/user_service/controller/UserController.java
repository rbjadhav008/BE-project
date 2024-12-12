package com.epam.user_service.controller;
import com.epam.user_service.dto.*;
import com.epam.user_service.entity.User;
import com.epam.user_service.exception.InvalidUserCredentials;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.epam.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;

import com.epam.user_service.dto.OtpRequest;
import com.epam.user_service.dto.UpdatePasswordRequest;
import com.epam.user_service.service.PasswordService;

@RestController
@RequestMapping("api/users")
@AllArgsConstructor
@Validated
public class UserController {


    private final UserService userService;
    private final PasswordService passwordService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) throws InvalidUserCredentials {
        String token = this.userService.login(loginRequest);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @GetMapping("/getUser")
    public User getUserByEmail(@RequestParam("email") String email){
        return userService.findByEmail(email).orElse(User.builder().build());
    }

    @PutMapping("/logout")
    public ResponseEntity<String> logout (@RequestHeader("X-User-Id") String user) {
        userService.logoutUser(user);
        return ResponseEntity.ok("Successfully logged out.");
    }

    @GetMapping("profile/{email}")
    public UserProfileResponse getUserProfileByEmail(@PathVariable String email) {
        return userService.getUserById(email);
    }


    @GetMapping("profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@RequestHeader("X-User-Id") String email) {
        return ResponseEntity.ok(userService.getUserById(email));
    }

    @PutMapping("profile")
    public ResponseEntity<MessageResponse> updateUserProfile(@Valid @ModelAttribute UserProfileRequest userProfileRequest, @RequestHeader("X-User-Id") String email) throws IOException {
        return ResponseEntity.ok(userService.updateUserById(userProfileRequest, email));
    }


    @PostMapping("/register")
     @Operation(
            summary = "Register a new user",
            description = "Registers a new user in the system and returns the user data along with a success message. " +
                    "Pre-validations include checking the format of email, ensuring the password meets complexity requirements, " +
                    "and verifying that the username doesn't already exist in the system.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserRegistrationResponse.class))),
                    @ApiResponse(responseCode = "409", description = "User already exists or validation failed", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Bad Request if validation fails", content = @Content)
            }
     )
    public ResponseEntity<UserRegistrationResponse> registerUser( @Valid @ModelAttribute  UserRegistrationRequest userRegistrationRequest) throws IOException {
        return new ResponseEntity<>(userService.registerUser(userRegistrationRequest), HttpStatus.CREATED);
    }

    @PostMapping("/getotp")
    public ResponseEntity<MessageResponse> getOtp(@Valid @RequestBody OtpRequest otpRequest) {
        String result = passwordService.creatingOtp(otpRequest.getEmail());
        return ResponseEntity.ok(new MessageResponse(result));
    }

    @PutMapping("/updatePassword")
    public ResponseEntity<MessageResponse>updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
        String responseMessage = passwordService.updatePassword(updatePasswordRequest.getEmail(), updatePasswordRequest.getPassword(), updatePasswordRequest.getOtp());
        return ResponseEntity.ok(new MessageResponse(responseMessage));
    }
}