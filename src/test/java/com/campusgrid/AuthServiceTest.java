package com.campusgrid;

import com.campusgrid.dto.AuthResponse;
import com.campusgrid.dto.LoginRequest;
import com.campusgrid.model.Role;
import com.campusgrid.model.User;
import com.campusgrid.model.UserSession;
import com.campusgrid.repository.FacultyRepository;
import com.campusgrid.repository.StudentRepository;
import com.campusgrid.repository.UserRepository;
import com.campusgrid.repository.UserSessionRepository;
import com.campusgrid.service.AuthService;
import com.campusgrid.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository sessionRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private User deactivatedUser;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(2L);
        activeUser.setUsername("active_student");
        activeUser.setEmail("active@campusgrid.edu");
        activeUser.setPasswordHash(encoder.encode("Password@123"));
        activeUser.setFullName("Active Student");
        activeUser.setRole(Role.STUDENT);
        activeUser.setActive(true);

        deactivatedUser = new User();
        deactivatedUser.setId(3L);
        deactivatedUser.setUsername("inactive_user");
        deactivatedUser.setEmail("inactive@campusgrid.edu");
        deactivatedUser.setPasswordHash(encoder.encode("Password@123"));
        deactivatedUser.setFullName("Inactive User");
        deactivatedUser.setRole(Role.STUDENT);
        deactivatedUser.setActive(false);
    }

    @Test
    @DisplayName("Should successfully authenticate valid user and issue token pair")
    void testSuccessfulLogin() {
        LoginRequest req = new LoginRequest("active_student", "Password@123", false);

        when(userRepository.findByUsernameOrEmail("active_student", "active_student"))
                .thenReturn(Optional.of(activeUser));
        when(jwtService.generateAccessToken(activeUser)).thenReturn("mocked.access.jwt");
        when(jwtService.generateRefreshToken()).thenReturn("mocked-refresh-token");
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse resp = authService.login(req, "127.0.0.1", "Mozilla/5.0");

        assertNotNull(resp);
        assertEquals("mocked.access.jwt", resp.getAccessToken());
        assertEquals("mocked-refresh-token", resp.getRefreshToken());
        verify(sessionRepository, times(1)).save(any(UserSession.class));
    }

    @Test
    @DisplayName("Should reject authentication for deactivated account")
    void testRejectDeactivatedAccount() {
        LoginRequest req = new LoginRequest("inactive_user", "Password@123", false);

        when(userRepository.findByUsernameOrEmail("inactive_user", "inactive_user"))
                .thenReturn(Optional.of(deactivatedUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            authService.login(req, "127.0.0.1", "Mozilla/5.0");
        });
        assertTrue(ex.getMessage().contains("deactivated"));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject authentication with invalid password")
    void testRejectInvalidPassword() {
        LoginRequest req = new LoginRequest("active_student", "WrongPassword!", false);

        when(userRepository.findByUsernameOrEmail("active_student", "active_student"))
                .thenReturn(Optional.of(activeUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(req, "127.0.0.1", "Mozilla/5.0");
        });
        assertTrue(ex.getMessage().contains("Invalid username or password"));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should rotate refresh token and revoke previous session on refresh")
    void testRefreshTokenRotation() {
        UserSession activeSession = new UserSession(
                activeUser,
                "old-refresh-token",
                "127.0.0.1",
                "Mozilla/5.0",
                LocalDateTime.now().plusDays(5)
        );

        when(sessionRepository.findByRefreshToken("old-refresh-token")).thenReturn(Optional.of(activeSession));
        when(jwtService.generateAccessToken(activeUser)).thenReturn("new.access.jwt");
        when(jwtService.generateRefreshToken()).thenReturn("new-refresh-token");
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse resp = authService.refreshAccessToken("old-refresh-token", "127.0.0.1", "Mozilla/5.0");

        assertNotNull(resp);
        assertEquals("new.access.jwt", resp.getAccessToken());
        assertEquals("new-refresh-token", resp.getRefreshToken());
        assertTrue(activeSession.isRevoked()); // Prior session revoked
        verify(sessionRepository, times(2)).save(any(UserSession.class)); // 1 revoke + 1 new session
    }

    @Test
    @DisplayName("Should revoke session on logout")
    void testLogoutRevocation() {
        UserSession session = new UserSession(
                activeUser,
                "token-to-logout",
                "127.0.0.1",
                "Mozilla/5.0",
                LocalDateTime.now().plusDays(5)
        );

        when(sessionRepository.findByRefreshToken("token-to-logout")).thenReturn(Optional.of(session));

        authService.logout("token-to-logout");

        assertTrue(session.isRevoked());
        verify(sessionRepository, times(1)).save(session);
    }
}
