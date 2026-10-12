package com.campusgrid.controller;

import com.campusgrid.dto.*;
import com.campusgrid.model.Role;
import com.campusgrid.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentPortalController {

    private final StudentService studentService;

    public StudentPortalController(StudentService studentService) {
        this.studentService = studentService;
    }

    private Long getAuthenticatedUserId(HttpServletRequest request) {
        Object idObj = request.getAttribute("authenticatedUserId");
        if (idObj instanceof Long) {
            return (Long) idObj;
        }
        return null;
    }

    private boolean isAuthorizedStudent(HttpServletRequest request) {
        Role role = (Role) request.getAttribute("authenticatedRole");
        return role == Role.STUDENT || role == Role.ADMIN;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(HttpServletRequest request) {
        Long userId = getAuthenticatedUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required."));
        }
        if (!isAuthorizedStudent(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Student access required."));
        }

        try {
            UserProfileDto profile = studentService.getStudentProfile(userId);
            return ResponseEntity.ok(ApiResponse.success(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<StudentDashboardDto>> getDashboard(HttpServletRequest request) {
        Long userId = getAuthenticatedUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required."));
        }
        if (!isAuthorizedStudent(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Student access required."));
        }

        try {
            StudentDashboardDto dashboard = studentService.getStudentDashboard(userId);
            return ResponseEntity.ok(ApiResponse.success(dashboard));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/timetable")
    public ResponseEntity<ApiResponse<List<StudentTimetableEntryDto>>> getTimetable(HttpServletRequest request) {
        Long userId = getAuthenticatedUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required."));
        }
        if (!isAuthorizedStudent(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Student access required."));
        }

        try {
            List<StudentTimetableEntryDto> timetable = studentService.getStudentTimetable(userId);
            return ResponseEntity.ok(ApiResponse.success(timetable));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/examinations")
    public ResponseEntity<ApiResponse<List<StudentExamAllocationDto>>> getExaminations(HttpServletRequest request) {
        Long userId = getAuthenticatedUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required."));
        }
        if (!isAuthorizedStudent(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Student access required."));
        }

        try {
            List<StudentExamAllocationDto> exams = studentService.getStudentExaminations(userId);
            return ResponseEntity.ok(ApiResponse.success(exams));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}