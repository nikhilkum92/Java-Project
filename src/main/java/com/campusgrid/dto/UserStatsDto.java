package com.campusgrid.dto;

public class UserStatsDto {

    private long totalStudents;
    private long activeStudents;
    private long totalFaculty;
    private long activeFaculty;
    private long totalUsers;
    private long activeUsers;
    private long totalDepartments;
    private long totalSections;

    public UserStatsDto() {}

    public UserStatsDto(long totalStudents, long activeStudents, long totalFaculty, long activeFaculty,
                        long totalUsers, long activeUsers, long totalDepartments, long totalSections) {
        this.totalStudents = totalStudents;
        this.activeStudents = activeStudents;
        this.totalFaculty = totalFaculty;
        this.activeFaculty = activeFaculty;
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.totalDepartments = totalDepartments;
        this.totalSections = totalSections;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getActiveStudents() {
        return activeStudents;
    }

    public void setActiveStudents(long activeStudents) {
        this.activeStudents = activeStudents;
    }

    public long getTotalFaculty() {
        return totalFaculty;
    }

    public void setTotalFaculty(long totalFaculty) {
        this.totalFaculty = totalFaculty;
    }

    public long getActiveFaculty() {
        return activeFaculty;
    }

    public void setActiveFaculty(long activeFaculty) {
        this.activeFaculty = activeFaculty;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public long getTotalSections() {
        return totalSections;
    }

    public void setTotalSections(long totalSections) {
        this.totalSections = totalSections;
    }
}
