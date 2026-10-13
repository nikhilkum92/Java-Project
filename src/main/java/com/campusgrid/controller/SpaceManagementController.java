package com.campusgrid.controller;

import com.campusgrid.dto.*;
import com.campusgrid.model.Role;
import com.campusgrid.model.SeatStatus;
import com.campusgrid.service.SeatingLayoutService;
import com.campusgrid.service.SpaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/spaces")
public class SpaceManagementController {

    private final SpaceService spaceService;
    private final SeatingLayoutService seatingLayoutService;

    public SpaceManagementController(SpaceService spaceService, SeatingLayoutService seatingLayoutService) {
        this.spaceService = spaceService;
        this.seatingLayoutService = seatingLayoutService;
    }

    private boolean checkAdminAuthorization(HttpServletRequest request) {
        Role role = (Role) request.getAttribute("authenticatedRole");
        return role == Role.ADMIN;
    }

    // ==========================================
    // Campus Endpoints
    // ==========================================

    @GetMapping("/campuses")
    public ResponseEntity<ApiResponse<List<CampusDto>>> getAllCampuses(HttpServletRequest request) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        return ResponseEntity.ok(ApiResponse.success(spaceService.getAllCampuses()));
    }

    @PostMapping("/campuses")
    public ResponseEntity<ApiResponse<CampusDto>> createCampus(
            @Valid @RequestBody CreateCampusRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            CampusDto created = spaceService.createCampus(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Campus created successfully.", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/campuses/{id}")
    public ResponseEntity<ApiResponse<CampusDto>> updateCampus(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateCampusRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            CampusDto updated = spaceService.updateCampus(id, req);
            return ResponseEntity.ok(ApiResponse.success("Campus updated successfully.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/campuses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampus(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            spaceService.deleteCampus(id);
            return ResponseEntity.ok(ApiResponse.success("Campus deleted successfully.", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // Block Endpoints
    // ==========================================

    @GetMapping("/campuses/{campusId}/blocks")
    public ResponseEntity<ApiResponse<List<BlockDto>>> getBlocksByCampus(
            @PathVariable("campusId") Long campusId,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        return ResponseEntity.ok(ApiResponse.success(spaceService.getBlocksByCampus(campusId)));
    }

    @PostMapping("/blocks")
    public ResponseEntity<ApiResponse<BlockDto>> createBlock(
            @Valid @RequestBody CreateBlockRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            BlockDto created = spaceService.createBlock(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Block created successfully.", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/blocks/{id}")
    public ResponseEntity<ApiResponse<BlockDto>> updateBlock(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateBlockRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            BlockDto updated = spaceService.updateBlock(id, req);
            return ResponseEntity.ok(ApiResponse.success("Block updated successfully.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/blocks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlock(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            spaceService.deleteBlock(id);
            return ResponseEntity.ok(ApiResponse.success("Block deleted successfully.", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // Floor Endpoints
    // ==========================================

    @GetMapping("/blocks/{blockId}/floors")
    public ResponseEntity<ApiResponse<List<FloorDto>>> getFloorsByBlock(
            @PathVariable("blockId") Long blockId,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        return ResponseEntity.ok(ApiResponse.success(spaceService.getFloorsByBlock(blockId)));
    }

    @PostMapping("/floors")
    public ResponseEntity<ApiResponse<FloorDto>> createFloor(
            @Valid @RequestBody CreateFloorRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            FloorDto created = spaceService.createFloor(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Floor created successfully.", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/floors/{id}")
    public ResponseEntity<ApiResponse<FloorDto>> updateFloor(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateFloorRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            FloorDto updated = spaceService.updateFloor(id, req);
            return ResponseEntity.ok(ApiResponse.success("Floor updated successfully.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/floors/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFloor(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            spaceService.deleteFloor(id);
            return ResponseEntity.ok(ApiResponse.success("Floor deleted successfully.", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // Room Endpoints (Mutations)
    // ==========================================

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<RoomDto>> createRoom(
            @Valid @RequestBody CreateRoomRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            RoomDto created = spaceService.createRoom(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Room created successfully.", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<RoomDto>> updateRoom(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateRoomRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            RoomDto updated = spaceService.updateRoom(id, req);
            return ResponseEntity.ok(ApiResponse.success("Room updated successfully.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            spaceService.deleteRoom(id);
            return ResponseEntity.ok(ApiResponse.success("Room deleted successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // Seating Layout Designer Endpoints
    // ==========================================

    @PostMapping("/rooms/{id}/layout")
    public ResponseEntity<ApiResponse<SeatingGridDto>> configureLayout(
            @PathVariable("id") Long id,
            @Valid @RequestBody ConfigureLayoutRequest req,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            SeatingGridDto grid = seatingLayoutService.configureAndGenerateLayout(id, req);
            return ResponseEntity.ok(ApiResponse.success("Seating layout configured successfully.", grid));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/rooms/{roomId}/seats/{seatId}/status")
    public ResponseEntity<ApiResponse<SeatDto>> updateSeatStatus(
            @PathVariable("roomId") Long roomId,
            @PathVariable("seatId") Long seatId,
            @RequestParam("status") SeatStatus status,
            HttpServletRequest request
    ) {
        if (!checkAdminAuthorization(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator role required."));
        }
        try {
            SeatDto updated = seatingLayoutService.updateSeatStatus(roomId, seatId, status);
            return ResponseEntity.ok(ApiResponse.success("Seat status updated to " + status + ".", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
