package com.campusgrid.service;

import com.campusgrid.dto.*;
import com.campusgrid.model.*;
import com.campusgrid.repository.BlockRepository;
import com.campusgrid.repository.ExamScheduleRepository;
import com.campusgrid.repository.StudentRepository;
import com.campusgrid.repository.TimetableRepository;
import com.campusgrid.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TimetableRepository timetableRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final BlockRepository blockRepository;

    public StudentService(UserRepository userRepository, StudentRepository studentRepository) {
        this(userRepository, studentRepository, null, null, null);
    }

    @Autowired
    public StudentService(UserRepository userRepository,
                          StudentRepository studentRepository,
                          TimetableRepository timetableRepository,
                          ExamScheduleRepository examScheduleRepository,
                          BlockRepository blockRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.timetableRepository = timetableRepository;
        this.examScheduleRepository = examScheduleRepository;
        this.blockRepository = blockRepository;
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

        dashboard.setTotalEnrolledSubjects(schedule.size());

        return dashboard;
    }

    // ==========================================
    // Section-Wise Timetable Methods (Feature 2)
    // ==========================================

    public List<StudentTimetableEntryDto> getStudentTimetable(Long userId) {
        Student student = getStudentByUserId(userId);
        if (timetableRepository == null || student.getSectionName() == null || student.getSectionName().trim().isEmpty()) {
            return Collections.emptyList();
        }
        return getSectionTimetable(student.getSectionName().trim());
    }

    public List<StudentTimetableEntryDto> getSectionTimetable(String sectionName) {
        if (timetableRepository == null || sectionName == null || sectionName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<TimetableEntry> entries = timetableRepository.findBySectionNameIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(sectionName.trim());
        return entries.stream().map(this::mapToTimetableDto).collect(Collectors.toList());
    }

    public List<String> getAvailableTimetableSections() {
        if (timetableRepository == null) {
            return Collections.emptyList();
        }
        List<String> sections = timetableRepository.findDistinctSections();
        return sections != null ? sections : Collections.emptyList();
    }

    private StudentTimetableEntryDto mapToTimetableDto(TimetableEntry t) {
        return new StudentTimetableEntryDto(
                t.getId(),
                t.getSectionName(),
                t.getDayOfWeek(),
                t.getTimeSlot(),
                t.getStartTime(),
                t.getEndTime(),
                t.getSubjectCode(),
                t.getSubjectName(),
                t.getRoomCode(),
                t.getRoomName(),
                t.getBlockName(),
                t.getFacultyName(),
                t.getStatus()
        );
    }

    // ==========================================
    // Examination Venue & Schedule Methods (Features 3 & 4)
    // ==========================================

    public List<StudentExamAllocationDto> getStudentExaminations(Long userId) {
        Student student = getStudentByUserId(userId);
        if (examScheduleRepository == null || student.getSectionName() == null || student.getSectionName().trim().isEmpty()) {
            return Collections.emptyList();
        }
        return getSectionExaminations(student.getSectionName().trim());
    }

    public List<StudentExamAllocationDto> getSectionExaminations(String sectionName) {
        if (examScheduleRepository == null || sectionName == null || sectionName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<ExamSchedule> exams = examScheduleRepository.findBySectionNameIgnoreCaseOrderByExamDateAscStartTimeAsc(sectionName.trim());
        return exams.stream().map(this::mapToExamDto).collect(Collectors.toList());
    }

    public List<String> getAvailableExamSections() {
        if (examScheduleRepository == null) {
            return Collections.emptyList();
        }
        List<String> sections = examScheduleRepository.findDistinctSections();
        return sections != null ? sections : Collections.emptyList();
    }

    private StudentExamAllocationDto mapToExamDto(ExamSchedule e) {
        return new StudentExamAllocationDto(
                e.getId(),
                e.getExamName(),
                e.getCourseCode(),
                e.getCourseName(),
                e.getExamDate(),
                e.getStartTime(),
                e.getEndTime(),
                e.getRoomCode(),
                e.getRoomName(),
                e.getBlockName(),
                e.getSeatNumber(),
                e.getStatus(),
                e.getInstructions(),
                e.getSectionName(),
                e.getBlockId()
        );
    }

    // ==========================================
    // Initial Academic Data Seeding
    // ==========================================

    @Transactional
    public void seedInitialAcademicData() {
        if (timetableRepository != null && timetableRepository.count() == 0) {
            System.out.println("Seeding Section-wise Academic Timetables (CSE-A, CSE-B, AI-DS-A, AI-DS-B)...");
            List<TimetableEntry> ttList = new ArrayList<>();

            // 1. CSE-A
            ttList.add(new TimetableEntry("CSE-A", "Monday", "09:00 AM", "10:00 AM", "CS301", "Data Mining & Warehousing", "Dr. Meera Faculty", "Block A — Computer Science", "A101", "Lecture Hall A101"));
            ttList.add(new TimetableEntry("CSE-A", "Monday", "10:15 AM", "11:15 AM", "CS302", "Distributed Systems", "Prof. Arjun Faculty", "Block A — Computer Science", "A102", "Interactive Classroom A102"));
            ttList.add(new TimetableEntry("CSE-A", "Tuesday", "09:00 AM", "11:00 AM", "CS303L", "Advanced Software Lab", "Prof. Arjun Faculty", "Block A — Computer Science", "A104", "Software Development Lab A104"));
            ttList.add(new TimetableEntry("CSE-A", "Wednesday", "11:30 AM", "12:30 PM", "CS304", "Compiler Design", "Dr. Meera Faculty", "Block A — Computer Science", "A101", "Lecture Hall A101"));
            ttList.add(new TimetableEntry("CSE-A", "Thursday", "02:00 PM", "03:00 PM", "CS305", "Web Technologies & Frameworks", "Dr. Rajesh Kumar", "Block A — Computer Science", "A103", "Classroom A103"));
            ttList.add(new TimetableEntry("CSE-A", "Friday", "10:00 AM", "11:00 AM", "CS306", "Computer Networks", "Dr. Meera Faculty", "Block A — Computer Science", "A101", "Lecture Hall A101"));

            // 2. CSE-B
            ttList.add(new TimetableEntry("CSE-B", "Monday", "10:15 AM", "11:15 AM", "CS311", "Cloud Computing & DevOps", "Dr. Vikram Joshi", "Block A — Computer Science", "A102", "Interactive Classroom A102"));
            ttList.add(new TimetableEntry("CSE-B", "Tuesday", "09:00 AM", "10:00 AM", "CS312", "Software Engineering & Testing", "Dr. Sunita Rao", "Block A — Computer Science", "A103", "Classroom A103"));
            ttList.add(new TimetableEntry("CSE-B", "Wednesday", "02:00 PM", "04:00 PM", "CS313L", "Database Systems Lab", "Dr. Vikram Joshi", "Block A — Computer Science", "A104", "Software Development Lab A104"));
            ttList.add(new TimetableEntry("CSE-B", "Thursday", "10:00 AM", "11:00 AM", "CS314", "Cryptography & Network Security", "Dr. Sunita Rao", "Block A — Computer Science", "A102", "Interactive Classroom A102"));
            ttList.add(new TimetableEntry("CSE-B", "Friday", "09:00 AM", "10:00 AM", "CS315", "Artificial Intelligence Basics", "Dr. Sunita Rao", "Block A — Computer Science", "A103", "Classroom A103"));

            // 3. AI-DS-A
            ttList.add(new TimetableEntry("AI-DS-A", "Monday", "09:00 AM", "10:00 AM", "AI301", "Machine Learning Foundations", "Dr. Ananya Sen", "Block B — Artificial Intelligence & Data Science", "B102", "Classroom B102"));
            ttList.add(new TimetableEntry("AI-DS-A", "Monday", "11:30 AM", "01:30 PM", "AI302L", "AI & Robotics Practical Lab", "Dr. Ananya Sen", "Block B — Artificial Intelligence & Data Science", "B101", "AI & Robotics Lab B101"));
            ttList.add(new TimetableEntry("AI-DS-A", "Tuesday", "10:15 AM", "11:15 AM", "AI303", "Deep Learning Architectures", "Prof. Suresh Iyer", "Block B — Artificial Intelligence & Data Science", "B103", "Classroom B103"));
            ttList.add(new TimetableEntry("AI-DS-A", "Wednesday", "09:00 AM", "10:00 AM", "AI304", "Big Data Analytics & Spark", "Dr. Ananya Sen", "Block B — Artificial Intelligence & Data Science", "B105", "Machine Learning Studio B105"));
            ttList.add(new TimetableEntry("AI-DS-A", "Thursday", "02:00 PM", "04:00 PM", "AI305L", "Data Analytics Lab", "Prof. Suresh Iyer", "Block B — Artificial Intelligence & Data Science", "B104", "Data Analytics Lab B104"));
            ttList.add(new TimetableEntry("AI-DS-A", "Friday", "11:30 AM", "12:30 PM", "AI306", "Python for Scientific Computing", "Prof. Suresh Iyer", "Block B — Artificial Intelligence & Data Science", "B102", "Classroom B102"));

            // 4. AI-DS-B
            ttList.add(new TimetableEntry("AI-DS-B", "Monday", "10:15 AM", "11:15 AM", "AI311", "Natural Language Processing", "Dr. Kavita Nair", "Block B — Artificial Intelligence & Data Science", "B103", "Classroom B103"));
            ttList.add(new TimetableEntry("AI-DS-B", "Tuesday", "02:00 PM", "04:00 PM", "AI312L", "Neural Networks Lab", "Dr. Kavita Nair", "Block B — Artificial Intelligence & Data Science", "B101", "AI & Robotics Lab B101"));
            ttList.add(new TimetableEntry("AI-DS-B", "Wednesday", "10:15 AM", "11:15 AM", "AI313", "Computer Vision & Pattern Recognition", "Prof. Rakesh Verma", "Block B — Artificial Intelligence & Data Science", "B105", "Machine Learning Studio B105"));
            ttList.add(new TimetableEntry("AI-DS-B", "Thursday", "09:00 AM", "10:00 AM", "AI314", "Reinforcement Learning", "Dr. Kavita Nair", "Block B — Artificial Intelligence & Data Science", "B102", "Classroom B102"));
            ttList.add(new TimetableEntry("AI-DS-B", "Friday", "02:00 PM", "03:00 PM", "AI315", "AI Ethics & Model Governance", "Prof. Rakesh Verma", "Block B — Artificial Intelligence & Data Science", "B103", "Classroom B103"));

            timetableRepository.saveAll(ttList);
            System.out.println("Timetable Seeding Complete: " + ttList.size() + " entries published across 4 sections.");
        }

        if (examScheduleRepository != null && examScheduleRepository.count() == 0) {
            System.out.println("Seeding Examination Schedules & Venues (CSE-A, CSE-B, AI-DS-A, AI-DS-B)...");
            List<ExamSchedule> exList = new ArrayList<>();

            Long blkAId = getBlockIdByCode("BLK-A");
            Long blkBId = getBlockIdByCode("BLK-B");
            Long blkEId = getBlockIdByCode("BLK-E");

            // 1. CSE-A Examinations
            exList.add(new ExamSchedule("End Semester Examination Fall 2026", "CSE-A", "CS301", "Data Mining",
                    "18 Nov 2026", "10:00 AM", "01:00 PM", blkAId, "Block A — Computer Science", "A101", "Lecture Hall A101",
                    "Desk A-101", "Report 15 minutes before scheduled start with institutional ID card and hall ticket."));
            exList.add(new ExamSchedule("End Semester Examination Fall 2026", "CSE-A", "CS302", "Distributed Systems",
                    "21 Nov 2026", "10:00 AM", "01:00 PM", blkAId, "Block A — Computer Science", "A102", "Interactive Classroom A102",
                    "Desk A-102", "Standard examination stationery allowed. Scientific calculators permitted."));
            exList.add(new ExamSchedule("End Semester Examination Fall 2026", "CSE-A", "CS304", "Compiler Design",
                    "25 Nov 2026", "10:00 AM", "01:00 PM", blkEId, "Block E — Examination and Academic Facilities", "E101", "Central Examination Hall E101",
                    "Desk E-101", "Biometric attendance verification at main hall entrance."));

            // 2. CSE-B Examinations
            exList.add(new ExamSchedule("End Semester Examination Fall 2026", "CSE-B", "CS311", "Cloud Computing",
                    "18 Nov 2026", "02:00 PM", "05:00 PM", blkAId, "Block A — Computer Science", "A103", "Classroom A103",
                    "Desk A-103", "Report 15 minutes before scheduled start with institutional ID card."));
            exList.add(new ExamSchedule("End Semester Examination Fall 2026", "CSE-B", "CS314", "Cryptography & Network Security",
                    "21 Nov 2026", "02:00 PM", "05:00 PM", blkAId, "Block A — Computer Science", "A104", "Software Development Lab A104",
                    "Desk A-104", "Closed-book examination. All electronic devices must be deposited outside."));
            exList.add(new ExamSchedule("End Semester Examination Fall 2026", "CSE-B", "CS312", "Software Engineering",
                    "25 Nov 2026", "02:00 PM", "05:00 PM", blkEId, "Block E — Examination and Academic Facilities", "E102", "Central Examination Hall E102",
                    "Desk E-102", "Biometric verification required at hall entrance."));

            // 3. AI-DS-A Examinations
            exList.add(new ExamSchedule("End Semester Examination Fall 2026", "AI-DS-A", "AI301", "Machine Learning Foundations",
                    "19 Nov 2026", "10:00 AM", "01:00 PM", blkBId, "Block B — Artificial Intelligence & Data Science", "B101", "AI & Robotics Lab B101",
                    "Desk B-101", "Report 15 minutes before scheduled start with ID card."));
            exList.add(new ExamSchedule("End Semester Examination Fall 2026", "AI-DS-A", "AI303", "Deep Learning Architectures",
                    "23 Nov 2026", "10:00 AM", "01:00 PM", blkBId, "Block B — Artificial Intelligence & Data Science", "B102", "Classroom B102",
                    "Desk B-102", "Formula sheets will be provided in the examination hall."));

            // 4. AI-DS-B Examinations
            exList.add(new ExamSchedule("End Semester Examination Fall 2026", "AI-DS-B", "AI311", "Natural Language Processing",
                    "19 Nov 2026", "02:00 PM", "05:00 PM", blkBId, "Block B — Artificial Intelligence & Data Science", "B104", "Data Analytics Lab B104",
                    "Desk B-104", "Report 15 minutes before scheduled start with ID card."));
            exList.add(new ExamSchedule("End Semester Examination Fall 2026", "AI-DS-B", "AI313", "Computer Vision",
                    "23 Nov 2026", "02:00 PM", "05:00 PM", blkEId, "Block E — Examination and Academic Facilities", "E102", "Central Examination Hall E102",
                    "Desk E-105", "Biometric verification required at hall entrance."));

            examScheduleRepository.saveAll(exList);
            System.out.println("Examination Seeding Complete: " + exList.size() + " examination venue records published.");
        }
    }

    private Long getBlockIdByCode(String code) {
        if (blockRepository == null) return null;
        return blockRepository.findAll().stream()
                .filter(b -> code.equalsIgnoreCase(b.getCode()))
                .map(Block::getId)
                .findFirst()
                .orElse(null);
    }
}
