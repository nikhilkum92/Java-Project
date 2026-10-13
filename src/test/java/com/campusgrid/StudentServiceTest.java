package com.campusgrid;

import com.campusgrid.dto.StudentDashboardDto;
import com.campusgrid.dto.StudentExamAllocationDto;
import com.campusgrid.dto.StudentTimetableEntryDto;
import com.campusgrid.dto.UserProfileDto;
import com.campusgrid.model.Role;
import com.campusgrid.model.Student;
import com.campusgrid.model.User;
import com.campusgrid.repository.StudentRepository;
import com.campusgrid.repository.UserRepository;
import com.campusgrid.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private User studentUser;
    private Student studentProfile;

    @BeforeEach
    void setUp() {
        studentUser = new User();
        studentUser.setId(10L);
        studentUser.setUid("USR-STUDENT-10");
        studentUser.setUsername("john.doe");
        studentUser.setEmail("john.doe@campusgrid.edu");
        studentUser.setFullName("John Doe");
        studentUser.setRole(Role.STUDENT);
        studentUser.setActive(true);

        studentProfile = new Student();
        studentProfile.setId(5L);
        studentProfile.setUser(studentUser);
        studentProfile.setRollNumber("STU-2024-001");
        studentProfile.setDepartment("Computer Science");
        studentProfile.setSectionName("CS-3A");
        studentProfile.setYear(3);
        studentProfile.setCurrentSemester(6);
    }

    @Test
    @DisplayName("Should successfully retrieve student profile with complete academic details")
    void testGetStudentProfile_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByUserId(10L)).thenReturn(Optional.of(studentProfile));

        UserProfileDto profile = studentService.getStudentProfile(10L);

        assertNotNull(profile);
        assertEquals(10L, profile.getId());
        assertEquals("USR-STUDENT-10", profile.getUid());
        assertEquals("john.doe", profile.getUsername());
        assertEquals("john.doe@campusgrid.edu", profile.getEmail());
        assertEquals("John Doe", profile.getFullName());
        assertEquals(Role.STUDENT, profile.getRole());
        assertTrue(profile.isActive());
        assertEquals("STU-2024-001", profile.getSpecificId());
        assertEquals("Computer Science", profile.getDepartment());
        assertEquals("CS-3A", profile.getSection());
        assertEquals(3, profile.getYear());
        assertEquals(6, profile.getSemester());

        verify(userRepository).findById(10L);
        verify(studentRepository).findByUserId(10L);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user record not found")
    void testGetStudentProfile_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                studentService.getStudentProfile(99L)
        );

        assertTrue(ex.getMessage().contains("User not found with ID: 99"));
        verify(studentRepository, never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when academic student profile record not found")
    void testGetStudentProfile_StudentProfileNotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByUserId(10L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                studentService.getStudentProfile(10L)
        );

        assertTrue(ex.getMessage().contains("Student academic profile not found for user ID: 10"));
    }

    @Test
    @DisplayName("Should build complete student dashboard with verified profile and schedule summary")
    void testGetStudentDashboard_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByUserId(10L)).thenReturn(Optional.of(studentProfile));

        StudentDashboardDto dashboard = studentService.getStudentDashboard(10L);

        assertNotNull(dashboard);
        assertNotNull(dashboard.getProfile());
        assertEquals("STU-2024-001", dashboard.getRollNumber());
        assertEquals("Computer Science", dashboard.getDepartment());
        assertEquals("CS-3A", dashboard.getAcademicGroup());
        assertEquals(3, dashboard.getAcademicYear());
        assertEquals(6, dashboard.getCurrentSemester());
        assertTrue(dashboard.isActiveStatus());

        // Schedule and exam status
        assertNotNull(dashboard.getTodaySchedule());
        assertEquals(0, dashboard.getTodayClassesCount());
        assertFalse(dashboard.isTimetableAvailable());

        assertNotNull(dashboard.getUpcomingExams());
        assertEquals(0, dashboard.getUpcomingExamsCount());
        assertFalse(dashboard.isExaminationsAvailable());
    }

    @Test
    @DisplayName("Should retrieve student timetable safely without fabricated records")
    void testGetStudentTimetable() {
        when(studentRepository.findByUserId(10L)).thenReturn(Optional.of(studentProfile));

        List<StudentTimetableEntryDto> timetable = studentService.getStudentTimetable(10L);

        assertNotNull(timetable);
        assertTrue(timetable.isEmpty(), "Timetable must return an empty list when Module 3 timetable has no records");
        verify(studentRepository).findByUserId(10L);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when getting timetable for non-existent student")
    void testGetStudentTimetable_StudentNotFound() {
        when(studentRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                studentService.getStudentTimetable(99L)
        );
    }

    @Test
    @DisplayName("Should retrieve student examinations safely without fabricated records")
    void testGetStudentExaminations() {
        when(studentRepository.findByUserId(10L)).thenReturn(Optional.of(studentProfile));

        List<StudentExamAllocationDto> exams = studentService.getStudentExaminations(10L);

        assertNotNull(exams);
        assertTrue(exams.isEmpty(), "Examinations must return an empty list when Module 4 exams has no records");
        verify(studentRepository).findByUserId(10L);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when getting examinations for non-existent student")
    void testGetStudentExaminations_StudentNotFound() {
        when(studentRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                studentService.getStudentExaminations(99L)
        );
    }
}