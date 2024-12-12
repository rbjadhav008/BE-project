package com.epam.APIGateway.JWT;
import com.epam.APIGateway.exceptions.InvalidTokenException;
import com.epam.APIGateway.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secretKey;
    private static final Logger logger = Logger.getLogger(JwtUtil.class.getName());

    public Claims parseToken(String token) throws SignatureException, ExpiredJwtException {
        try {
            logger.info("The parse token util method has been invoked");
            byte[] secretKeyBytes = Base64.getDecoder().decode(secretKey);

            logger.info("Using secret key for verification.");

            return Jwts.parser()
                    .setSigningKey(secretKeyBytes)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (SignatureException e) {
            logger.warning("Suspicious activity detected: The token's signature is invalid. This may indicate that the token has been tampered with or is not issued by a trusted source.");
            throw new InvalidTokenException("We have detected a suspicious activity on your account. Try changing your password and login again.");
        } catch (ExpiredJwtException e) {
            logger.warning("Expired token received. Access denied!");
            throw new InvalidTokenException("Your session has been expired. Please log in again.");
        } catch (Exception e) {
            logger.severe("Error parsing token: " + e.getMessage());
            throw new InvalidTokenException("Access denied! Please login and try again.");
        }
    }

    public boolean hasRole(String token, String requiredRole) {
        try {
            Claims claims = parseToken(token);
            String roles = claims.get("roles", String.class);

            if (roles == null) {
                logger.warning("No roles found in token");
                return false;
            }

            // Split the roles string and check if it contains the required role
            return Arrays.asList(roles.split(","))
                    .contains(requiredRole);
        } catch (Exception e) {
            logger.warning("Error checking roles: " + e.getMessage());
            return false;
        }
    }

    public boolean isAdmin(String token) {
        return hasRole(token, "ROLE_ADMIN");
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getTokenExpirationTime(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime();
    }
}

