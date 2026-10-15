package com.campusgrid;

import com.campusgrid.controller.ExamManagementController;
import com.campusgrid.dto.ExamDetailDto;
import com.campusgrid.model.Role;
import com.campusgrid.service.ExamService;
import com.campusgrid.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExamManagementController.class)
class ExamManagementControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExamService examService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("Should block anonymous request to /api/admin/exams with 403 Forbidden")
    void testAnonymousAccessBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/exams"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should block student role from accessing /api/admin/exams with 403 Forbidden")
    void testStudentAccessBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/exams")
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should block student role from creating exams with 403 Forbidden")
    void testStudentCreateExamBlocked() throws Exception {
        mockMvc.perform(post("/api/admin/exams")
                        .contentType("application/json")
                        .content("{\"title\":\"Midterm\",\"department\":\"CS\",\"semester\":3,\"examDate\":\"2026-10-25\",\"startTime\":\"09:00\",\"endTime\":\"12:00\"}")
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should block faculty role from scheduling or deleting exams")
    void testFacultyDeleteExamBlocked() throws Exception {
        mockMvc.perform(delete("/api/admin/exams/1")
                        .requestAttr("authenticatedRole", Role.FACULTY))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin role to list exams with 200 OK")
    void testAdminGetExamsAllowed() throws Exception {
        when(examService.filterExams(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/exams")
                        .requestAttr("authenticatedRole", Role.ADMIN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow admin role to schedule exams with 201 Created")
    void testAdminCreateExamAllowed() throws Exception {
        ExamDetailDto dto = new ExamDetailDto();
        dto.setId(1L);
        dto.setTitle("Midterm");
        when(examService.createExam(any())).thenReturn(dto);

        mockMvc.perform(post("/api/admin/exams")
                        .contentType("application/json")
                        .content("{\"title\":\"Midterm\",\"department\":\"Computer Science\",\"semester\":3,\"examDate\":\"2026-10-25\",\"startTime\":\"09:00\",\"endTime\":\"12:00\",\"maxMarks\":100}")
                        .requestAttr("authenticatedRole", Role.ADMIN))
                .andExpect(status().isCreated());
    }
}
