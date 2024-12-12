package com.epam.user_service.service;

import com.epam.user_service.entity.ERole;
import com.epam.user_service.entity.Role;
import com.epam.user_service.dto.*;
import com.epam.user_service.entity.User;
import com.epam.user_service.exception.InvalidUserCredentials;
import com.epam.user_service.exception.UserException;
import com.epam.user_service.exception.UserNotFound;
import com.epam.user_service.repository.RoleRepository;
import com.epam.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.epam.user_service.exception.UserAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    static final byte[] DEFAULT_IMAGE;
    static {
        try {
            ClassPathResource resource = new ClassPathResource("static/default.jpg");
            DEFAULT_IMAGE = StreamUtils.copyToByteArray(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default image", e);
        }
    }



    @Override
    public String login(LoginRequest loginRequest) throws InvalidUserCredentials {
        try {
            logger.info("Processing login request..");
            Authentication authentication = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                logger.info("user has been authenticated.");
                String username = loginRequest.getUsername();
                User user = userRepository.findByUsername(username).orElseThrow(() -> new InvalidUserCredentials("User not found."));
                if(user.getIsEnabled() == 0){
                    throw new UserException("User is Disabled", HttpStatus.BAD_REQUEST);
                }

                user.setIsActive(1);
                userRepository.save(user);
                return this.tokenService.generateToken(authentication);
            }
        } catch (BadCredentialsException e) {
            throw new InvalidUserCredentials("Invalid username or password.");
        } catch (AuthenticationException e) {
            throw new InvalidUserCredentials("Authentication failed: " + e.getMessage());
        }
        throw new InvalidUserCredentials("Authentication was unsuccessful.");
    }


    public void logoutUser(String username) {
        User user = userRepository.findByUsername(username).get();

        user.setIsActive(0);
        userRepository.save(user);
    }



    @Override
    public UserRegistrationResponse registerUser(@Valid UserRegistrationRequest userRegistrationRequest) throws IOException {
        if (userRepository.existsByUsername(userRegistrationRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email " + userRegistrationRequest.getEmail() + " is already in use.");
        }

        String encodedPassword = passwordEncoder.encode(userRegistrationRequest.getPassword());

        User user = new User();
        user.setFirstName(userRegistrationRequest.getFirstName());
        user.setLastName(userRegistrationRequest.getLastName());
        user.setCity(userRegistrationRequest.getCity());
        user.setCountry(userRegistrationRequest.getCountry());
        user.setUsername(userRegistrationRequest.getEmail());
        user.setPassword(encodedPassword);
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.getRoles().add(userRole);
        UserRegistrationResponse response = new UserRegistrationResponse();

        if (userRegistrationRequest.getImage() != null && !userRegistrationRequest.getImage().isEmpty()) {
            try {
                user.setProfileImage(userRegistrationRequest.getImage().getBytes());
                response.setImageURL("Image Uploaded");
            } catch (IOException e) {
                throw new IOException("Error occurred while processing user profile image", e);
            }
        } else {
            user.setProfileImage(DEFAULT_IMAGE);
            response.setImageURL("You haven't uploaded image");
        }

        userRepository.save(user);
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getUsername());
        response.setCity(user.getCity());
        response.setCountry(user.getCountry());

        return response;
    }
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByUsername(email);
    }

    @Override
    public UserProfileResponse getUserById(String email) {
        User user=userRepository.findByUsername(email).orElseThrow(()-> new UserException("User not found with email " + email, HttpStatus.NOT_FOUND));
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setEmail(user.getUsername());
        userProfileResponse.setFirstName(user.getFirstName());
        userProfileResponse.setLastName(user.getLastName());
        userProfileResponse.setCity(user.getCity());
        userProfileResponse.setCountry(user.getCountry());
        userProfileResponse.setImageURL(getImageURL(user));
        return userProfileResponse;
    }

    @Override
    public MessageResponse updateUserById(UserProfileRequest userProfileRequest, String email) throws IOException {
        User user=userRepository.findByUsername(email).orElseThrow(()-> new UserException("User not found with email " + email,HttpStatus.NOT_FOUND));
        user.setFirstName(userProfileRequest.getFirstName());
        user.setLastName(userProfileRequest.getLastName());
        user.setCity(userProfileRequest.getCity());
        user.setCountry(userProfileRequest.getCountry());
        setPassword(userProfileRequest, user);
        if(userProfileRequest.getProfileImage() != null && !userProfileRequest.getProfileImage().isEmpty()) {
            user.setProfileImage(userProfileRequest.getProfileImage().getBytes());
        }

        userRepository.save(user);
        return new MessageResponse("User updated.");
    }


    private void setPassword(UserProfileRequest userProfileRequest, User user) {
        if (userProfileRequest.getPassword()!=null && !userProfileRequest.getPassword().isEmpty()) {
            if(userProfileRequest.getPassword().length() < 8){
                throw new UserException("Password length should be greater than 7",HttpStatus.BAD_REQUEST);
            }else {
                user.setPassword(passwordEncoder.encode(userProfileRequest.getPassword()));
            }
        }
    }

    private static String getImageURL(User user) {
        if(user.getProfileImage() == null){
            return "https://yourteachingmentor.com/wp-content/uploads/2020/12/istockphoto-1223671392-612x612-1.jpg";
        }
        return "data:" + getMimeType(user.getProfileImage()) + ";base64," + Base64.getEncoder().encodeToString(user.getProfileImage());
    }

    public static String getMimeType(byte[] imageData) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(imageData)) {
            return URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            System.err.println("Error determining MIME type: " + e.getMessage());
            return "application/octet-stream";
        }
    }
}
