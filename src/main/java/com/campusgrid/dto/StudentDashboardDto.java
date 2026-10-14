package com.campusgrid.dto;

import java.util.ArrayList;
import java.util.List;

public class StudentDashboardDto {

    private UserProfileDto profile;
    private String rollNumber;
    private String department;
    private String academicGroup;
    private int academicYear;
    private int currentSemester;
    private boolean activeStatus;

    private int todayClassesCount;
    private int upcomingExamsCount;
    private int totalEnrolledSubjects;
    private boolean timetableAvailable;
    private boolean examinationsAvailable;

    private List<StudentTimetableEntryDto> todaySchedule = new ArrayList<>();
    private List<StudentExamAllocationDto> upcomingExams = new ArrayList<>();

    public StudentDashboardDto() {
    }

    public UserProfileDto getProfile() {
        return profile;
    }

    public void setProfile(UserProfileDto profile) {
        this.profile = profile;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAcademicGroup() {
        return academicGroup;
    }

    public void setAcademicGroup(String academicGroup) {
        this.academicGroup = academicGroup;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public int getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(int currentSemester) {
        this.currentSemester = currentSemester;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public int getTodayClassesCount() {
        return todayClassesCount;
    }

    public void setTodayClassesCount(int todayClassesCount) {
        this.todayClassesCount = todayClassesCount;
    }

    public int getUpcomingExamsCount() {
        return upcomingExamsCount;
    }

    public void setUpcomingExamsCount(int upcomingExamsCount) {
        this.upcomingExamsCount = upcomingExamsCount;
    }

    public int getTotalEnrolledSubjects() {
        return totalEnrolledSubjects;
    }

    public void setTotalEnrolledSubjects(int totalEnrolledSubjects) {
        this.totalEnrolledSubjects = totalEnrolledSubjects;
    }

    public boolean isTimetableAvailable() {
        return timetableAvailable;
    }

    public void setTimetableAvailable(boolean timetableAvailable) {
        this.timetableAvailable = timetableAvailable;
    }

    public boolean isExaminationsAvailable() {
        return examinationsAvailable;
    }

    public void setExaminationsAvailable(boolean examinationsAvailable) {
        this.examinationsAvailable = examinationsAvailable;
    }

    public List<StudentTimetableEntryDto> getTodaySchedule() {
        return todaySchedule;
    }

    public void setTodaySchedule(List<StudentTimetableEntryDto> todaySchedule) {
        this.todaySchedule = todaySchedule;
    }

    public List<StudentExamAllocationDto> getUpcomingExams() {
        return upcomingExams;
    }

    public void setUpcomingExams(List<StudentExamAllocationDto> upcomingExams) {
        this.upcomingExams = upcomingExams;
    }
}
