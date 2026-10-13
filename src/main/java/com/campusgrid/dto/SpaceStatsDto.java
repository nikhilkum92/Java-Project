package com.campusgrid.dto;

public class SpaceStatsDto {
    private long totalCampuses;
    private long totalBlocks;
    private long totalFloors;
    private long totalRooms;
    private long totalCapacity;
    private long classroomsCount;
    private long laboratoriesCount;
    private long auditoriumsCount;
    private long availableRoomsCount;
    private long occupiedRoomsCount;
    private long maintenanceRoomsCount;

    public SpaceStatsDto() {
    }

    public SpaceStatsDto(long totalCampuses, long totalBlocks, long totalFloors, long totalRooms, long totalCapacity, long classroomsCount, long laboratoriesCount, long auditoriumsCount, long availableRoomsCount, long occupiedRoomsCount, long maintenanceRoomsCount) {
        this.totalCampuses = totalCampuses;
        this.totalBlocks = totalBlocks;
        this.totalFloors = totalFloors;
        this.totalRooms = totalRooms;
        this.totalCapacity = totalCapacity;
        this.classroomsCount = classroomsCount;
        this.laboratoriesCount = laboratoriesCount;
        this.auditoriumsCount = auditoriumsCount;
        this.availableRoomsCount = availableRoomsCount;
        this.occupiedRoomsCount = occupiedRoomsCount;
        this.maintenanceRoomsCount = maintenanceRoomsCount;
    }

    public long getTotalCampuses() {
        return totalCampuses;
    }

    public void setTotalCampuses(long totalCampuses) {
        this.totalCampuses = totalCampuses;
    }

    public long getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(long totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    public long getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(long totalFloors) {
        this.totalFloors = totalFloors;
    }

    public long getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(long totalRooms) {
        this.totalRooms = totalRooms;
    }

    public long getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(long totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public long getClassroomsCount() {
        return classroomsCount;
    }

    public void setClassroomsCount(long classroomsCount) {
        this.classroomsCount = classroomsCount;
    }

    public long getLaboratoriesCount() {
        return laboratoriesCount;
    }

    public void setLaboratoriesCount(long laboratoriesCount) {
        this.laboratoriesCount = laboratoriesCount;
    }

    public long getAuditoriumsCount() {
        return auditoriumsCount;
    }

    public void setAuditoriumsCount(long auditoriumsCount) {
        this.auditoriumsCount = auditoriumsCount;
    }

    public long getAvailableRoomsCount() {
        return availableRoomsCount;
    }

    public void setAvailableRoomsCount(long availableRoomsCount) {
        this.availableRoomsCount = availableRoomsCount;
    }

    public long getOccupiedRoomsCount() {
        return occupiedRoomsCount;
    }

    public void setOccupiedRoomsCount(long occupiedRoomsCount) {
        this.occupiedRoomsCount = occupiedRoomsCount;
    }

    public long getMaintenanceRoomsCount() {
        return maintenanceRoomsCount;
    }

    public void setMaintenanceRoomsCount(long maintenanceRoomsCount) {
        this.maintenanceRoomsCount = maintenanceRoomsCount;
    }
}
