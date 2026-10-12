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
}
