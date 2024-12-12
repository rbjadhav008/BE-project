package com.epam.APIGateway.filter;

import com.epam.APIGateway.JWT.JwtUtil;
import com.epam.APIGateway.entity.ERole;
import com.epam.APIGateway.entity.User;
import com.epam.APIGateway.exceptions.InvalidTokenException;
import com.epam.APIGateway.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private static final Logger logger = Logger.getLogger(JwtUtil.class.getName());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.equals("/api/blogs/filter")||path.equals("/api/users/register") ||
                path.equals("/api/users/login") || path.equals("/api/users/updatePassword")||
                path.equals("/api/users/getotp")  || path.startsWith("/api/blogs/get/")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        logger.info("AUTH HEADER RECEIVED BY THE SERVER");

        if(path.equals("/api/blogs/allBlogs") && authHeader == null){
            exchange.getRequest().mutate()
                    .header("X-User-Id", "GUEST")
                    .build();
            return chain.filter(exchange);
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return createUnauthorizedResponse(exchange, "You cannot access this resource. You are not logged in. Please log in and try again.");
        }

        String token = authHeader.substring(7);
        logger.info("Processing token: " + token);

        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
            logger.info("CLAIMS extracted BY THE SERVER as follows: ");
            System.out.println(claims);
        } catch (Exception e) {
            return createUnauthorizedResponse(exchange, "Invalid or expired token");
        }

        String username = claims.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getIsActive() == 0) {
            return createUnauthorizedResponse(exchange, "We detected a suspicious activity. Please log in and try again.");
        }

        // Check for admin-only endpoints
        if (path.startsWith("/api/admin/")) {
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);

            if (!isAdmin) {
                return createForbiddenResponse(exchange, "Access denied. Admin role required.");
            }
        }

        // Add user information to headers
        exchange.getRequest().mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-Role", claims.get("roles", String.class))
                .build();

        return chain.filter(exchange);
    }

    private Mono<Void> createUnauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return createJsonResponse(exchange, message);
    }

    private Mono<Void> createForbiddenResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return createJsonResponse(exchange, message);
    }

    private Mono<Void> createJsonResponse(ServerWebExchange exchange, String message) {
        String responseMessage = String.format("{\"message\":\"%s\"}", message);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(responseMessage.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
