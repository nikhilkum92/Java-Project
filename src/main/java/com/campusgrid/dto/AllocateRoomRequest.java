package com.campusgrid.dto;

import jakarta.validation.constraints.NotNull;

public class AllocateRoomRequest {

    @NotNull(message = "Room ID is required")
    private Long roomId;

    public AllocateRoomRequest() {
    }

    public AllocateRoomRequest(Long roomId) {
        this.roomId = roomId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
