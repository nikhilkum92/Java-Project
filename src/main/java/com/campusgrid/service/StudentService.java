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
    private com.campusgrid.repository.TimetableEntryRepository timetableEntryRepository;
    private ExamService examService;

    public StudentService(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setTimetableEntryRepository(com.campusgrid.repository.TimetableEntryRepository timetableEntryRepository) {
        this.timetableEntryRepository = timetableEntryRepository;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setExamService(ExamService examService) {
        this.examService = examService;
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
        if (timetableEntryRepository == null) {
            return Collections.emptyList();
        }

        List<com.campusgrid.model.TimetableEntry> entries = timetableEntryRepository
                .findByDepartmentAndSectionNameAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
                        student.getDepartment(), student.getSectionName()
                );

        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }

        List<StudentTimetableEntryDto> result = new ArrayList<>();
        for (var e : entries) {
            String roomCode = e.getRoom() != null ? e.getRoom().getRoomCode() : "TBA";
            String roomName = e.getRoom() != null ? e.getRoom().getRoomName() : "To Be Assigned";
            String blockName = (e.getRoom() != null && e.getRoom().getFloor() != null && e.getRoom().getFloor().getBlock() != null)
                    ? e.getRoom().getFloor().getBlock().getName() : "";
            String facultyName = (e.getFaculty() != null && e.getFaculty().getUser() != null)
                    ? e.getFaculty().getUser().getFullName() : "Faculty TBA";

            StudentTimetableEntryDto dto = new StudentTimetableEntryDto(
                    e.getId(),
                    e.getDayOfWeek().name(),
                    e.getStartTime() + " - " + e.getEndTime(),
                    e.getStartTime().toString(),
                    e.getEndTime().toString(),
                    e.getSubject() != null ? e.getSubject().getSubjectCode() : "",
                    e.getSubject() != null ? e.getSubject().getSubjectName() : "",
                    roomCode,
                    roomName,
                    blockName,
                    facultyName,
                    e.getStatus().name()
            );
            result.add(dto);
        }
        return result;
    }

    public List<StudentExamAllocationDto> getStudentExaminations(Long userId) {
        Student student = getStudentByUserId(userId);
        if (examService != null) {
            return examService.getStudentExaminations(userId);
        }
        return Collections.emptyList();
    }

    public StudentExamAllocationDto getStudentExamAllocation(Long userId, Long examId) {
        Student student = getStudentByUserId(userId);
        if (examService != null) {
            return examService.getStudentExamAllocation(userId, examId);
        }
        throw new IllegalStateException("Exam service is not available.");
    }
}
