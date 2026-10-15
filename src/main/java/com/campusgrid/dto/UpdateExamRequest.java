package com.campusgrid.dto;

import com.campusgrid.model.ExamStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class UpdateExamRequest {

    @NotBlank(message = "Exam title cannot be blank.")
    private String title;

    private Long subjectId;

    private String courseCode;

    private String courseName;

    @NotBlank(message = "Department cannot be blank.")
    private String department;

    @Min(1)
    @Max(8)
    private int semester;

    private String targetSection;

    @NotNull(message = "Exam date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate examDate;

    @NotNull(message = "Start time is required.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "End time is required.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @Min(1)
    private int maxMarks = 100;

    private String instructions;

    private ExamStatus status;

    public UpdateExamRequest() {
    }

    // Getters and Setters
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
}
