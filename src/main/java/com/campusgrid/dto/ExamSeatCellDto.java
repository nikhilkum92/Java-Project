package com.campusgrid.dto;

import com.campusgrid.model.ExamAttendanceStatus;
import com.campusgrid.model.SeatStatus;

public class ExamSeatCellDto {

    private Long seatId;
    private String seatNumber;
    private int rowNumber;
    private int columnNumber;
    private String rowLabel;
    private SeatStatus physicalSeatStatus = SeatStatus.AVAILABLE;
    private boolean walkway;
    private boolean allocated;
    private Long studentId;
    private String studentName;
    private String studentRollNumber;
    private String studentDepartment;
    private String studentSection;
    private ExamAttendanceStatus attendanceStatus;
    private Long allocationId;

    public ExamSeatCellDto() {
    }

    public ExamSeatCellDto(Long seatId, String seatNumber, int rowNumber, int columnNumber, String rowLabel, boolean walkway) {
        this(seatId, seatNumber, rowNumber, columnNumber, rowLabel, SeatStatus.AVAILABLE, walkway);
    }

    public ExamSeatCellDto(Long seatId, String seatNumber, int rowNumber, int columnNumber, String rowLabel, SeatStatus physicalSeatStatus, boolean walkway) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
        this.rowLabel = rowLabel;
        this.physicalSeatStatus = physicalSeatStatus != null ? physicalSeatStatus : SeatStatus.AVAILABLE;
        this.walkway = walkway;
        this.allocated = false;
    }

    // Getters and Setters
    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
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

    public SeatStatus getPhysicalSeatStatus() {
        return physicalSeatStatus;
    }

    public void setPhysicalSeatStatus(SeatStatus physicalSeatStatus) {
        this.physicalSeatStatus = physicalSeatStatus;
    }

    public boolean isWalkway() {
        return walkway;
    }

    public void setWalkway(boolean walkway) {
        this.walkway = walkway;
    }

    public boolean isAllocated() {
        return allocated;
    }

    public void setAllocated(boolean allocated) {
        this.allocated = allocated;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(String studentDepartment) {
        this.studentDepartment = studentDepartment;
    }

    public String getStudentSection() {
        return studentSection;
    }

    public void setStudentSection(String studentSection) {
        this.studentSection = studentSection;
    }

    public ExamAttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(ExamAttendanceStatus attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public Long getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Long allocationId) {
        this.allocationId = allocationId;
    }
}
