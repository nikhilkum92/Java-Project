package com.campusgrid.controller;

import com.campusgrid.dto.*;
import com.campusgrid.model.Role;
import com.campusgrid.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<List<StudentTimetableEntryDto>>> getTimetable(
            @RequestParam(name = "section", required = false) String section,
            HttpServletRequest request
    ) {
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
            List<StudentTimetableEntryDto> timetable;
            if (section != null && !section.trim().isEmpty()) {
                timetable = studentService.getSectionTimetable(section.trim());
            } else {
                timetable = studentService.getStudentTimetable(userId);
            }
            return ResponseEntity.ok(ApiResponse.success(timetable));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/timetable/sections")
    public ResponseEntity<ApiResponse<List<String>>> getTimetableSections(HttpServletRequest request) {
        Long userId = getAuthenticatedUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required."));
        }
        if (!isAuthorizedStudent(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Student access required."));
        }

        List<String> sections = studentService.getAvailableTimetableSections();
        return ResponseEntity.ok(ApiResponse.success(sections));
    }

    @GetMapping("/examinations")
    public ResponseEntity<ApiResponse<List<StudentExamAllocationDto>>> getExaminations(
            @RequestParam(name = "section", required = false) String section,
            HttpServletRequest request
    ) {
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
            List<StudentExamAllocationDto> exams;
            if (section != null && !section.trim().isEmpty()) {
                exams = studentService.getSectionExaminations(section.trim());
            } else {
                exams = studentService.getStudentExaminations(userId);
            }
            return ResponseEntity.ok(ApiResponse.success(exams));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/examinations/sections")
    public ResponseEntity<ApiResponse<List<String>>> getExamSections(HttpServletRequest request) {
        Long userId = getAuthenticatedUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required."));
        }
        if (!isAuthorizedStudent(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Student access required."));
        }

        List<String> sections = studentService.getAvailableExamSections();
        return ResponseEntity.ok(ApiResponse.success(sections));
    }

    @GetMapping("/examinations/{id}/allocation")
    public ResponseEntity<ApiResponse<StudentExamAllocationDto>> getExamAllocation(
            @PathVariable("id") Long examId,
            HttpServletRequest request
    ) {
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
            StudentExamAllocationDto allocation = studentService.getStudentExamAllocation(userId, examId);
            return ResponseEntity.ok(ApiResponse.success(allocation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
