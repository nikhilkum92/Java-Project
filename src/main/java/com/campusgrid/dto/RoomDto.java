package com.campusgrid.dto;

import com.campusgrid.model.LayoutTemplate;
import com.campusgrid.model.RoomStatus;
import com.campusgrid.model.RoomType;

import java.time.LocalDateTime;

public class RoomDto {
    private Long id;
    private Long floorId;
    private int floorNumber;
    private String floorName;
    private Long blockId;
    private String blockName;
    private String blockCode;
    private Long campusId;
    private String campusName;
    private String roomCode;
    private String roomName;
    private RoomType roomType;
    private int capacity;
    private int systemCount;
    private int rowsCount;
    private int columnsCount;
    private LayoutTemplate layoutTemplate;
    private RoomStatus status;
    private String facilities;
    private String maintenanceReason;
    private boolean active;
    private int seatCount;
    private LocalDateTime createdAt;

    public RoomDto() {
    }

    public RoomDto(Long id, Long floorId, int floorNumber, String floorName, Long blockId, String blockName, String blockCode, Long campusId, String campusName, String roomCode, String roomName, RoomType roomType, int capacity, int systemCount, int rowsCount, int columnsCount, LayoutTemplate layoutTemplate, RoomStatus status, String facilities, String maintenanceReason, boolean active, int seatCount, LocalDateTime createdAt) {
        this.id = id;
        this.floorId = floorId;
        this.floorNumber = floorNumber;
        this.floorName = floorName;
        this.blockId = blockId;
        this.blockName = blockName;
        this.blockCode = blockCode;
        this.campusId = campusId;
        this.campusName = campusName;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.roomType = roomType;
        this.capacity = capacity;
        this.systemCount = systemCount;
        this.rowsCount = rowsCount;
        this.columnsCount = columnsCount;
        this.layoutTemplate = layoutTemplate;
        this.status = status;
        this.facilities = facilities;
        this.maintenanceReason = maintenanceReason;
        this.active = active;
        this.seatCount = seatCount;
        this.createdAt = createdAt;
    }

    public RoomDto(Long id, Long floorId, Long campusId, String campusName, Long blockId, String blockName, String floorName, String roomCode, String roomName, RoomType roomType, int capacity, String facilities, RoomStatus status, LayoutTemplate layoutTemplate, int rowsCount, int columnsCount, int systemCount, boolean active) {
        this(id, floorId, 0, floorName, blockId, blockName, null, campusId, campusName, roomCode, roomName, roomType, capacity, systemCount, rowsCount, columnsCount, layoutTemplate, status, facilities, null, active, 0, null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFloorId() {
        return floorId;
    }

    public void setFloorId(Long floorId) {
        this.floorId = floorId;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getBlockCode() {
        return blockCode;
    }

    public void setBlockCode(String blockCode) {
        this.blockCode = blockCode;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(int systemCount) {
        this.systemCount = systemCount;
    }

    public int getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(int rowsCount) {
        this.rowsCount = rowsCount;
    }

    public int getColumnsCount() {
        return columnsCount;
    }

    public void setColumnsCount(int columnsCount) {
        this.columnsCount = columnsCount;
    }

    public LayoutTemplate getLayoutTemplate() {
        return layoutTemplate;
    }

    public void setLayoutTemplate(LayoutTemplate layoutTemplate) {
        this.layoutTemplate = layoutTemplate;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getMaintenanceReason() {
        return maintenanceReason;
    }

    public void setMaintenanceReason(String maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
