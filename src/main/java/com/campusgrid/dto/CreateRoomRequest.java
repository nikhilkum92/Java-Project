package com.campusgrid.dto;

import com.campusgrid.model.LayoutTemplate;
import com.campusgrid.model.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateRoomRequest {

    @NotBlank(message = "Room code is required.")
    @Size(max = 50, message = "Room code must not exceed 50 characters.")
    private String roomCode;

    @NotBlank(message = "Room name is required.")
    @Size(max = 100, message = "Room name must not exceed 100 characters.")
    private String roomName;

    @NotNull(message = "Room type is required.")
    private RoomType roomType;

    @NotNull(message = "Capacity is required.")
    @Min(value = 1, message = "Capacity must be a positive integer greater than zero.")
    private Integer capacity;

    private Integer systemCount = 0;

    @Min(value = 1, message = "Rows count must be at least 1.")
    private Integer rowsCount = 6;

    @Min(value = 1, message = "Columns count must be at least 1.")
    private Integer columnsCount = 6;

    private LayoutTemplate layoutTemplate = LayoutTemplate.STANDARD_GRID;

    @Size(max = 500, message = "Facilities must not exceed 500 characters.")
    private String facilities;

    private Long floorId;

    private com.campusgrid.model.RoomStatus status = com.campusgrid.model.RoomStatus.AVAILABLE;

    public CreateRoomRequest() {
    }

    public CreateRoomRequest(Long floorId, String roomCode, String roomName, RoomType roomType, Integer capacity, String facilities, com.campusgrid.model.RoomStatus status) {
        this.floorId = floorId;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.roomType = roomType;
        this.capacity = capacity;
        this.facilities = facilities;
        this.status = status != null ? status : com.campusgrid.model.RoomStatus.AVAILABLE;
    }

    public CreateRoomRequest(String roomCode, String roomName, RoomType roomType, Integer capacity, Integer systemCount, Integer rowsCount, Integer columnsCount, LayoutTemplate layoutTemplate, String facilities) {
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.roomType = roomType;
        this.capacity = capacity;
        this.systemCount = systemCount != null ? systemCount : 0;
        this.rowsCount = rowsCount != null ? rowsCount : 6;
        this.columnsCount = columnsCount != null ? columnsCount : 6;
        this.layoutTemplate = layoutTemplate != null ? layoutTemplate : LayoutTemplate.STANDARD_GRID;
        this.facilities = facilities;
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(Integer systemCount) {
        this.systemCount = systemCount;
    }

    public Integer getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(Integer rowsCount) {
        this.rowsCount = rowsCount;
    }

    public Integer getColumnsCount() {
        return columnsCount;
    }

    public void setColumnsCount(Integer columnsCount) {
        this.columnsCount = columnsCount;
    }

    public LayoutTemplate getLayoutTemplate() {
        return layoutTemplate;
    }

    public void setLayoutTemplate(LayoutTemplate layoutTemplate) {
        this.layoutTemplate = layoutTemplate;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public Long getFloorId() {
        return floorId;
    }

    public void setFloorId(Long floorId) {
        this.floorId = floorId;
    }

    public com.campusgrid.model.RoomStatus getStatus() {
        return status;
    }

    public void setStatus(com.campusgrid.model.RoomStatus status) {
        this.status = status;
    }
}
