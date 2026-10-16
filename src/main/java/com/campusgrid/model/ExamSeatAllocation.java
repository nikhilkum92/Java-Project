package com.campusgrid.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_seat_allocations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_exam_student", columnNames = {"exam_id", "student_id"}),
        @UniqueConstraint(name = "uq_exam_physical_seat", columnNames = {"exam_id", "seat_id"})
}, indexes = {
        @Index(name = "idx_seat_alloc_exam_room", columnList = "exam_id, room_id"),
        @Index(name = "idx_seat_alloc_student", columnList = "student_id")
})
public class ExamSeatAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private ExamSession examSession;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private PhysicalSeat physicalSeat;

    @Column(name = "seat_number", nullable = false, length = 30)
    private String seatNumber;

    @Column(name = "`row_number`", nullable = false)
    private int rowNumber;

    @Column(name = "`column_number`", nullable = false)
    private int columnNumber;

    @Column(name = "row_label", length = 10)
    private String rowLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 30)
    private ExamAttendanceStatus attendanceStatus = ExamAttendanceStatus.ALLOCATED;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @Column(name = "allocated_at", nullable = false, updatable = false)
    private LocalDateTime allocatedAt;

    public ExamSeatAllocation() {
    }

    public ExamSeatAllocation(ExamSession examSession, Student student, Room room, PhysicalSeat physicalSeat) {
        this.examSession = examSession;
        this.student = student;
        this.room = room;
        this.physicalSeat = physicalSeat;
        if (physicalSeat != null) {
            this.seatNumber = physicalSeat.getSeatNumber();
            this.rowNumber = physicalSeat.getRowNumber();
            this.columnNumber = physicalSeat.getColumnNumber();
            this.rowLabel = physicalSeat.getRowLabel();
        }
        this.attendanceStatus = ExamAttendanceStatus.ALLOCATED;
    }

    @PrePersist
    protected void onCreate() {
        this.allocatedAt = LocalDateTime.now();
        if (this.physicalSeat != null) {
            if (this.seatNumber == null) this.seatNumber = this.physicalSeat.getSeatNumber();
            if (this.rowNumber == 0) this.rowNumber = this.physicalSeat.getRowNumber();
            if (this.columnNumber == 0) this.columnNumber = this.physicalSeat.getColumnNumber();
            if (this.rowLabel == null) this.rowLabel = this.physicalSeat.getRowLabel();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExamSession getExamSession() {
        return examSession;
    }

    public void setExamSession(ExamSession examSession) {
        this.examSession = examSession;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public PhysicalSeat getPhysicalSeat() {
        return physicalSeat;
    }

    public void setPhysicalSeat(PhysicalSeat physicalSeat) {
        this.physicalSeat = physicalSeat;
        if (physicalSeat != null) {
            this.seatNumber = physicalSeat.getSeatNumber();
            this.rowNumber = physicalSeat.getRowNumber();
            this.columnNumber = physicalSeat.getColumnNumber();
            this.rowLabel = physicalSeat.getRowLabel();
        }
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

    public ExamAttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(ExamAttendanceStatus attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }
}
