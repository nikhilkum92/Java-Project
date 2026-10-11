package com.campusgrid;

import com.campusgrid.model.Role;
import com.campusgrid.model.User;
import com.campusgrid.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String VALID_TEST_SECRET = "TestSecretKeyForCampusGridJwtTestsMustBeAtLeast32BytesLong2026Secure";
    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(VALID_TEST_SECRET, 900000L, 604800000L);

        testUser = new User();
        testUser.setId(10L);
        testUser.setUid("STU-1001");
        testUser.setUsername("john.doe");
        testUser.setEmail("john.doe@campusgrid.edu");
        testUser.setFullName("John Doe");
        testUser.setRole(Role.STUDENT);
        testUser.setActive(true);
    }

    @Test
    @DisplayName("Should successfully generate and validate a JWT access token")
    void testGenerateAndValidateToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("john.doe", jwtService.extractUsername(token));
        assertEquals(10L, jwtService.extractUserId(token));
        assertEquals(Role.STUDENT, jwtService.extractRole(token));
    }

    @Test
    @DisplayName("Should reject tampered or malformed JWT token")
    void testRejectTamperedToken() {
        String token = jwtService.generateAccessToken(testUser);
        String tamperedToken = token + "corrupted";
        assertFalse(jwtService.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Should reject expired JWT access token")
    void testRejectExpiredToken() {
        // Construct JwtService with negative expiration (already expired)
        JwtService expiredJwtService = new JwtService(VALID_TEST_SECRET, -1000L, 604800000L);
        String expiredToken = expiredJwtService.generateAccessToken(testUser);
        assertFalse(jwtService.validateToken(expiredToken));
    }

    @Test
    @DisplayName("Should fail startup safely if JWT secret is shorter than 32 characters")
    void testFailStartupOnShortSecret() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new JwtService("too-short-secret", 900000L, 604800000L);
        });
        assertTrue(ex.getMessage().contains("JWT signing secret is missing or shorter than 32 characters"));
    }

    @Test
    @DisplayName("Should fail startup safely if JWT secret is null or empty")
    void testFailStartupOnEmptySecret() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new JwtService("", 900000L, 604800000L);
        });
        assertTrue(ex.getMessage().contains("JWT signing secret is missing or shorter than 32 characters"));
    }

    @Test
    @DisplayName("Should generate random refresh token of appropriate length")
    void testGenerateRefreshToken() {
        String rToken1 = jwtService.generateRefreshToken();
        String rToken2 = jwtService.generateRefreshToken();
        assertNotNull(rToken1);
        assertNotNull(rToken2);
        assertNotEquals(rToken1, rToken2);
        assertEquals(64, rToken1.length());
    }
}
