package com.epam.recommendation_service.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ERoleTest {

    @Test
    public void testEnumValues() {
        ERole[] roles = ERole.values();

        // Check that all expected roles are present
        assertTrue(contains(roles, ERole.ROLE_USER), "ERole should contain ROLE_USER");
        assertTrue(contains(roles, ERole.ROLE_ADMIN), "ERole should contain ROLE_ADMIN");

        // Check that there are no extra roles defined
        assertEquals(2, roles.length, "ERole should only contain exactly two roles");
    }

    private boolean contains(ERole[] roles, ERole role) {
        for (ERole r : roles) {
            if (r == role) {
                return true;
            }
        }
        return false;
    }
}