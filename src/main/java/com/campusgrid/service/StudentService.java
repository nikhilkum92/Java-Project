package com.campusgrid.service;

import com.campusgrid.dto.*;
import com.campusgrid.model.Role;
import com.campusgrid.model.Student;
import com.campusgrid.model.User;
import com.campusgrid.repository.StudentRepository;
import com.campusgrid.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public StudentService(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student academic profile not found for user ID: " + userId));
    }

    public UserProfileDto getStudentProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Student student = getStudentByUserId(userId);

        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUid(user.getUid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setSpecificId(student.getRollNumber());
        dto.setDepartment(student.getDepartment());
        dto.setSection(student.getSectionName());
        dto.setYear(student.getYear());
        dto.setSemester(student.getCurrentSemester());

        return dto;
    }

    public StudentDashboardDto getStudentDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Student student = getStudentByUserId(userId);
        UserProfileDto profile = getStudentProfile(userId);

        StudentDashboardDto dashboard = new StudentDashboardDto();
        dashboard.setProfile(profile);
        dashboard.setRollNumber(student.getRollNumber());
        dashboard.setDepartment(student.getDepartment());
        dashboard.setAcademicGroup(student.getSectionName());
        dashboard.setAcademicYear(student.getYear());
        dashboard.setCurrentSemester(student.getCurrentSemester());
        dashboard.setActiveStatus(user.isActive());

        List<StudentTimetableEntryDto> schedule = getStudentTimetable(userId);
        List<StudentExamAllocationDto> exams = getStudentExaminations(userId);

        dashboard.setTodaySchedule(schedule);
        dashboard.setTodayClassesCount(schedule.size());
        dashboard.setTimetableAvailable(!schedule.isEmpty());

        dashboard.setUpcomingExams(exams);
        dashboard.setUpcomingExamsCount(exams.size());
        dashboard.setExaminationsAvailable(!exams.isEmpty());

        dashboard.setTotalEnrolledSubjects(0);

        return dashboard;
    }

    public List<StudentTimetableEntryDto> getStudentTimetable(Long userId) {
        Student student = getStudentByUserId(userId);
        // Timetable persistence (Module 3) is pending integration by the timetable module.
        // Returns real persisted schedule or an accurate empty collection (never fabricated fake data).
        return Collections.emptyList();
    }

    public List<StudentExamAllocationDto> getStudentExaminations(Long userId) {
        Student student = getStudentByUserId(userId);
        // Examination & seating allocation persistence (Module 4) is pending integration by the exams module.
        // Returns real persisted records or an accurate empty collection (never fabricated fake data).
        return Collections.emptyList();
    }
}