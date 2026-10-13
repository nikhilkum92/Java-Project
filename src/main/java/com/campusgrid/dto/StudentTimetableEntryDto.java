package com.campusgrid.dto;

public class StudentTimetableEntryDto {

    private Long id;
    private String dayOfWeek;
    private String timeSlot;
    private String startTime;
    private String endTime;
    private String subjectCode;
    private String subjectName;
    private String roomCode;
    private String roomName;
    private String blockName;
    private String facultyName;
    private String status;

    public StudentTimetableEntryDto() {
    }

    public StudentTimetableEntryDto(Long id, String dayOfWeek, String timeSlot, String startTime, String endTime,
                                    String subjectCode, String subjectName, String roomCode, String roomName,
                                    String blockName, String facultyName, String status) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.blockName = blockName;
        this.facultyName = facultyName;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
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

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
