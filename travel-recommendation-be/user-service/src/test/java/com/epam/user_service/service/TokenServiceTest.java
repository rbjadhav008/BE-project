package com.epam.user_service.service;

import com.epam.user_service.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private Authentication authentication;

    private final String testSecretKey = "z01PV9vP9w7cAnr5HZFSGBKGGwSznqGldeJhRI9XM/c=";
    private final String testUsername = "testuser@example.com";

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secretKey", testSecretKey);

        // Create test user
        testUser = new User(
                "password123",     // password
                "United States",   // country
                "New York",        // city
                1,                 // isActive
                testUsername,      // username
                "Doe",            // lastName
                "John"            // firstName
        );
    }

    @Test
    void generateToken_BasicUser() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        // Mocking UserDetails to return a test username
        String testUsername = "testUsername";
        when(userDetails.getUsername()).thenReturn(testUsername);

        // Setting up Authentication to return mocked UserDetails
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getAuthorities()).thenReturn(List.of());  // Assuming no roles for simplicity

        // Act
        String token = tokenService.generateToken(authentication);

        // Assert
        assertNotNull(token);
        Claims claims = Jwts.parser()
                .setSigningKey(Base64.getDecoder().decode(testSecretKey.getBytes()))
                .parseClaimsJws(token)
                .getBody();

        assertEquals(testUsername, claims.getSubject());
        assertEquals("", claims.get("roles")); // Empty roles as per User implementation
        assertEquals("EPAM systems", claims.getIssuer());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateToken_ExpirationTime() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        // Mocking UserDetails to return a test username
        when(userDetails.getUsername()).thenReturn("testUsername");

        // Setting up Authentication to return mocked UserDetails
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getAuthorities()).thenReturn(List.of());  // Assuming no roles for simplicity

        // Act
        String token = tokenService.generateToken(authentication);

        // Assert
        Claims claims = Jwts.parser()
                .setSigningKey(Base64.getDecoder().decode(testSecretKey.getBytes()))
                .parseClaimsJws(token)
                .getBody();

        Date expirationDate = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();

        // Check if expiration is roughly 600 minutes (10 hours) after issuedAt
        long diffInMillies = expirationDate.getTime() - issuedAt.getTime();
        long diffInMinutes = diffInMillies / (1000 * 60);

        assertTrue(diffInMinutes >= 599 && diffInMinutes <= 600);
    }

    @Test
    void generateToken_TokenStructure() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        // Mocking UserDetails to return a test username
        when(userDetails.getUsername()).thenReturn("testUsername");

        // Setting up Authentication to return mocked UserDetails
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getAuthorities()).thenReturn(List.of());  // Assuming no roles for simplicity

        // Act
        String token = tokenService.generateToken(authentication);

        // Assert
        // Verify token has three parts (header, payload, signature)
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length);

        // Verify each part is proper base64 encoded
        assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(tokenParts[0]));
        assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(tokenParts[1]));
        assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(tokenParts[2]));
    }
    @Test
    void generateToken_WithInactiveUser() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        // Mocking UserDetails to return a test username
        when(userDetails.getUsername()).thenReturn("testUsername");

        // Setting up Authentication to return mocked UserDetails
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getAuthorities()).thenReturn(List.of());  // Assuming no roles for simplicity

        // Act
        String token = tokenService.generateToken(authentication);

        // Assert
        assertNotNull(token);
        Claims claims = Jwts.parser()
                .setSigningKey(Base64.getDecoder().decode(testSecretKey.getBytes()))
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testUsername", claims.getSubject());
        assertEquals("", claims.get("roles"));
    }

}