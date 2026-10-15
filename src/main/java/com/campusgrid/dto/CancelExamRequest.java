package com.campusgrid.dto;

public class CancelExamRequest {

    private String reason;

    public CancelExamRequest() {
    }

    public CancelExamRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
