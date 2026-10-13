package com.campusgrid.dto;

public class StudentExamAllocationDto {

    private Long id;
    private String examName;
    private String courseCode;
    private String courseName;
    private String examDate;
    private String startTime;
    private String endTime;
    private String roomCode;
    private String roomName;
    private String blockName;
    private String seatNumber;
    private String status; // "ALLOCATED", "PENDING", "COMPLETED"
    private String instructions;

    public StudentExamAllocationDto() {
    }

    public StudentExamAllocationDto(Long id, String examName, String courseCode, String courseName, String examDate,
                                    String startTime, String endTime, String roomCode, String roomName,
                                    String blockName, String seatNumber, String status, String instructions) {
        this.id = id;
        this.examName = examName;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.examDate = examDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.blockName = blockName;
        this.seatNumber = seatNumber;
        this.status = status;
        this.instructions = instructions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
