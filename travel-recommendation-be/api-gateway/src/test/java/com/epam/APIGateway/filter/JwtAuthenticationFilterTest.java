package com.epam.APIGateway.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.epam.APIGateway.JWT.JwtUtil;
import com.epam.APIGateway.entity.ERole;
import com.epam.APIGateway.entity.Role;
import com.epam.APIGateway.entity.User;
import com.epam.APIGateway.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenTokenIsMissing_thenShouldReturnUnauthorized() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/secure/resource"));

        // Call the filter method which returns a Mono<Void>
        Mono<Void> result = filter.filter(exchange, chain);

        // Block the Mono to execute it and check the response status
        result.block();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void whenTokenIsValid_thenShouldContinueChain() {
        String token = "valid.token";
        Claims claims = mock(Claims.class);
        User user = new User();
        user.setIsActive(1);

        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/secure/resource")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        when(jwtUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Call the filter method which returns a Mono<Void>
        Mono<Void> result = filter.filter(exchange, chain);

        // Block the Mono to execute it and check the response status
        result.block();
        verify(chain, times(1)).filter(exchange); // Verify that the chain was continued
    }
    @Test
    void whenUserIsNotAdmin_thenShouldReturnForbidden() {
        String token = "valid.token";
        Claims claims = mock(Claims.class);
        User user = new User();
        user.setIsActive(1);
        user.setRoles(Set.of(new Role(1L,ERole.ROLE_USER))); // User does not have admin role

        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/admin/resource")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        when(jwtUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        Mono<Void> result = filter.filter(exchange, chain);

        result.block();
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }
    @Test
    void whenUserIsAdmin_thenShouldContinueChain() {
        String token = "valid.token";
        Claims claims = mock(Claims.class);
        User user = new User();
        user.setIsActive(1);
        user.setRoles(Set.of(new Role(1L,ERole.ROLE_ADMIN))); // User has admin role

        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/admin/resource")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        when(jwtUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        result.block();
        verify(chain, times(1)).filter(exchange); // Verify that the chain was continued
    }
}