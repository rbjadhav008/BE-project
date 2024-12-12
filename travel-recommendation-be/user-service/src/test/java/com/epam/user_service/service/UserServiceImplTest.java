package com.epam.user_service.service;

import com.epam.user_service.dto.*;
import com.epam.user_service.entity.ERole;
import com.epam.user_service.entity.Role;
import com.epam.user_service.entity.User;
import com.epam.user_service.exception.InvalidUserCredentials;
import com.epam.user_service.exception.UserAlreadyExistsException;
import com.epam.user_service.exception.UserException;
import com.epam.user_service.repository.RoleRepository;
import com.epam.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    Authentication authentication;


    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationRequest validRequest;
    private User user;
    @Mock
    private MultipartFile mockMultipartFile;

    @TempDir
    Path tempDir;

    private User testUser;
    private UserProfileRequest testUserProfileRequest;
    private LoginRequest loginRequest;



    @BeforeEach
    void setUp() {
        validRequest = new UserRegistrationRequest("John", "Doe", "john@example.com", "CityName", "CountryName", "password", null);
        user = new User("encodedPassword", "CountryName", "CityName", 1, "john@example.com", "Doe", "John");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setCity("New York");
        testUser.setCountry("USA");
        testUser.setProfileImage(new byte[]{1, 2, 3, 4});

        testUserProfileRequest = new UserProfileRequest();
        testUserProfileRequest.setFirstName("Jane");
        testUserProfileRequest.setLastName("Smith");
        testUserProfileRequest.setCity("Los Angeles");
        testUserProfileRequest.setCountry("USA");
    }

    @Test
    void testDefaultImageLoadFailure() {
        // Arrange
        Path nonExistentFile = tempDir.resolve("non-existent.jpg");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            try {
                ClassPathResource resource = new ClassPathResource(nonExistentFile.toString());
                StreamUtils.copyToByteArray(resource.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException("Failed to load default image", e);
            }
        });
    }

    @Test
    void registerUser_ValidRequest_Success() throws IOException {
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.existsByUsername(validRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn(user.getPassword());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserRegistrationResponse response = userService.registerUser(validRequest);

        assertNotNull(response);
        assertEquals(validRequest.getFirstName(), response.getFirstName());
        assertEquals(validRequest.getLastName(), response.getLastName());
        assertEquals(validRequest.getEmail(), response.getEmail());
        assertEquals(validRequest.getCity(), response.getCity());
        assertEquals(validRequest.getCountry(), response.getCountry());
        assertEquals("You haven't uploaded image", response.getImageURL());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_UserAlreadyExists_ThrowsException() {
        when(userRepository.existsByUsername(validRequest.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(validRequest));

        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void registerUser_ImageConversionException_ThrowsRuntimeException() throws IOException {
        // Mocking the role repository to return a valid role
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

        // Mocking the multipart file to throw an IOException
        MultipartFile mockImage = mock(MultipartFile.class);
        when(mockImage.getBytes()).thenThrow(IOException.class);

        // Setting up the user registration request
        UserRegistrationRequest requestWithImage = new UserRegistrationRequest("John", "Doe", "john@example.com", "CityName", "CountryName", "password", mockImage);

        // Mocking repository and encoder behaviors
        when(userRepository.existsByUsername(requestWithImage.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(requestWithImage.getPassword())).thenReturn("encodedPassword");

        // Asserting the expected exception and verifying interactions
        assertThrows(IOException.class, () -> userService.registerUser(requestWithImage));
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void registerUser_NoImage_Success() throws IOException {
        UserRegistrationRequest requestWithoutImage = new UserRegistrationRequest("John", "Doe", "john@example.com", "CityName", "CountryName", "password", null);
        User userWithoutImage = new User("encodedPassword", "CountryName", "CityName", 1, "john@example.com", "Doe", "John");

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

        when(userRepository.existsByUsername(requestWithoutImage.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(requestWithoutImage.getPassword())).thenReturn(userWithoutImage.getPassword());
        when(userRepository.save(any(User.class))).thenReturn(userWithoutImage);

        UserRegistrationResponse response = userService.registerUser(requestWithoutImage);

        assertNotNull(response);
        assertEquals("You haven't uploaded image", response.getImageURL());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        // Arrange
        String email = "john@example.com";
        User expectedUser = new User("encodedPassword", "CountryName", "CityName", 1, email, "Doe", "John");
        when(userRepository.findByUsername(email)).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertEquals(expectedUser, result.get());
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmptyOptional() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByUsername(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertEquals(Optional.empty(), result);
    }

    @Test
    void registerUser_NullProfileImage_ReturnsImageUploadedMessage() throws IOException {
        // Arrange
        UserRegistrationRequest requestWithoutImage = new UserRegistrationRequest("John", "Doe", "john@example.com", "CityName", "CountryName", "password", null);
        User userWithNullImage = new User(1L,"encodedPassword", "CountryName", "CityName", "1", 0, "Doe", "John", null,1, Set.of());

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

        when(userRepository.existsByUsername(requestWithoutImage.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(requestWithoutImage.getPassword())).thenReturn(userWithNullImage.getPassword());
        when(userRepository.save(any(User.class))).thenReturn(userWithNullImage);

        // Act
        UserRegistrationResponse response = userService.registerUser(requestWithoutImage);

        // Assert
        assertEquals("You haven't uploaded image" +
                "", response.getImageURL());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        UserProfileResponse response = userService.getUserById("test@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getUsername(), response.getEmail());
        assertEquals(testUser.getFirstName(), response.getFirstName());
        assertEquals(testUser.getLastName(), response.getLastName());
        assertEquals(testUser.getCity(), response.getCity());
        assertEquals(testUser.getCountry(), response.getCountry());
        assertTrue(response.getImageURL().startsWith("data:"));
        verify(userRepository, times(1)).findByUsername("test@example.com");
    }

    @Test
    void getUserById_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        UserException exception = assertThrows(UserException.class,
                () -> userService.getUserById("test@example.com"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void updateUserById_Success() throws IOException {
        // Arrange
        testUserProfileRequest.setPassword(""); // Set empty password instead of null
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        MessageResponse response = userService.updateUserById(testUserProfileRequest, "test@example.com");

        // Assert
        assertNotNull(response);
        assertEquals("User updated.", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserById_WithPassword_Success() throws IOException {
        // Arrange
        testUserProfileRequest.setPassword("newPassword");
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        MessageResponse response = userService.updateUserById(testUserProfileRequest, "test@example.com");

        // Assert
        assertNotNull(response);
        assertEquals("User updated.", response.getMessage());
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }


    @Test
    void updateUserById_WithProfileImage_Success() throws IOException {
        // Arrange
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        testUserProfileRequest.setProfileImage(profileImage);
        testUserProfileRequest.setPassword(""); // Set empty password instead of null
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        MessageResponse response = userService.updateUserById(testUserProfileRequest, "test@example.com");

        // Assert
        assertNotNull(response);
        assertEquals("User updated.", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserById_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        UserException exception = assertThrows(UserException.class,
                () -> userService.updateUserById(testUserProfileRequest, "test@example.com"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void getMimeType_Success() {
        // Arrange
        byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};

        // Act
        String mimeType = UserServiceImpl.getMimeType(jpegBytes);

        // Assert
        assertNotNull(mimeType);
        assertTrue(mimeType.startsWith("image/jpeg"));
    }

    @Test
    void testLogin_InvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        InvalidUserCredentials exception = assertThrows(InvalidUserCredentials.class,
                () -> userService.login(loginRequest));

        assertEquals("Invalid username or password.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogoutUser() {
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setIsActive(1);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        userService.logoutUser(username);

        assertEquals(0, user.getIsActive());
        verify(userRepository, times(1)).save(user);
    }


    @Test
    void login_WithNullLoginRequest_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            userService.login(null);
        });
    }

    @Test
    void login_WithEmptyCredentials_ThrowsInvalidUserCredentials() {
        // Arrange
        LoginRequest emptyRequest = new LoginRequest("", "");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Empty credentials"));

        // Act & Assert
        InvalidUserCredentials exception = assertThrows(InvalidUserCredentials.class, () -> {
            userService.login(emptyRequest);
        });
        assertEquals("Invalid username or password.", exception.getMessage());
    }



    @Test
    void login_InvalidCredentials_ThrowsException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("username", "password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(BadCredentialsException.class);

        // Act & Assert
        assertThrows(InvalidUserCredentials.class, () -> userService.login(loginRequest));
    }

    @Test
    void login_UserDisabled_ThrowsException() throws InvalidUserCredentials {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("username", "password");
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        User user = new User();
        user.setIsEnabled(0);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> userService.login(loginRequest));
        assertEquals("User is Disabled", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }




}