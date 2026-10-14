package com.campusgrid;

import com.campusgrid.controller.AuthController;
import com.campusgrid.dto.AuthResponse;
import com.campusgrid.dto.LoginRequest;
import com.campusgrid.dto.UserProfileDto;
import com.campusgrid.model.Role;
import com.campusgrid.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.campusgrid.service.JwtService jwtService;

    @Test
    @DisplayName("Should successfully authenticate valid student credentials with 200 OK")
    void testSuccessfulStudentLogin() throws Exception {
        UserProfileDto profile = new UserProfileDto();
        profile.setId(10L);
        profile.setUsername("rahul123");
        profile.setRole(Role.STUDENT);

        AuthResponse authResponse = new AuthResponse("mock.access.token", "mock-refresh-token", 900L, profile);
        when(authService.login(any(LoginRequest.class), any(), any())).thenReturn(authResponse);

        String loginJson = "{\"username\":\"rahul123\",\"password\":\"Student@123\",\"rememberMe\":false}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("mock.access.token"))
                .andExpect(jsonPath("$.data.user.role").value("STUDENT"));
    }

    @Test
    @DisplayName("Should reject invalid password with 401 Unauthorized")
    void testInvalidPasswordLogin() throws Exception {
        when(authService.login(any(LoginRequest.class), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid username or password."));

        String loginJson = "{\"username\":\"rahul123\",\"password\":\"WrongPassword!\",\"rememberMe\":false}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    @DisplayName("Should reject deactivated account with 403 Forbidden")
    void testDeactivatedAccountLogin() throws Exception {
        when(authService.login(any(LoginRequest.class), any(), any()))
                .thenThrow(new IllegalStateException("Your account has been deactivated."));

        String loginJson = "{\"username\":\"deactivated_user\",\"password\":\"AnyPassword@123\",\"rememberMe\":false}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Your account has been deactivated."));
    }

    @Test
    @DisplayName("Should reject missing credentials with 400 Bad Request")
    void testMissingCredentialsLogin() throws Exception {
        String invalidJson = "{\"username\":\"\",\"password\":\"\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject /api/auth/me request without Authorization header with 401 Unauthorized")
    void testGetCurrentUserMissingToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Missing or malformed Authorization header."));
    }

    @Test
    @DisplayName("Should allow logout request with 200 OK")
    void testLogoutSuccess() throws Exception {
        String logoutJson = "{\"refreshToken\":\"any-token\"}";

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Logged out successfully."));
    }
}
