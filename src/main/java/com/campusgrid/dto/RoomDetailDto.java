package com.campusgrid.dto;

import java.util.ArrayList;
import java.util.List;

public class RoomDetailDto {
    private RoomDto room;
    private List<String> facilitiesList = new ArrayList<>();
    private String hierarchyPath; // e.g. "Main Campus > Block A > 2nd Floor > A-201"
    private int configuredSeats;
    private int availableSeats;
    private int blockedSeats;
    
    // Explicit Scheduling Integration Boundary
    private String scheduleIntegrationStatus; // "PENDING_MODULE_6_TIMETABLE"
    private Object currentSchedule; // null until Module 6
    private List<Object> upcomingSchedules = new ArrayList<>(); // empty until Module 6

    public RoomDetailDto() {
    }

    public RoomDetailDto(RoomDto room, List<String> facilitiesList, String hierarchyPath, int configuredSeats, int availableSeats, int blockedSeats) {
        this.room = room;
        this.facilitiesList = facilitiesList != null ? facilitiesList : new ArrayList<>();
        this.hierarchyPath = hierarchyPath;
        this.configuredSeats = configuredSeats;
        this.availableSeats = availableSeats;
        this.blockedSeats = blockedSeats;
        this.scheduleIntegrationStatus = "SCHEDULE_INTEGRATION_PENDING_MODULE_6";
        this.currentSchedule = null;
        this.upcomingSchedules = new ArrayList<>();
    }

    public RoomDto getRoom() {
        return room;
    }

    public void setRoom(RoomDto room) {
        this.room = room;
    }

    public List<String> getFacilitiesList() {
        return facilitiesList;
    }

    public void setFacilitiesList(List<String> facilitiesList) {
        this.facilitiesList = facilitiesList;
    }

    public String getHierarchyPath() {
        return hierarchyPath;
    }

    public void setHierarchyPath(String hierarchyPath) {
        this.hierarchyPath = hierarchyPath;
    }

    public int getConfiguredSeats() {
        return configuredSeats;
    }

    public void setConfiguredSeats(int configuredSeats) {
        this.configuredSeats = configuredSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public int getBlockedSeats() {
        return blockedSeats;
    }

    public void setBlockedSeats(int blockedSeats) {
        this.blockedSeats = blockedSeats;
    }

    public String getScheduleIntegrationStatus() {
        return scheduleIntegrationStatus;
    }

    public void setScheduleIntegrationStatus(String scheduleIntegrationStatus) {
        this.scheduleIntegrationStatus = scheduleIntegrationStatus;
    }

    public Object getCurrentSchedule() {
        return currentSchedule;
    }

    public void setCurrentSchedule(Object currentSchedule) {
        this.currentSchedule = currentSchedule;
    }

    public List<Object> getUpcomingSchedules() {
        return upcomingSchedules;
    }

    public void setUpcomingSchedules(List<Object> upcomingSchedules) {
        this.upcomingSchedules = upcomingSchedules;
    }
}
