package com.campusgrid.dto;

import com.campusgrid.model.ExamAttendanceStatus;
import jakarta.validation.constraints.NotNull;

public class MarkExamAttendanceRequest {

    @NotNull(message = "Attendance status is required.")
    private ExamAttendanceStatus attendanceStatus;

    private String remarks;

    public MarkExamAttendanceRequest() {
    }

    public MarkExamAttendanceRequest(ExamAttendanceStatus attendanceStatus, String remarks) {
        this.attendanceStatus = attendanceStatus;
        this.remarks = remarks;
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
