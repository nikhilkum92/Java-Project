package com.campusgrid;

import com.campusgrid.controller.UserManagementController;
import com.campusgrid.model.Role;
import com.campusgrid.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserManagementController.class)
class UserManagementControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private com.campusgrid.service.JwtService jwtService;

    @Test
    @DisplayName("Should block anonymous requests to /api/admin/users/students with 403 Forbidden")
    void testAnonymousAccessBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/users/students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should block student role from accessing /api/admin/users/students with 403 Forbidden")
    void testStudentRoleBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/users/students")
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should block faculty role from accessing /api/admin/users/faculty with 403 Forbidden")
    void testFacultyRoleBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/users/faculty")
                        .requestAttr("authenticatedRole", Role.FACULTY))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin role to access /api/admin/users/students with 200 OK")
    void testAdminAccessAllowed() throws Exception {
        when(userService.getAllStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/users/students")
                        .requestAttr("authenticatedRole", Role.ADMIN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should block non-admin from exporting CSV")
    void testNonAdminExportCsvBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/users/export?type=students")
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isForbidden());
    }
}
