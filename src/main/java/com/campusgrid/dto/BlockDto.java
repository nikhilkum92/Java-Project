package com.campusgrid.dto;

import java.time.LocalDateTime;

public class BlockDto {
    private Long id;
    private Long campusId;
    private String campusName;
    private String name;
    private String code;
    private String description;
    private boolean active;
    private int floorCount;
    private int roomCount;
    private int totalCapacity;
    private LocalDateTime createdAt;

    public BlockDto() {
    }

    public BlockDto(Long id, Long campusId, String campusName, String name, String code, String description, boolean active, int floorCount, int roomCount, int totalCapacity, LocalDateTime createdAt) {
        this.id = id;
        this.campusId = campusId;
        this.campusName = campusName;
        this.name = name;
        this.code = code;
        this.description = description;
        this.active = active;
        this.floorCount = floorCount;
        this.roomCount = roomCount;
        this.totalCapacity = totalCapacity;
        this.createdAt = createdAt;
    }

    public BlockDto(Long id, Long campusId, String campusName, String name, String code, boolean active) {
        this(id, campusId, campusName, name, code, null, active, 0, 0, 0, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getFloorCount() {
        return floorCount;
    }

    public void setFloorCount(int floorCount) {
        this.floorCount = floorCount;
    }

    public int getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(int roomCount) {
        this.roomCount = roomCount;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
