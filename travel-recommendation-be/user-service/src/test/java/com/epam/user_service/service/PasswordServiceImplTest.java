package com.epam.user_service.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.epam.user_service.exception.UserNotFound;
import com.epam.user_service.repository.UserRepository;
import com.epam.user_service.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

class PasswordServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private PasswordServiceImpl passwordServiceImpl;
    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailMessageCaptor;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    void testCreatingOtp_UserNotFound() {
        when(userRepository.findByUsername("user@example.com")).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () ->
                passwordServiceImpl.creatingOtp("user@example.com")
        );

        assertTrue(true);
    }


    @Test
    void updatePassword_UserNotFound() {
        String email = "test@example.com";
        when(userRepository.findByUsername(email)).thenReturn(Optional.empty());
        assertThrows(UserNotFound.class, () -> passwordServiceImpl.updatePassword(email, "password123", "123456"));
    }
    @Test
    void testUpdatePasswordWithCorrectOtp() {
        User user = new User();
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(user));
        when(encoder.encode("newPassword")).thenReturn("encodedPassword");
        passwordServiceImpl.creatingOtp("user@example.com");
        String storedOtp = PasswordServiceImpl.otpStore.get("user@example.com");
        String result = passwordServiceImpl.updatePassword("user@example.com", "newPassword", storedOtp);
        assertEquals("Password updated successfully", result);
    }

    @Test
    void testUpdatePasswordWithIncorrectOtp() {
        User user = new User();
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(user));
        passwordServiceImpl.creatingOtp("user@example.com"); // Generate and store OTP
        String invalidOtp = "123456"; // Assume this is not the stored OTP
       // String result = otpAndPasswordServiceImpl.updatePassword("user@example.com", "newPassword", invalidOtp);
        assertThrows(Exception.class, ()-> passwordServiceImpl.updatePassword("user@example.com", "newPassword", invalidOtp));
    }
}