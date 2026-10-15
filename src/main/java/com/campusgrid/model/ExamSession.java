package com.campusgrid.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "exam_sessions", indexes = {
        @Index(name = "idx_exam_dept_sem", columnList = "department, semester"),
        @Index(name = "idx_exam_date", columnList = "exam_date")
})
public class ExamSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_code", nullable = false, unique = true, length = 50)
    private String examCode;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "course_code", length = 50)
    private String courseCode;

    @Column(name = "course_name", length = 150)
    private String courseName;

    @Column(name = "department", nullable = false, length = 80)
    private String department;

    @Column(name = "semester", nullable = false)
    private int semester;

    @Column(name = "target_section", length = 50)
    private String targetSection = "ALL";

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "max_marks", nullable = false)
    private int maxMarks = 100;

    @Column(name = "instructions", length = 1000)
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ExamStatus status = ExamStatus.SCHEDULED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ExamSession() {
    }

    public ExamSession(String examCode, String title, Subject subject, String department, int semester,
                       String targetSection, LocalDate examDate, LocalTime startTime, LocalTime endTime,
                       int maxMarks, String instructions) {
        this.examCode = examCode;
        this.title = title;
        this.subject = subject;
        if (subject != null) {
            this.courseCode = subject.getSubjectCode();
            this.courseName = subject.getSubjectName();
        }
        this.department = department;
        this.semester = semester;
        this.targetSection = targetSection != null ? targetSection : "ALL";
        this.examDate = examDate;
        this.startTime = startTime;
        this.endTime = endTime;
        if (startTime != null && endTime != null) {
            this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
        this.maxMarks = maxMarks;
        this.instructions = instructions;
        this.status = ExamStatus.SCHEDULED;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.subject != null) {
            if (this.courseCode == null || this.courseCode.isBlank()) {
                this.courseCode = this.subject.getSubjectCode();
            }
            if (this.courseName == null || this.courseName.isBlank()) {
                this.courseName = this.subject.getSubjectName();
            }
        }
        if (this.durationMinutes <= 0 && this.startTime != null && this.endTime != null) {
            this.durationMinutes = (int) java.time.Duration.between(this.startTime, this.endTime).toMinutes();
        }
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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
        if (subject != null) {
            this.courseCode = subject.getSubjectCode();
            this.courseName = subject.getSubjectName();
        }
    }

    public String getCourseCode() {
        return courseCode != null ? courseCode : (subject != null ? subject.getSubjectCode() : "");
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName != null ? courseName : (subject != null ? subject.getSubjectName() : "");
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
