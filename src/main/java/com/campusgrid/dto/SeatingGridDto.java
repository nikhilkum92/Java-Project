package com.campusgrid.dto;

import com.campusgrid.model.LayoutTemplate;
import com.campusgrid.model.RoomType;

import java.util.ArrayList;
import java.util.List;

public class SeatingGridDto {
    private Long roomId;
    private String roomCode;
    private String roomName;
    private RoomType roomType;
    private int capacity;
    private LayoutTemplate layoutTemplate;
    private int rowsCount;
    private int columnsCount;
    private int configuredSeatsCount;
    private int availableSeatsCount;
    private int blockedSeatsCount;
    private List<Integer> aisleColumns = new ArrayList<>();
    private List<Integer> aisleRows = new ArrayList<>();
    private List<SeatDto> seats = new ArrayList<>();

    public SeatingGridDto() {
    }

    public SeatingGridDto(Long roomId, String roomCode, String roomName, RoomType roomType, int capacity, LayoutTemplate layoutTemplate, int rowsCount, int columnsCount, int configuredSeatsCount, int availableSeatsCount, int blockedSeatsCount, List<Integer> aisleColumns, List<Integer> aisleRows, List<SeatDto> seats) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.roomType = roomType;
        this.capacity = capacity;
        this.layoutTemplate = layoutTemplate;
        this.rowsCount = rowsCount;
        this.columnsCount = columnsCount;
        this.configuredSeatsCount = configuredSeatsCount;
        this.availableSeatsCount = availableSeatsCount;
        this.blockedSeatsCount = blockedSeatsCount;
        this.aisleColumns = aisleColumns != null ? aisleColumns : new ArrayList<>();
        this.aisleRows = aisleRows != null ? aisleRows : new ArrayList<>();
        this.seats = seats != null ? seats : new ArrayList<>();
    }

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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public LayoutTemplate getLayoutTemplate() {
        return layoutTemplate;
    }

    public void setLayoutTemplate(LayoutTemplate layoutTemplate) {
        this.layoutTemplate = layoutTemplate;
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

    public int getConfiguredSeatsCount() {
        return configuredSeatsCount;
    }

    public void setConfiguredSeatsCount(int configuredSeatsCount) {
        this.configuredSeatsCount = configuredSeatsCount;
    }

    public int getAvailableSeatsCount() {
        return availableSeatsCount;
    }

    public void setAvailableSeatsCount(int availableSeatsCount) {
        this.availableSeatsCount = availableSeatsCount;
    }

    public int getBlockedSeatsCount() {
        return blockedSeatsCount;
    }

    public void setBlockedSeatsCount(int blockedSeatsCount) {
        this.blockedSeatsCount = blockedSeatsCount;
    }

    public List<Integer> getAisleColumns() {
        return aisleColumns;
    }

    public void setAisleColumns(List<Integer> aisleColumns) {
        this.aisleColumns = aisleColumns;
    }

    public List<Integer> getAisleRows() {
        return aisleRows;
    }

    public void setAisleRows(List<Integer> aisleRows) {
        this.aisleRows = aisleRows;
    }

    public List<SeatDto> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDto> seats) {
        this.seats = seats;
    }
}
