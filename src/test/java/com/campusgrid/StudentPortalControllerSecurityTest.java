package com.campusgrid;

import com.campusgrid.controller.StudentPortalController;
import com.campusgrid.dto.StudentDashboardDto;
import com.campusgrid.dto.UserProfileDto;
import com.campusgrid.model.Role;
import com.campusgrid.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentPortalController.class)
class StudentPortalControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @MockBean
    private com.campusgrid.service.JwtService jwtService;

    // --- Anonymous access control ---

    @Test
    @DisplayName("Should block anonymous requests to /api/student/profile with 401 Unauthorized")
    void testAnonymousProfileBlocked() throws Exception {
        mockMvc.perform(get("/api/student/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Authentication required."));
    }

    @Test
    @DisplayName("Should block anonymous requests to /api/student/dashboard with 401 Unauthorized")
    void testAnonymousDashboardBlocked() throws Exception {
        mockMvc.perform(get("/api/student/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Should block anonymous requests to /api/student/timetable with 401 Unauthorized")
    void testAnonymousTimetableBlocked() throws Exception {
        mockMvc.perform(get("/api/student/timetable"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Should block anonymous requests to /api/student/examinations with 401 Unauthorized")
    void testAnonymousExaminationsBlocked() throws Exception {
        mockMvc.perform(get("/api/student/examinations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    // --- Role-based authorization: Faculty blocked from Student endpoints ---

    @Test
    @DisplayName("Should block faculty role from accessing /api/student/profile with 403 Forbidden")
    void testFacultyProfileBlocked() throws Exception {
        mockMvc.perform(get("/api/student/profile")
                        .requestAttr("authenticatedUserId", 20L)
                        .requestAttr("authenticatedRole", Role.FACULTY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Access denied: Student access required."));
    }

    @Test
    @DisplayName("Should block faculty role from accessing /api/student/dashboard with 403 Forbidden")
    void testFacultyDashboardBlocked() throws Exception {
        mockMvc.perform(get("/api/student/dashboard")
                        .requestAttr("authenticatedUserId", 20L)
                        .requestAttr("authenticatedRole", Role.FACULTY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    // --- Legitimate Student Role access ---

    @Test
    @DisplayName("Should allow authenticated student to access /api/student/profile with 200 OK")
    void testStudentProfileAllowed() throws Exception {
        UserProfileDto mockProfile = new UserProfileDto();
        mockProfile.setId(10L);
        mockProfile.setFullName("John Doe");
        mockProfile.setRole(Role.STUDENT);
        mockProfile.setSpecificId("STU-2024-001");

        when(studentService.getStudentProfile(10L)).thenReturn(mockProfile);

        mockMvc.perform(get("/api/student/profile")
                        .requestAttr("authenticatedUserId", 10L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.specificId").value("STU-2024-001"));
    }

    @Test
    @DisplayName("Should allow authenticated student to access /api/student/dashboard with 200 OK")
    void testStudentDashboardAllowed() throws Exception {
        StudentDashboardDto mockDashboard = new StudentDashboardDto();
        mockDashboard.setRollNumber("STU-2024-001");
        mockDashboard.setDepartment("Computer Science");
        mockDashboard.setTodaySchedule(Collections.emptyList());

        when(studentService.getStudentDashboard(10L)).thenReturn(mockDashboard);

        mockMvc.perform(get("/api/student/dashboard")
                        .requestAttr("authenticatedUserId", 10L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.rollNumber").value("STU-2024-001"))
                .andExpect(jsonPath("$.data.department").value("Computer Science"));
    }

    @Test
    @DisplayName("Should allow authenticated student to access /api/student/timetable with 200 OK")
    void testStudentTimetableAllowed() throws Exception {
        when(studentService.getStudentTimetable(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/student/timetable")
                        .requestAttr("authenticatedUserId", 10L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Should allow authenticated student to access /api/student/examinations with 200 OK")
    void testStudentExaminationsAllowed() throws Exception {
        when(studentService.getStudentExaminations(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/student/examinations")
                        .requestAttr("authenticatedUserId", 10L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // --- Admin access to student portal ---

    @Test
    @DisplayName("Should allow admin role to access /api/student/dashboard with 200 OK")
    void testAdminAccessAllowed() throws Exception {
        StudentDashboardDto mockDashboard = new StudentDashboardDto();
        mockDashboard.setRollNumber("ADMIN-VIEW");
        when(studentService.getStudentDashboard(1L)).thenReturn(mockDashboard);

        mockMvc.perform(get("/api/student/dashboard")
                        .requestAttr("authenticatedUserId", 1L)
                        .requestAttr("authenticatedRole", Role.ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    // --- Error handling ---

    @Test
    @DisplayName("Should return 404 Not Found when student profile does not exist")
    void testStudentProfileNotFound() throws Exception {
        when(studentService.getStudentProfile(anyLong()))
                .thenThrow(new IllegalArgumentException("Student academic profile not found"));

        mockMvc.perform(get("/api/student/profile")
                        .requestAttr("authenticatedUserId", 999L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Student academic profile not found"));
    }
}