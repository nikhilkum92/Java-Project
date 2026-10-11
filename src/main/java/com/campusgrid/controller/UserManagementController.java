package com.campusgrid.controller;

import com.campusgrid.dto.ApiResponse;
import com.campusgrid.dto.CreateUserRequest;
import com.campusgrid.dto.UserTableDto;
import com.campusgrid.model.Role;
import com.campusgrid.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController {

    private final UserService userService;

    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    private boolean checkAdminAuthorization(HttpServletRequest request) {
        Role role = (Role) request.getAttribute("authenticatedRole");
        return role == Role.ADMIN;
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<UserTableDto>>> getStudents(HttpServletRequest request) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        List<UserTableDto> students = userService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/faculty")
    public ResponseEntity<ApiResponse<List<UserTableDto>>> getFaculty(HttpServletRequest request) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        List<UserTableDto> faculty = userService.getAllFaculty();
        return ResponseEntity.ok(ApiResponse.success(faculty));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserTableDto>> createUser(
            @Valid @RequestBody CreateUserRequest requestBody,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            UserTableDto createdUser = userService.createUser(requestBody);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User created successfully.", createdUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Boolean>> toggleStatus(
            @PathVariable("id") Long userId,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            boolean newStatus = userService.toggleUserStatus(userId);
            String message = newStatus ? "User activated." : "User deactivated.";
            return ResponseEntity.ok(ApiResponse.success(message, newStatus));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable("id") Long userId,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User removed successfully.", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable("id") Long userId,
            @RequestBody java.util.Map<String, String> body,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            String newPassword = body.get("newPassword");
            userService.resetPassword(userId, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully.", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse<UserTableDto>> updateUser(
            @PathVariable("id") Long userId,
            @Valid @RequestBody com.campusgrid.dto.UpdateUserRequest requestBody,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            UserTableDto updatedUser = userService.updateUser(userId, requestBody);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully.", updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<String>>> getDepartments(HttpServletRequest request) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        return ResponseEntity.ok(ApiResponse.success(userService.getDistinctDepartments()));
    }

    @GetMapping("/sections")
    public ResponseEntity<ApiResponse<List<String>>> getSections(HttpServletRequest request) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        return ResponseEntity.ok(ApiResponse.success(userService.getDistinctSections()));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<com.campusgrid.dto.UserStatsDto>> getUserStats(HttpServletRequest request) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        return ResponseEntity.ok(ApiResponse.success(userService.getUserStats()));
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportCsv(
            @RequestParam(name = "type", defaultValue = "students") String type,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
        String csv = userService.exportUsersCsv(type);
        String dateStr = java.time.LocalDate.now().toString();
        String filename = type.equalsIgnoreCase("faculty") ? "campusgrid-faculty-" + dateStr + ".csv" : "campusgrid-students-" + dateStr + ".csv";

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @PostMapping(value = "/import", consumes = {org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<com.campusgrid.dto.CsvImportResultDto>> importCsvMultipart(
            @RequestParam(name = "role", defaultValue = "STUDENT") Role role,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Please upload a valid, non-empty CSV file."));
        }
        try {
            String csvContent = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            com.campusgrid.dto.CsvImportResultDto report = userService.importUsersCsv(csvContent, role);
            String summaryMsg = String.format("File '%s' processed: %d added, %d duplicate(s), %d error(s).",
                    file.getOriginalFilename(), report.getSuccessfullyImported(), report.getDuplicatesCount(), report.getErrorsCount());
            return ResponseEntity.ok(ApiResponse.success(summaryMsg, report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("File import failed: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/import", consumes = {org.springframework.http.MediaType.TEXT_PLAIN_VALUE, "text/csv", org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE, "*/*"})
    public ResponseEntity<ApiResponse<com.campusgrid.dto.CsvImportResultDto>> importCsvText(
            @RequestParam(name = "role", defaultValue = "STUDENT") Role role,
            @RequestBody String csvContent,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            com.campusgrid.dto.CsvImportResultDto report = userService.importUsersCsv(csvContent, role);
            String summaryMsg = String.format("Import processed: %d added, %d duplicate(s), %d error(s).",
                    report.getSuccessfullyImported(), report.getDuplicatesCount(), report.getErrorsCount());
            return ResponseEntity.ok(ApiResponse.success(summaryMsg, report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Import failed: " + e.getMessage()));
        }
    }
}


