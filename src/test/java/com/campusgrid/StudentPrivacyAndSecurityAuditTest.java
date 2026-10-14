package com.campusgrid;

import com.campusgrid.controller.StudentPortalController;
import com.campusgrid.dto.StudentDashboardDto;
import com.campusgrid.dto.StudentExamAllocationDto;
import com.campusgrid.dto.StudentTimetableEntryDto;
import com.campusgrid.dto.UserProfileDto;
import com.campusgrid.model.Role;
import com.campusgrid.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentPortalController.class)
class StudentPrivacyAndSecurityAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @MockBean
    private com.campusgrid.service.JwtService jwtService;

    private UserProfileDto studentAProfile;
    private UserProfileDto studentBProfile;

    @BeforeEach
    void setUp() {
        studentAProfile = new UserProfileDto();
        studentAProfile.setId(101L);
        studentAProfile.setUid("USR-STU-101");
        studentAProfile.setUsername("alice.student");
        studentAProfile.setEmail("alice@campusgrid.edu");
        studentAProfile.setFullName("Alice Student");
        studentAProfile.setRole(Role.STUDENT);
        studentAProfile.setActive(true);
        studentAProfile.setSpecificId("STU-2024-001");
        studentAProfile.setDepartment("Computer Science");
        studentAProfile.setSection("CS-4A");
        studentAProfile.setYear(4);
        studentAProfile.setSemester(7);

        studentBProfile = new UserProfileDto();
        studentBProfile.setId(102L);
        studentBProfile.setUid("USR-STU-102");
        studentBProfile.setUsername("bob.student");
        studentBProfile.setEmail("bob@campusgrid.edu");
        studentBProfile.setFullName("Bob Student");
        studentBProfile.setRole(Role.STUDENT);
        studentBProfile.setActive(true);
        studentBProfile.setSpecificId("STU-2024-002");
        studentBProfile.setDepartment("Information Technology");
        studentBProfile.setSection("IT-3B");
        studentBProfile.setYear(3);
        studentBProfile.setSemester(5);
    }

    // ==========================================
    // 1. DATA PRIVACY & MULTI-STUDENT ISOLATION
    // ==========================================

    @Test
    @DisplayName("Student A retrieves only Student A's profile based on authenticated token")
    void testStudentARetrievesOwnProfile() throws Exception {
        when(studentService.getStudentProfile(101L)).thenReturn(studentAProfile);

        mockMvc.perform(get("/api/student/profile")
                        .requestAttr("authenticatedUserId", 101L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.fullName").value("Alice Student"))
                .andExpect(jsonPath("$.data.specificId").value("STU-2024-001"))
                .andExpect(jsonPath("$.data.department").value("Computer Science"));

        verify(studentService).getStudentProfile(101L);
        verify(studentService, never()).getStudentProfile(102L);
    }

    @Test
    @DisplayName("Student B retrieves only Student B's profile based on authenticated token")
    void testStudentBRetrievesOwnProfile() throws Exception {
        when(studentService.getStudentProfile(102L)).thenReturn(studentBProfile);

        mockMvc.perform(get("/api/student/profile")
                        .requestAttr("authenticatedUserId", 102L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(102))
                .andExpect(jsonPath("$.data.fullName").value("Bob Student"))
                .andExpect(jsonPath("$.data.specificId").value("STU-2024-002"))
                .andExpect(jsonPath("$.data.department").value("Information Technology"));

        verify(studentService).getStudentProfile(102L);
        verify(studentService, never()).getStudentProfile(101L);
    }

    @Test
    @DisplayName("IDOR Immunity: Student A cannot retrieve Student B's profile by supplying studentId in URL query")
    void testIdorImmunityInProfileQueryParam() throws Exception {
        when(studentService.getStudentProfile(101L)).thenReturn(studentAProfile);

        // Even if Student A passes ?studentId=102 or ?userId=102, the controller strictly uses authenticatedUserId (101L)
        mockMvc.perform(get("/api/student/profile?studentId=102&userId=102")
                        .requestAttr("authenticatedUserId", 101L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.fullName").value("Alice Student"))
                .andExpect(jsonPath("$.data.specificId").value("STU-2024-001"));

        verify(studentService).getStudentProfile(101L);
        verify(studentService, never()).getStudentProfile(102L);
    }

    @Test
    @DisplayName("Privacy: Passwords, password hashes, and JWT tokens are not exposed in response DTO")
    void testNoSensitiveDataExposedInProfile() throws Exception {
        when(studentService.getStudentProfile(101L)).thenReturn(studentAProfile);

        mockMvc.perform(get("/api/student/profile")
                        .requestAttr("authenticatedUserId", 101L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andExpect(jsonPath("$.data.accessToken").doesNotExist())
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    // ==========================================
    // 2. TIMETABLE & EXAMINATION ISOLATION
    // ==========================================

    @Test
    @DisplayName("IDOR Immunity: Student A cannot retrieve Student B's timetable by tampering URL params")
    void testIdorImmunityInTimetable() throws Exception {
        when(studentService.getStudentTimetable(101L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/student/timetable?studentId=102")
                        .requestAttr("authenticatedUserId", 101L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());

        verify(studentService).getStudentTimetable(101L);
        verify(studentService, never()).getStudentTimetable(102L);
    }

    @Test
    @DisplayName("IDOR Immunity: Student A cannot retrieve Student B's examination seat allocation by tampering URL params")
    void testIdorImmunityInExaminations() throws Exception {
        when(studentService.getStudentExaminations(101L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/student/examinations?studentId=102")
                        .requestAttr("authenticatedUserId", 101L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());

        verify(studentService).getStudentExaminations(101L);
        verify(studentService, never()).getStudentExaminations(102L);
    }

    // ==========================================
    // 3. ROLE-BASED ACCESS CONTROL (RBAC) BOUNDARIES
    // ==========================================

    @Test
    @DisplayName("Faculty role is rejected with 403 Forbidden on /api/student/timetable")
    void testFacultyAccessToTimetableForbidden() throws Exception {
        mockMvc.perform(get("/api/student/timetable")
                        .requestAttr("authenticatedUserId", 201L)
                        .requestAttr("authenticatedRole", Role.FACULTY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Access denied: Student access required."));

        verify(studentService, never()).getStudentTimetable(anyLong());
    }

    @Test
    @DisplayName("Faculty role is rejected with 403 Forbidden on /api/student/examinations")
    void testFacultyAccessToExaminationsForbidden() throws Exception {
        mockMvc.perform(get("/api/student/examinations")
                        .requestAttr("authenticatedUserId", 201L)
                        .requestAttr("authenticatedRole", Role.FACULTY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Access denied: Student access required."));

        verify(studentService, never()).getStudentExaminations(anyLong());
    }

    @Test
    @DisplayName("Unauthenticated request is rejected with 401 Unauthorized across all student APIs")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/student/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Authentication required."));

        mockMvc.perform(get("/api/student/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"));

        mockMvc.perform(get("/api/student/timetable"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"));

        mockMvc.perform(get("/api/student/examinations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"));

        verifyNoInteractions(studentService);
    }
}
