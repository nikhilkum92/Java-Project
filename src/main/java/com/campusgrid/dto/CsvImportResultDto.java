package com.campusgrid.dto;

import java.util.ArrayList;
import java.util.List;

public class CsvImportResultDto {

    private int totalSubmitted;
    private int successfullyImported;
    private int duplicatesCount;
    private int errorsCount;
    private List<RowIssue> issues = new ArrayList<>();

    public CsvImportResultDto() {}

    public CsvImportResultDto(int totalSubmitted, int successfullyImported, int duplicatesCount, int errorsCount) {
        this.totalSubmitted = totalSubmitted;
        this.successfullyImported = successfullyImported;
        this.duplicatesCount = duplicatesCount;
        this.errorsCount = errorsCount;
    }

    public static class RowIssue {
        private int rowNumber;
        private String identifier;
        private String reason;
        private String status; // DUPLICATE or ERROR

        public RowIssue() {}

        public RowIssue(int rowNumber, String identifier, String reason, String status) {
            this.rowNumber = rowNumber;
            this.identifier = identifier;
            this.reason = reason;
            this.status = status;
        }

        public int getRowNumber() { return rowNumber; }
        public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public void addIssue(int row, String id, String reason, String status) {
        this.issues.add(new RowIssue(row, id, reason, status));
    }

    public int getTotalSubmitted() { return totalSubmitted; }
    public void setTotalSubmitted(int totalSubmitted) { this.totalSubmitted = totalSubmitted; }

    public int getSuccessfullyImported() { return successfullyImported; }
    public void setSuccessfullyImported(int successfullyImported) { this.successfullyImported = successfullyImported; }

    public int getDuplicatesCount() { return duplicatesCount; }
    public void setDuplicatesCount(int duplicatesCount) { this.duplicatesCount = duplicatesCount; }

    public int getErrorsCount() { return errorsCount; }
    public void setErrorsCount(int errorsCount) { this.errorsCount = errorsCount; }

    public List<RowIssue> getIssues() { return issues; }
    public void setIssues(List<RowIssue> issues) { this.issues = issues; }
}
