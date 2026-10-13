package com.campusgrid.dto;

import com.campusgrid.model.SeatStatus;
import com.campusgrid.model.SeatType;

public class SeatDto {
    private Long id;
    private String seatNumber;
    private int rowNumber;
    private int columnNumber;
    private String rowLabel;
    private SeatType seatType;
    private SeatStatus status;
    private boolean active;

    public SeatDto() {
    }

    public SeatDto(Long id, String seatNumber, int rowNumber, int columnNumber, String rowLabel, SeatType seatType, SeatStatus status, boolean active) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
        this.rowLabel = rowLabel;
        this.seatType = seatType;
        this.status = status;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
