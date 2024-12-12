package com.epam.APIGateway.JWT;

import com.epam.APIGateway.JWT.JwtUtil;
import com.epam.APIGateway.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private Key secretKey;
    private String validToken;
    private String expiredToken;
    private String invalidSignatureToken;
    private String tokenWithoutRoles;


    @BeforeEach
    void setUp() {
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        ReflectionTestUtils.setField(jwtUtil, "secretKey", encodedKey);

        // Generate a valid token
        validToken = Jwts.builder()
                .setSubject("testuser")
                .claim("roles", "ROLE_USER,ROLE_ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(secretKey)
                .compact();

        // Generate an expired token
        expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 120000))
                .setExpiration(new Date(System.currentTimeMillis() - 60000))
                .signWith(secretKey)
                .compact();

        // Generate a token with an invalid signature
        Key invalidKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        invalidSignatureToken = Jwts.builder()
                .setSubject("testuser")
                .signWith(invalidKey)
                .compact();

        // Generate a token without roles
        tokenWithoutRoles = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(secretKey)
                .compact();
    }

    @Test
    void parseToken_ValidToken_ReturnsClaims() {
        Claims claims = jwtUtil.parseToken(validToken);
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
    }

    @Test
    void parseToken_ExpiredToken_ThrowsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () -> jwtUtil.parseToken(expiredToken));
    }

    @Test
    void parseToken_InvalidSignatureToken_ThrowsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () -> jwtUtil.parseToken(invalidSignatureToken));
    }

    @Test
    void hasRole_UserWithRequiredRole_ReturnsTrue() {
        assertTrue(jwtUtil.hasRole(validToken, "ROLE_ADMIN"));
    }

    @Test
    void hasRole_UserWithoutRequiredRole_ReturnsFalse() {
        assertFalse(jwtUtil.hasRole(validToken, "ROLE_MANAGER"));
    }

    @Test
    void isAdmin_AdminUser_ReturnsTrue() {
        assertTrue(jwtUtil.isAdmin(validToken));
    }

    @Test
    void isAdmin_NonAdminUser_ReturnsFalse() {
        String nonAdminToken = Jwts.builder()
                .setSubject("testuser")
                .claim("roles", "ROLE_USER")
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        assertFalse(jwtUtil.isAdmin(nonAdminToken));
    }

    @Test
    void getUsername_ValidToken_ReturnsUsername() {
        String username = jwtUtil.getUsername(validToken);
        assertEquals("testuser", username);
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        assertTrue(jwtUtil.isTokenValid(validToken));
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        assertFalse(jwtUtil.isTokenValid(expiredToken));
    }

    @Test
    void isTokenValid_InvalidSignatureToken_ReturnsFalse() {
        assertFalse(jwtUtil.isTokenValid(invalidSignatureToken));
    }

    @Test
    void getTokenExpirationTime_ValidToken_ReturnsExpirationTime() {
        long expirationTime = jwtUtil.getTokenExpirationTime(validToken);
        assertTrue(expirationTime > System.currentTimeMillis());
    }



    @Test
    void hasRole_TokenWithoutRoles_ReturnsFalse() {
        assertFalse(jwtUtil.hasRole(tokenWithoutRoles, "ROLE_USER"));
    }

    @Test
    void hasRole_InvalidToken_ReturnsFalse() {
        assertFalse(jwtUtil.hasRole("invalid_token", "ROLE_USER"));
    }





}