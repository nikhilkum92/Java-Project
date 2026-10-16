package com.campusgrid.controller;

import com.campusgrid.dto.ApiResponse;
import com.campusgrid.dto.TimetableEntryDto;
import com.campusgrid.service.TimetableService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetables")
public class TimetableDirectoryController {

    private final TimetableService timetableService;

    public TimetableDirectoryController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        return request.getAttribute("authenticatedUserId") != null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimetableEntryDto>> getEntryById(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to access timetable information."));
        }

        try {
            TimetableEntryDto entry = timetableService.getTimetableEntryById(id);
            return ResponseEntity.ok(ApiResponse.success(entry));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<List<TimetableEntryDto>>> getTimetableByRoom(
            @PathVariable("roomId") Long roomId,
            HttpServletRequest request
    ) {
        if (!isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to access room schedule."));
        }

        try {
            List<TimetableEntryDto> schedule = timetableService.getTimetableByRoom(roomId);
            return ResponseEntity.ok(ApiResponse.success(schedule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<ApiResponse<List<TimetableEntryDto>>> getTimetableByFaculty(
            @PathVariable("facultyId") Long facultyId,
            HttpServletRequest request
    ) {
        if (!isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to access faculty schedule."));
        }

        try {
            List<TimetableEntryDto> schedule = timetableService.getTimetableByFaculty(facultyId);
            return ResponseEntity.ok(ApiResponse.success(schedule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<TimetableEntryDto>>> getTimetableByGroup(
            @RequestParam("department") String department,
            @RequestParam("sectionName") String sectionName,
            HttpServletRequest request
    ) {
        if (!isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to access group schedule."));
        }

        try {
            List<TimetableEntryDto> schedule = timetableService.getTimetableByGroup(department, sectionName);
            return ResponseEntity.ok(ApiResponse.success(schedule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my-schedule")
    public ResponseEntity<ApiResponse<List<TimetableEntryDto>>> getMySchedule(HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to access schedule."));
        }
        Long userId = (Long) request.getAttribute("authenticatedUserId");
        List<TimetableEntryDto> schedule = timetableService.getTimetableByFacultyUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<com.campusgrid.dto.SubjectDto>>> getAllSubjects(HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to access subjects directory."));
        }
        List<com.campusgrid.dto.SubjectDto> subjects = timetableService.getAllSubjects();
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }
}
