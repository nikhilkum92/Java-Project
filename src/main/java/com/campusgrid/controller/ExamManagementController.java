package com.campusgrid.controller;

import com.campusgrid.dto.*;
import com.campusgrid.model.ExamStatus;
import com.campusgrid.model.Role;
import com.campusgrid.service.ExamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/admin/examinations", "/api/admin/exams"})
public class ExamManagementController {

    private final ExamService examService;

    public ExamManagementController(ExamService examService) {
        this.examService = examService;
    }

    private boolean checkAdminAuthorization(HttpServletRequest request) {
        Role role = (Role) request.getAttribute("authenticatedRole");
        return role == Role.ADMIN;
    }

    private boolean checkAdminOrFacultyAuthorization(HttpServletRequest request) {
        Role role = (Role) request.getAttribute("authenticatedRole");
        return role == Role.ADMIN || role == Role.FACULTY;
    }

    // =========================================================================
    // EXAM MANAGEMENT ENDPOINTS
    // =========================================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExamDetailDto>>> getExams(
            @RequestParam(name = "department", required = false) String department,
            @RequestParam(name = "semester", required = false) Integer semester,
            @RequestParam(name = "status", required = false) ExamStatus status,
            @RequestParam(name = "search", required = false) String search,
            HttpServletRequest request
    ) {
        if (!checkAdminOrFacultyAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrative or Faculty privilege required."));
        }

        List<ExamDetailDto> list = examService.filterExams(department, semester, status, search);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ExamStatsDto>> getStats(HttpServletRequest request) {
        if (!checkAdminOrFacultyAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrative privilege required."));
        }

        ExamStatsDto stats = examService.getExamStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/my-duties")
    public ResponseEntity<ApiResponse<List<ExamRoomAllocationDto>>> getMyInvigilationDuties(HttpServletRequest request) {
        if (!checkAdminOrFacultyAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrative or Faculty privilege required."));
        }
        Long userId = (Long) request.getAttribute("authenticatedUserId");
        List<ExamRoomAllocationDto> duties = examService.getFacultyInvigilationDuties(userId);
        return ResponseEntity.ok(ApiResponse.success(duties));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamDetailDto>> getExamById(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminOrFacultyAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrative or Faculty privilege required."));
        }

        try {
            ExamDetailDto dto = examService.getExamById(id);
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<ExamDetailDto>> getExamSummary(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        return getExamById(id, request);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExamDetailDto>> createExam(
            @Valid @RequestBody CreateExamRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required to schedule exams."));
        }

        try {
            ExamDetailDto created = examService.createExam(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Examination session scheduled successfully.", created));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamDetailDto>> updateExam(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateExamRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            ExamDetailDto updated = examService.updateExam(id, req);
            return ResponseEntity.ok(ApiResponse.success("Examination details updated successfully.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ExamDetailDto>> cancelExam(
            @PathVariable("id") Long id,
            @RequestBody(required = false) CancelExamRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            String reason = (req != null && req.getReason() != null) ? req.getReason() : "Cancelled by examination administrator";
            ExamDetailDto cancelled = examService.cancelExam(id, reason);
            return ResponseEntity.ok(ApiResponse.success("Examination session cancelled successfully.", cancelled));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExam(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            examService.deleteExam(id);
            return ResponseEntity.ok(ApiResponse.success("Exam session and associated allocations deleted successfully.", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // =========================================================================
    // ROOM ALLOCATION & AVAILABILITY ENDPOINTS
    // =========================================================================

    @GetMapping("/{id}/available-rooms")
    public ResponseEntity<ApiResponse<List<AvailableExamRoomDto>>> getAvailableRooms(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminOrFacultyAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrative privilege required."));
        }

        try {
            List<AvailableExamRoomDto> rooms = examService.getAvailableRoomsForExam(id);
            return ResponseEntity.ok(ApiResponse.success(rooms));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/rooms")
    public ResponseEntity<ApiResponse<ExamRoomAllocationDto>> assignRoom(
            @PathVariable("id") Long id,
            @Valid @RequestBody AssignExamRoomRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            ExamRoomAllocationDto dto = examService.assignRoomToExam(id, req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Examination hall assigned successfully.", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/rooms/{roomId}")
    public ResponseEntity<ApiResponse<Void>> unassignRoom(
            @PathVariable("id") Long id,
            @PathVariable("roomId") Long roomId,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            examService.unassignRoomFromExam(id, roomId);
            return ResponseEntity.ok(ApiResponse.success("Examination hall unassigned successfully.", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // =========================================================================
    // PHYSICAL SEAT ALLOCATION & GENERATION ENDPOINTS
    // =========================================================================

    @PostMapping({"/{id}/allocate-seats", "/{id}/generate-seating"})
    public ResponseEntity<ApiResponse<ExamDetailDto>> allocateSeats(
            @PathVariable("id") Long id,
            @RequestBody(required = false) AllocateSeatsRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        if (req == null) {
            req = new AllocateSeatsRequest();
        }

        try {
            ExamDetailDto result = examService.allocateSeats(id, req);
            return ResponseEntity.ok(ApiResponse.success("Automated examination seating generated successfully.", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping({"/{id}/allocations", "/{id}/seating"})
    public ResponseEntity<ApiResponse<Void>> clearSeating(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }

        try {
            examService.clearSeatingArrangement(id);
            return ResponseEntity.ok(ApiResponse.success("Seating allocation cleared for this exam session.", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/allocations")
    public ResponseEntity<ApiResponse<List<ExamRosterStudentDto>>> getExamAllocations(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminOrFacultyAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrative or Faculty privilege required."));
        }

        try {
            List<ExamRosterStudentDto> list = examService.getExamAllocations(id);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping({"/{id}/seating-plan", "/{id}/rooms/{roomId}/seating-plan", "/{id}/rooms/{roomId}/seating-grid"})
    public ResponseEntity<ApiResponse<ExamSeatingGridDto>> getSeatingPlan(
            @PathVariable("id") Long id,
            @PathVariable(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "roomId", required = false) Long queryRoomId,
            HttpServletRequest request
    ) {
        if (!checkAdminOrFacultyAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied."));
        }

        Long targetRoomId = roomId != null ? roomId : queryRoomId;

        try {
            if (targetRoomId == null) {
                // Default to the first assigned room of this exam
                ExamDetailDto exam = examService.getExamById(id);
                if (exam.getRooms() == null || exam.getRooms().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("No examination rooms have been assigned to this exam."));
                }
                targetRoomId = exam.getRooms().get(0).getRoomId();
            }

            ExamSeatingGridDto grid = examService.getExamRoomSeatingPlan(id, targetRoomId);
            return ResponseEntity.ok(ApiResponse.success(grid));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/rooms/{roomId}/roster")
    public ResponseEntity<ApiResponse<List<ExamRosterStudentDto>>> getRoomRoster(
            @PathVariable("id") Long id,
            @PathVariable("roomId") Long roomId,
            HttpServletRequest request
    ) {
        if (!checkAdminOrFacultyAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied."));
        }

        try {
            List<ExamRosterStudentDto> roster = examService.getExamRoomRoster(id, roomId);
            return ResponseEntity.ok(ApiResponse.success(roster));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/attendance/{allocationId}")
    public ResponseEntity<ApiResponse<Void>> markAttendance(
            @PathVariable("allocationId") Long allocationId,
            @Valid @RequestBody MarkExamAttendanceRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminOrFacultyAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Invigilator or Administrator access required."));
        }

        try {
            examService.markAttendance(allocationId, req);
            return ResponseEntity.ok(ApiResponse.success("Candidate attendance marked successfully.", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
