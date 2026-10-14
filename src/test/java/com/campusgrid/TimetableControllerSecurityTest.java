package com.campusgrid;

import com.campusgrid.controller.TimetableDirectoryController;
import com.campusgrid.controller.TimetableManagementController;
import com.campusgrid.dto.TimetableEntryDto;
import com.campusgrid.model.Role;
import com.campusgrid.service.TimetableService;
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

@WebMvcTest({TimetableManagementController.class, TimetableDirectoryController.class})
class TimetableControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimetableService timetableService;

    @MockBean
    private com.campusgrid.service.JwtService jwtService;

    // --- Admin Timetable Authorization Tests ---

    @Test
    @DisplayName("Should block anonymous request to /api/admin/timetables with 403 Forbidden")
    void testAnonymousAccessToAdminTimetablesBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/timetables"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should block student role from mutating timetables with 403 Forbidden")
    void testStudentMutationBlocked() throws Exception {
        mockMvc.perform(post("/api/admin/timetables")
                        .contentType("application/json")
                        .content("{\"subjectId\": 1, \"department\": \"CS\", \"sectionName\": \"CSE-A\", \"dayOfWeek\": \"MONDAY\", \"startTime\": \"09:00\", \"endTime\": \"10:00\"}")
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should block faculty role from mutating timetables with 403 Forbidden")
    void testFacultyMutationBlocked() throws Exception {
        mockMvc.perform(post("/api/admin/timetables")
                        .contentType("application/json")
                        .content("{\"subjectId\": 1, \"department\": \"CS\", \"sectionName\": \"CSE-A\", \"dayOfWeek\": \"MONDAY\", \"startTime\": \"09:00\", \"endTime\": \"10:00\"}")
                        .requestAttr("authenticatedRole", Role.FACULTY))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin role to access /api/admin/timetables with 200 OK")
    void testAdminAccessAllowed() throws Exception {
        when(timetableService.filterTimetableEntries(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/timetables")
                        .requestAttr("authenticatedRole", Role.ADMIN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow admin role to allocate room with 200 OK")
    void testAdminAllocateRoomAllowed() throws Exception {
        when(timetableService.allocateRoom(any(), any())).thenReturn(new TimetableEntryDto());

        mockMvc.perform(put("/api/admin/timetables/1/allocate-room")
                        .contentType("application/json")
                        .content("{\"roomId\": 101}")
                        .requestAttr("authenticatedRole", Role.ADMIN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should block student role from checking admin room availability with 403 Forbidden")
    void testStudentRoomAvailabilityBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/rooms/availability")
                        .param("dayOfWeek", "MONDAY")
                        .param("startTime", "09:00")
                        .param("endTime", "10:00")
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow faculty role to check room availability with 200 OK")
    void testFacultyRoomAvailabilityAllowed() throws Exception {
        when(timetableService.findAvailableRooms(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/rooms/availability")
                        .param("dayOfWeek", "MONDAY")
                        .param("startTime", "09:00")
                        .param("endTime", "10:00")
                        .requestAttr("authenticatedRole", Role.FACULTY))
                .andExpect(status().isOk());
    }

    // --- Timetable Directory Authorization Tests ---

    @Test
    @DisplayName("Should block anonymous request to /api/timetables/rooms/{id} with 401 Unauthorized")
    void testAnonymousDirectoryAccessBlocked() throws Exception {
        mockMvc.perform(get("/api/timetables/rooms/101"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow authenticated student to view room schedule with 200 OK")
    void testAuthenticatedStudentCanViewRoomSchedule() throws Exception {
        when(timetableService.getTimetableByRoom(101L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/timetables/rooms/101")
                        .requestAttr("authenticatedUserId", 10L)
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk());
    }
}
