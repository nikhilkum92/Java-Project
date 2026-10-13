package com.campusgrid.dto;

import java.time.LocalDateTime;

public class CampusDto {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String description;
    private boolean active;
    private int blockCount;
    private int roomCount;
    private int totalCapacity;
    private LocalDateTime createdAt;

    public CampusDto() {
    }

    public CampusDto(Long id, String name, String code, String address, String description, boolean active, int blockCount, int roomCount, int totalCapacity, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.address = address;
        this.description = description;
        this.active = active;
        this.blockCount = blockCount;
        this.roomCount = roomCount;
        this.totalCapacity = totalCapacity;
        this.createdAt = createdAt;
    }

    public CampusDto(Long id, String name, String code, String address, boolean active) {
        this(id, name, code, address, null, active, 0, 0, 0, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public int getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
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
