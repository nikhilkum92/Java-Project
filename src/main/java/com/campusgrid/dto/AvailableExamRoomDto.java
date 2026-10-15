package com.campusgrid.dto;

import com.campusgrid.model.RoomType;

public class AvailableExamRoomDto {

    private Long roomId;
    private String roomCode;
    private String roomName;
    private RoomType roomType;
    private String blockName;
    private int floorNumber;
    private int roomCapacity;
    private int totalPhysicalSeats;
    private int usableSeats;
    private int unavailableSeats;
    private boolean readyForSeating;
    private String unavailabilityReason;
    private boolean assignedToThisExam;

    public AvailableExamRoomDto() {
    }

    public AvailableExamRoomDto(Long roomId, String roomCode, String roomName, RoomType roomType,
                                String blockName, int floorNumber, int roomCapacity, int totalPhysicalSeats,
                                int usableSeats, int unavailableSeats, boolean readyForSeating,
                                String unavailabilityReason, boolean assignedToThisExam) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.roomType = roomType;
        this.blockName = blockName;
        this.floorNumber = floorNumber;
        this.roomCapacity = roomCapacity;
        this.totalPhysicalSeats = totalPhysicalSeats;
        this.usableSeats = usableSeats;
        this.unavailableSeats = unavailableSeats;
        this.readyForSeating = readyForSeating;
        this.unavailabilityReason = unavailabilityReason;
        this.assignedToThisExam = assignedToThisExam;
    }

    // Getters and Setters
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
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

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }

    public void setRoomCapacity(int roomCapacity) {
        this.roomCapacity = roomCapacity;
    }

    public int getTotalPhysicalSeats() {
        return totalPhysicalSeats;
    }

    public void setTotalPhysicalSeats(int totalPhysicalSeats) {
        this.totalPhysicalSeats = totalPhysicalSeats;
    }

    public int getUsableSeats() {
        return usableSeats;
    }

    public void setUsableSeats(int usableSeats) {
        this.usableSeats = usableSeats;
    }

    public int getUnavailableSeats() {
        return unavailableSeats;
    }

    public void setUnavailableSeats(int unavailableSeats) {
        this.unavailableSeats = unavailableSeats;
    }

    public boolean isReadyForSeating() {
        return readyForSeating;
    }

    public void setReadyForSeating(boolean readyForSeating) {
        this.readyForSeating = readyForSeating;
    }

    public String getUnavailabilityReason() {
        return unavailabilityReason;
    }

    public void setUnavailabilityReason(String unavailabilityReason) {
        this.unavailabilityReason = unavailabilityReason;
    }

    public boolean isAssignedToThisExam() {
        return assignedToThisExam;
    }

    public void setAssignedToThisExam(boolean assignedToThisExam) {
        this.assignedToThisExam = assignedToThisExam;
    }
}
