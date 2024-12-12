package com.epam.user_service.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

class UserDetailsTest {

    @Test
    void testUserDetailsInterface() {
        User user = new User("password123", "USA", "New York", 1, "john@example.com", "Doe", "John");

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertTrue(authorities.isEmpty());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }
}