package com.campusgrid.dto;

public class ExamStatsDto {

    private long totalExams;
    private long scheduledExams;
    private long seatingAllocatedExams;
    private long completedExams;
    private long totalAllocatedSeats;
    private long activeExamHalls;
    private long totalStudentsSeated;

    public ExamStatsDto() {
    }

    public ExamStatsDto(long totalExams, long scheduledExams, long seatingAllocatedExams, long completedExams,
                        long totalAllocatedSeats, long activeExamHalls, long totalStudentsSeated) {
        this.totalExams = totalExams;
        this.scheduledExams = scheduledExams;
        this.seatingAllocatedExams = seatingAllocatedExams;
        this.completedExams = completedExams;
        this.totalAllocatedSeats = totalAllocatedSeats;
        this.activeExamHalls = activeExamHalls;
        this.totalStudentsSeated = totalStudentsSeated;
    }

    // Getters and Setters
    public long getTotalExams() {
        return totalExams;
    }

    public void setTotalExams(long totalExams) {
        this.totalExams = totalExams;
    }

    public long getScheduledExams() {
        return scheduledExams;
    }

    public void setScheduledExams(long scheduledExams) {
        this.scheduledExams = scheduledExams;
    }

    public long getSeatingAllocatedExams() {
        return seatingAllocatedExams;
    }

    public void setSeatingAllocatedExams(long seatingAllocatedExams) {
        this.seatingAllocatedExams = seatingAllocatedExams;
    }

    public long getCompletedExams() {
        return completedExams;
    }

    public void setCompletedExams(long completedExams) {
        this.completedExams = completedExams;
    }

    public long getTotalAllocatedSeats() {
        return totalAllocatedSeats;
    }

    public void setTotalAllocatedSeats(long totalAllocatedSeats) {
        this.totalAllocatedSeats = totalAllocatedSeats;
    }

    public long getActiveExamHalls() {
        return activeExamHalls;
    }

    public void setActiveExamHalls(long activeExamHalls) {
        this.activeExamHalls = activeExamHalls;
    }

    public long getTotalStudentsSeated() {
        return totalStudentsSeated;
    }

    public void setTotalStudentsSeated(long totalStudentsSeated) {
        this.totalStudentsSeated = totalStudentsSeated;
    }
}
