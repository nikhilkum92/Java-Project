package com.campusgrid.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Entity
@Table(name = "timetable_entries", indexes = {
        @Index(name = "idx_tt_room_day", columnList = "room_id, day_of_week"),
        @Index(name = "idx_tt_faculty_day", columnList = "faculty_id, day_of_week"),
        @Index(name = "idx_tt_group_day", columnList = "department, section_name, day_of_week"),
        @Index(name = "idx_tt_status", columnList = "status"),
        @Index(name = "idx_tt_schedule_date", columnList = "schedule_date"),
        @Index(name = "idx_tt_section", columnList = "section_name")
})
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = true)
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = true)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = true)
    private Room room;

    @Column(name = "academic_year", nullable = false)
    private int academicYear = 2;

    @Column(name = "semester", nullable = false)
    private int semester = 3;

    @Column(name = "department", length = 100)
    private String department = "Computer Science";

    @Column(name = "section_name", nullable = false, length = 50)
    private String sectionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek = DayOfWeek.MONDAY;

    @Column(name = "schedule_date")
    private LocalDate scheduleDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime = LocalTime.of(9, 0);

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime = LocalTime.of(10, 0);

    @Enumerated(EnumType.STRING)
    @Column(name = "class_type", nullable = false, length = 30)
    private ClassType classType = ClassType.LECTURE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TimetableStatus status = TimetableStatus.SCHEDULED;

    @Column(name = "expected_capacity", nullable = false)
    private int expectedCapacity = 40;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "remarks", length = 500)
    private String remarks;

    // Transient or fallback string fields for non-relational compatibility
    @Transient
    private String subjectCodeFallback;
    @Transient
    private String subjectNameFallback;
    @Transient
    private String facultyNameFallback;
    @Transient
    private String blockNameFallback;
    @Transient
    private String roomCodeFallback;
    @Transient
    private String roomNameFallback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TimetableEntry() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TimetableEntry(Subject subject, Faculty faculty, Room room, String department, String sectionName,
                          int academicYear, int semester, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                          ClassType classType, int expectedCapacity) {
        this.subject = subject;
        this.faculty = faculty;
        this.room = room;
        this.department = department;
        this.sectionName = sectionName;
        this.academicYear = academicYear;
        this.semester = semester;
        this.dayOfWeek = dayOfWeek != null ? dayOfWeek : DayOfWeek.MONDAY;
        this.startTime = startTime != null ? startTime : LocalTime.of(9, 0);
        this.endTime = endTime != null ? endTime : LocalTime.of(10, 0);
        this.classType = classType != null ? classType : ClassType.LECTURE;
        this.expectedCapacity = expectedCapacity;
        this.status = TimetableStatus.SCHEDULED;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TimetableEntry(String sectionName, String dayOfWeek, String startTime, String endTime,
                          String subjectCode, String subjectName, String facultyName,
                          String blockName, String roomCode, String roomName) {
        this.sectionName = sectionName;
        this.dayOfWeek = parseDayOfWeekSafely(dayOfWeek);
        this.startTime = parseTimeSafely(startTime, LocalTime.of(9, 0));
        this.endTime = parseTimeSafely(endTime, LocalTime.of(10, 0));
        this.subjectCodeFallback = subjectCode;
        this.subjectNameFallback = subjectName;
        this.facultyNameFallback = facultyName;
        this.blockNameFallback = blockName;
        this.roomCodeFallback = roomCode;
        this.roomNameFallback = roomName;
        this.status = TimetableStatus.SCHEDULED;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    private static DayOfWeek parseDayOfWeekSafely(String day) {
        if (day == null || day.trim().isEmpty()) {
            return DayOfWeek.MONDAY;
        }
        try {
            return DayOfWeek.valueOf(day.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return DayOfWeek.MONDAY;
        }
    }

    private static LocalTime parseTimeSafely(String timeStr, LocalTime fallback) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return fallback;
        }
        String clean = timeStr.trim();
        try {
            if (clean.toUpperCase(Locale.ROOT).contains("AM") || clean.toUpperCase(Locale.ROOT).contains("PM")) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
                return LocalTime.parse(clean, dtf);
            }
            return LocalTime.parse(clean);
        } catch (Exception ex) {
            try {
                DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
                return LocalTime.parse(clean, dtf2);
            } catch (Exception ex2) {
                return fallback;
            }
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
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

    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    public TimetableStatus getStatus() {
        return status;
    }

    public void setStatus(TimetableStatus status) {
        this.status = status;
    }

    public int getExpectedCapacity() {
        return expectedCapacity;
    }

    public void setExpectedCapacity(int expectedCapacity) {
        this.expectedCapacity = expectedCapacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper getters for backward compatibility and DTO projections
    public String getSubjectCode() {
        if (subject != null && subject.getSubjectCode() != null) {
            return subject.getSubjectCode();
        }
        return subjectCodeFallback != null ? subjectCodeFallback : "";
    }

    public void setSubjectCode(String code) {
        this.subjectCodeFallback = code;
    }

    public String getSubjectName() {
        if (subject != null && subject.getSubjectName() != null) {
            return subject.getSubjectName();
        }
        return subjectNameFallback != null ? subjectNameFallback : "";
    }

    public void setSubjectName(String name) {
        this.subjectNameFallback = name;
    }

    public String getFacultyName() {
        if (faculty != null && faculty.getUser() != null) {
            return faculty.getUser().getFullName();
        }
        return facultyNameFallback != null ? facultyNameFallback : "Faculty TBA";
    }

    public void setFacultyName(String name) {
        this.facultyNameFallback = name;
    }

    public String getRoomCode() {
        if (room != null && room.getRoomCode() != null) {
            return room.getRoomCode();
        }
        return roomCodeFallback != null ? roomCodeFallback : "TBA";
    }

    public void setRoomCode(String code) {
        this.roomCodeFallback = code;
    }

    public String getRoomName() {
        if (room != null && room.getRoomName() != null) {
            return room.getRoomName();
        }
        return roomNameFallback != null ? roomNameFallback : "To Be Assigned";
    }

    public void setRoomName(String name) {
        this.roomNameFallback = name;
    }

    public String getBlockName() {
        if (room != null && room.getFloor() != null && room.getFloor().getBlock() != null) {
            return room.getFloor().getBlock().getName();
        }
        return blockNameFallback != null ? blockNameFallback : "";
    }

    public void setBlockName(String name) {
        this.blockNameFallback = name;
    }

    public String getTimeSlot() {
        if (startTime != null && endTime != null) {
            return startTime + " - " + endTime;
        }
        return "";
    }
}
