package com.campusgrid.service;

import com.campusgrid.dto.AuthResponse;
import com.campusgrid.dto.LoginRequest;
import com.campusgrid.dto.UserProfileDto;
import com.campusgrid.model.Faculty;
import com.campusgrid.model.Student;
import com.campusgrid.model.User;
import com.campusgrid.model.UserSession;
import com.campusgrid.repository.FacultyRepository;
import com.campusgrid.repository.StudentRepository;
import com.campusgrid.repository.UserRepository;
import com.campusgrid.repository.UserSessionRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public AuthService(
            UserRepository userRepository,
            UserSessionRepository sessionRepository,
            FacultyRepository facultyRepository,
            StudentRepository studentRepository,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String identifier = request.getUsername().trim();
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier, identifier);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            throw new IllegalStateException("Your account has been deactivated. Please contact an administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        // Generate Access and Refresh tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken();

        // Calculate session expiry
        long refreshExpiryMs = request.isRememberMe()
                ? (jwtService.getRefreshTokenExpirationMs() * 2) // Extended for remember me
                : jwtService.getRefreshTokenExpirationMs();
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(refreshExpiryMs * 1_000_000);

        // Persist session
        UserSession session = new UserSession(user, refreshToken, ipAddress, userAgent, expiresAt);
        sessionRepository.save(session);

        // Build User Profile DTO
        UserProfileDto profile = buildUserProfile(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpirationSeconds(),
                profile
        );
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken, String ipAddress, String userAgent) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required.");
        }

        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or unrecognized refresh token."));

        if (!session.isValid()) {
            throw new IllegalStateException("Session has expired or been revoked. Please log in again.");
        }

        User user = session.getUser();
        if (!user.isActive()) {
            throw new IllegalStateException("Your account is deactivated.");
        }

        // Token Rotation: revoke old session and issue fresh pair
        session.setRevoked(true);
        sessionRepository.save(session);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(jwtService.getRefreshTokenExpirationMs() * 1_000_000);

        UserSession newSession = new UserSession(user, newRefreshToken, ipAddress, userAgent, expiresAt);
        sessionRepository.save(newSession);

        UserProfileDto profile = buildUserProfile(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                jwtService.getAccessTokenExpirationSeconds(),
                profile
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            sessionRepository.findByRefreshToken(refreshToken).ifPresent(session -> {
                session.setRevoked(true);
                sessionRepository.save(session);
            });
        }
    }

    public UserProfileDto getCurrentUser(String token) {
        if (!jwtService.validateToken(token)) {
            throw new IllegalArgumentException("Token is invalid or expired.");
        }
        Long userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return buildUserProfile(user);
    }

    public UserProfileDto buildUserProfile(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUid(user.getUid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());

        if (user.getRole() == com.campusgrid.model.Role.FACULTY) {
            facultyRepository.findByUserId(user.getId()).ifPresent(f -> {
                dto.setSpecificId(f.getEmployeeId());
                dto.setDepartment(f.getDepartment());
                dto.setDesignation(f.getDesignation());
            });
        } else if (user.getRole() == com.campusgrid.model.Role.STUDENT) {
            studentRepository.findByUserId(user.getId()).ifPresent(s -> {
                dto.setSpecificId(s.getRollNumber());
                dto.setDepartment(s.getDepartment());
                dto.setSection(s.getSectionName());
                dto.setYear(s.getYear());
                dto.setSemester(s.getCurrentSemester());
            });
        }
        return dto;
    }

    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
