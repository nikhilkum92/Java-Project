package com.campusgrid.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExamSeatingGridDto {

    private Long examId;
    private String examTitle;
    private String examCode;
    private LocalDate examDate;
    private String timeSlot;
    private Long roomId;
    private String roomCode;
    private String roomName;
    private String blockName;
    private int rowsCount;
    private int columnsCount;
    private int totalSeats;
    private int allocatedSeats;
    private int availableSeats;
    private String invigilatorName;
    private List<ExamSeatCellDto> seats = new ArrayList<>();

    public ExamSeatingGridDto() {
    }

    // Getters and Setters
    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
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

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
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

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAllocatedSeats() {
        return allocatedSeats;
    }

    public void setAllocatedSeats(int allocatedSeats) {
        this.allocatedSeats = allocatedSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getInvigilatorName() {
        return invigilatorName;
    }

    public void setInvigilatorName(String invigilatorName) {
        this.invigilatorName = invigilatorName;
    }

    public List<ExamSeatCellDto> getSeats() {
        return seats;
    }

    public void setSeats(List<ExamSeatCellDto> seats) {
        this.seats = seats;
    }
}
