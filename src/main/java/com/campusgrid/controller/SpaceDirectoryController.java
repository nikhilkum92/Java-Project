package com.campusgrid.controller;

import com.campusgrid.dto.*;
import com.campusgrid.model.Role;
import com.campusgrid.model.RoomStatus;
import com.campusgrid.model.RoomType;
import com.campusgrid.service.SeatingLayoutService;
import com.campusgrid.service.SpaceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
public class SpaceDirectoryController {

    private final SpaceService spaceService;
    private final SeatingLayoutService seatingLayoutService;

    public SpaceDirectoryController(SpaceService spaceService, SeatingLayoutService seatingLayoutService) {
        this.spaceService = spaceService;
        this.seatingLayoutService = seatingLayoutService;
    }

    private boolean checkAuthenticated(HttpServletRequest request) {
        Role role = (Role) request.getAttribute("authenticatedRole");
        return role != null;
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<RoomDto>>> filterRooms(
            @RequestParam(name = "campusId", required = false) Long campusId,
            @RequestParam(name = "blockId", required = false) Long blockId,
            @RequestParam(name = "floorId", required = false) Long floorId,
            @RequestParam(name = "roomType", required = false) RoomType roomType,
            @RequestParam(name = "status", required = false) RoomStatus status,
            @RequestParam(name = "minCapacity", required = false) Integer minCapacity,
            @RequestParam(name = "search", required = false) String search,
            HttpServletRequest request
    ) {
        if (!checkAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to access space directory."));
        }
        List<RoomDto> rooms = spaceService.filterRooms(campusId, blockId, floorId, roomType, status, minCapacity, search);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<RoomDetailDto>> getRoomDetail(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to access space directory."));
        }
        try {
            RoomDetailDto detail = spaceService.getRoomDetail(id);
            return ResponseEntity.ok(ApiResponse.success(detail));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rooms/{id}/grid")
    public ResponseEntity<ApiResponse<SeatingGridDto>> getRoomGrid(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (!checkAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to view seating grid."));
        }
        try {
            SeatingGridDto grid = seatingLayoutService.getRoomSeatingGrid(id);
            return ResponseEntity.ok(ApiResponse.success(grid));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CampusTreeDto>>> getCampusTree(HttpServletRequest request) {
        if (!checkAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to view campus hierarchy."));
        }
        List<CampusTreeDto> tree = spaceService.getCampusTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SpaceStatsDto>> getSpaceStats(HttpServletRequest request) {
        if (!checkAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required to view space statistics."));
        }
        SpaceStatsDto stats = spaceService.getSpaceStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
