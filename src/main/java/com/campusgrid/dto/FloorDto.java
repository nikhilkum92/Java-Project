package com.campusgrid.dto;

import java.time.LocalDateTime;

public class FloorDto {
    private Long id;
    private Long blockId;
    private String blockName;
    private Long campusId;
    private String campusName;
    private int floorNumber;
    private String name;
    private String description;
    private boolean active;
    private int roomCount;
    private int totalCapacity;
    private LocalDateTime createdAt;

    public FloorDto() {
    }

    public FloorDto(Long id, Long blockId, String blockName, Long campusId, String campusName, int floorNumber, String name, String description, boolean active, int roomCount, int totalCapacity, LocalDateTime createdAt) {
        this.id = id;
        this.blockId = blockId;
        this.blockName = blockName;
        this.campusId = campusId;
        this.campusName = campusName;
        this.floorNumber = floorNumber;
        this.name = name;
        this.description = description;
        this.active = active;
        this.roomCount = roomCount;
        this.totalCapacity = totalCapacity;
        this.createdAt = createdAt;
    }

    public FloorDto(Long id, Long blockId, String blockName, int floorNumber, String name, boolean active) {
        this(id, blockId, blockName, null, null, floorNumber, name, null, active, 0, 0, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
