package com.campusgrid.dto;

import com.campusgrid.model.ExamStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ExamDetailDto {

    private Long id;
    private String examCode;
    private String title;
    private Long subjectId;
    private String courseCode;
    private String courseName;
    private String department;
    private int semester;
    private String targetSection;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int durationMinutes;
    private int maxMarks;
    private String instructions;
    private ExamStatus status;
    private int allocatedRoomsCount;
    private int allocatedStudentsCount;
    private List<ExamRoomAllocationDto> rooms = new ArrayList<>();

    public ExamDetailDto() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getTargetSection() {
        return targetSection;
    }

    public void setTargetSection(String targetSection) {
        this.targetSection = targetSection;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(int maxMarks) {
        this.maxMarks = maxMarks;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
    }

    public int getAllocatedRoomsCount() {
        return allocatedRoomsCount;
    }

    public void setAllocatedRoomsCount(int allocatedRoomsCount) {
        this.allocatedRoomsCount = allocatedRoomsCount;
    }

    public int getAllocatedStudentsCount() {
        return allocatedStudentsCount;
    }

    public void setAllocatedStudentsCount(int allocatedStudentsCount) {
        this.allocatedStudentsCount = allocatedStudentsCount;
    }

    public List<ExamRoomAllocationDto> getRooms() {
        return rooms;
    }

    public void setRooms(List<ExamRoomAllocationDto> rooms) {
        this.rooms = rooms;
    }
}
