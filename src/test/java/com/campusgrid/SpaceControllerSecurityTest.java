package com.campusgrid;

import com.campusgrid.controller.SpaceDirectoryController;
import com.campusgrid.controller.SpaceManagementController;
import com.campusgrid.model.Role;
import com.campusgrid.service.SeatingLayoutService;
import com.campusgrid.service.SpaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SpaceManagementController.class, SpaceDirectoryController.class})
class SpaceControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpaceService spaceService;

    @MockBean
    private SeatingLayoutService seatingLayoutService;

    @MockBean
    private com.campusgrid.service.JwtService jwtService;

    // --- Admin Space Management Authorization ---

    @Test
    @DisplayName("Should block anonymous request to /api/admin/spaces/campuses with 403 Forbidden")
    void testAnonymousAccessToAdminCampusesBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/spaces/campuses"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should block student role from mutating spaces with 403 Forbidden")
    void testStudentMutationBlocked() throws Exception {
        mockMvc.perform(post("/api/admin/spaces/campuses")
                        .contentType("application/json")
                        .content("{\"name\":\"New Campus\",\"code\":\"NC\"}")
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should block faculty role from mutating spaces with 403 Forbidden")
    void testFacultyMutationBlocked() throws Exception {
        mockMvc.perform(post("/api/admin/spaces/campuses")
                        .contentType("application/json")
                        .content("{\"name\":\"New Campus\",\"code\":\"NC\"}")
                        .requestAttr("authenticatedRole", Role.FACULTY))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin role to access /api/admin/spaces/campuses with 200 OK")
    void testAdminAccessAllowed() throws Exception {
        when(spaceService.getAllCampuses()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/spaces/campuses")
                        .requestAttr("authenticatedRole", Role.ADMIN))
                .andExpect(status().isOk());
    }

    // --- Space Directory Authorization (All authenticated roles) ---

    @Test
    @DisplayName("Should block anonymous requests to /api/spaces/rooms with 401 Unauthorized")
    void testAnonymousAccessToDirectoryBlocked() throws Exception {
        mockMvc.perform(get("/api/spaces/rooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow student role to view /api/spaces/rooms directory with 200 OK")
    void testStudentDirectoryAccessAllowed() throws Exception {
        when(spaceService.filterRooms(null, null, null, null, null, null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/spaces/rooms")
                        .requestAttr("authenticatedRole", Role.STUDENT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow faculty role to view /api/spaces/rooms directory with 200 OK")
    void testFacultyDirectoryAccessAllowed() throws Exception {
        when(spaceService.filterRooms(null, null, null, null, null, null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/spaces/rooms")
                        .requestAttr("authenticatedRole", Role.FACULTY))
                .andExpect(status().isOk());
    }
}
