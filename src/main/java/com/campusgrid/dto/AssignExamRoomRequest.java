package com.campusgrid.dto;

import jakarta.validation.constraints.NotNull;

public class AssignExamRoomRequest {

    @NotNull(message = "Room ID is required.")
    private Long roomId;

    private Long invigilatorId;

    private int allocatedCapacity;

    private String notes;

    public AssignExamRoomRequest() {
    }

    public AssignExamRoomRequest(Long roomId, Long invigilatorId, int allocatedCapacity, String notes) {
        this.roomId = roomId;
        this.invigilatorId = invigilatorId;
        this.allocatedCapacity = allocatedCapacity;
        this.notes = notes;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getInvigilatorId() {
        return invigilatorId;
    }

    public void setInvigilatorId(Long invigilatorId) {
        this.invigilatorId = invigilatorId;
    }

    public int getAllocatedCapacity() {
        return allocatedCapacity;
    }

    public void setAllocatedCapacity(int allocatedCapacity) {
        this.allocatedCapacity = allocatedCapacity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
