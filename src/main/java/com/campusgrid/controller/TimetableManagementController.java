package com.campusgrid.controller;

import com.campusgrid.dto.*;
import com.campusgrid.model.Role;
import com.campusgrid.model.RoomType;
import com.campusgrid.model.TimetableStatus;
import com.campusgrid.service.TimetableService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class TimetableManagementController {

    private final TimetableService timetableService;

    public TimetableManagementController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    private boolean checkAdminAuthorization(HttpServletRequest request) {
        Role role = (Role) request.getAttribute("authenticatedRole");
        return role == Role.ADMIN;
    }

    // =========================================================================
    // TIMETABLE CRUD & ALLOCATION ENDPOINTS
    // =========================================================================

    @GetMapping("/timetables")
    public ResponseEntity<ApiResponse<List<TimetableEntryDto>>> getTimetables(
            @RequestParam(name = "dayOfWeek", required = false) DayOfWeek dayOfWeek,
            @RequestParam(name = "department", required = false) String department,
            @RequestParam(name = "sectionName", required = false) String sectionName,
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "facultyId", required = false) Long facultyId,
            @RequestParam(name = "status", required = false) TimetableStatus status,
            @RequestParam(name = "search", required = false) String search,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        List<TimetableEntryDto> list = timetableService.filterTimetableEntries(
                dayOfWeek, department, sectionName, roomId, facultyId, status, search
        );
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/timetables/{id}")
    public ResponseEntity<ApiResponse<TimetableEntryDto>> getTimetableById(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            TimetableEntryDto entry = timetableService.getTimetableEntryById(id);
            return ResponseEntity.ok(ApiResponse.success(entry));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/timetables")
    public ResponseEntity<ApiResponse<TimetableEntryDto>> createTimetable(
            @Valid @RequestBody CreateTimetableRequest requestBody,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            TimetableEntryDto created = timetableService.createTimetableEntry(requestBody);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Timetable entry scheduled successfully.", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create timetable entry: " + e.getMessage()));
        }
    }

    @PutMapping("/timetables/{id}")
    public ResponseEntity<ApiResponse<TimetableEntryDto>> updateTimetable(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTimetableRequest requestBody,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            TimetableEntryDto updated = timetableService.updateTimetableEntry(id, requestBody);
            return ResponseEntity.ok(ApiResponse.success("Timetable entry updated successfully.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update timetable entry: " + e.getMessage()));
        }
    }

    @PutMapping("/timetables/{id}/allocate-room")
    public ResponseEntity<ApiResponse<TimetableEntryDto>> allocateRoom(
            @PathVariable("id") Long id,
            @Valid @RequestBody AllocateRoomRequest requestBody,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            TimetableEntryDto updated = timetableService.allocateRoom(id, requestBody.getRoomId());
            return ResponseEntity.ok(ApiResponse.success("Room allocated successfully.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to allocate room: " + e.getMessage()));
        }
    }

    @PutMapping("/timetables/{id}/deallocate-room")
    public ResponseEntity<ApiResponse<TimetableEntryDto>> deallocateRoom(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            TimetableEntryDto updated = timetableService.deallocateRoom(id);
            return ResponseEntity.ok(ApiResponse.success("Room deallocated successfully.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to deallocate room: " + e.getMessage()));
        }
    }

    @DeleteMapping("/timetables/{id}")
    public ResponseEntity<ApiResponse<TimetableEntryDto>> cancelTimetable(
            @PathVariable("id") Long id,
            @RequestParam(name = "reason", required = false) String reason,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            TimetableEntryDto cancelled = timetableService.cancelTimetableEntry(id, reason);
            return ResponseEntity.ok(ApiResponse.success("Timetable entry cancelled successfully.", cancelled));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to cancel timetable entry: " + e.getMessage()));
        }
    }

    @GetMapping("/timetables/stats")
    public ResponseEntity<ApiResponse<TimetableStatsDto>> getTimetableStats(HttpServletRequest request) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        TimetableStatsDto stats = timetableService.getTimetableStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // =========================================================================
    // ROOM AVAILABILITY ENGINE
    // =========================================================================

    @GetMapping("/rooms/availability")
    public ResponseEntity<ApiResponse<List<AvailableRoomDto>>> checkRoomAvailability(
            @RequestParam(name = "dayOfWeek") DayOfWeek dayOfWeek,
            @RequestParam(name = "scheduleDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduleDate,
            @RequestParam(name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(name = "minCapacity", required = false) Integer minCapacity,
            @RequestParam(name = "roomType", required = false) RoomType roomType,
            @RequestParam(name = "campusId", required = false) Long campusId,
            @RequestParam(name = "blockId", required = false) Long blockId,
            HttpServletRequest request
    ) {
        Role role = (Role) request.getAttribute("authenticatedRole");
        if (role != Role.ADMIN && role != Role.FACULTY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator or Faculty role required."));
        }

        try {
            RoomAvailabilityRequest req = new RoomAvailabilityRequest();
            req.setDayOfWeek(dayOfWeek);
            req.setScheduleDate(scheduleDate);
            req.setStartTime(startTime);
            req.setEndTime(endTime);
            req.setMinCapacity(minCapacity);
            req.setRoomType(roomType);
            req.setCampusId(campusId);
            req.setBlockId(blockId);

            List<AvailableRoomDto> availableRooms = timetableService.findAvailableRooms(req);
            return ResponseEntity.ok(ApiResponse.success(availableRooms));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error computing room availability: " + e.getMessage()));
        }
    }

    // =========================================================================
    // SUBJECTS DIRECTORY
    // =========================================================================

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<SubjectDto>>> getSubjects(
            @RequestParam(name = "department", required = false) String department,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        List<SubjectDto> subjects;
        if (department != null && !department.isBlank()) {
            subjects = timetableService.getSubjectsByDepartment(department.trim());
        } else {
            subjects = timetableService.getAllSubjects();
        }
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<SubjectDto>> createSubject(
            @Valid @RequestBody CreateSubjectRequest requestBody,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            SubjectDto created = timetableService.createSubject(requestBody);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Subject created successfully.", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create subject: " + e.getMessage()));
        }
    }
}
