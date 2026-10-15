package com.campusgrid.dto;

import com.campusgrid.model.ExamAttendanceStatus;

public class ExamRosterStudentDto {

    private Long allocationId;
    private String seatNumber;
    private String rowLabel;
    private int rowNumber;
    private int columnNumber;
    private Long studentId;
    private String studentRollNumber;
    private String studentName;
    private String studentDepartment;
    private String studentSection;
    private ExamAttendanceStatus attendanceStatus;
    private String remarks;

    public ExamRosterStudentDto() {
    }

    public ExamRosterStudentDto(Long allocationId, String seatNumber, String rowLabel, int rowNumber,
                                int columnNumber, Long studentId, String studentRollNumber, String studentName,
                                String studentDepartment, String studentSection,
                                ExamAttendanceStatus attendanceStatus, String remarks) {
        this.allocationId = allocationId;
        this.seatNumber = seatNumber;
        this.rowLabel = rowLabel;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
        this.studentId = studentId;
        this.studentRollNumber = studentRollNumber;
        this.studentName = studentName;
        this.studentDepartment = studentDepartment;
        this.studentSection = studentSection;
        this.attendanceStatus = attendanceStatus;
        this.remarks = remarks;
    }

    // Getters and Setters
    public Long getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Long allocationId) {
        this.allocationId = allocationId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
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

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
