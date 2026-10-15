package com.campusgrid;

import com.campusgrid.dto.*;
import com.campusgrid.model.*;
import com.campusgrid.repository.*;
import com.campusgrid.service.ExamSeatingEngine;
import com.campusgrid.service.ExamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamServiceTest {

    @Mock
    private ExamSessionRepository examSessionRepository;

    @Mock
    private ExamRoomAllocationRepository examRoomAllocationRepository;

    @Mock
    private ExamSeatAllocationRepository examSeatAllocationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PhysicalSeatRepository physicalSeatRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Spy
    private ExamSeatingEngine examSeatingEngine = new ExamSeatingEngine();

    @InjectMocks
    private ExamService examService;

    private ExamSession sampleExam;
    private Room sampleRoom;
    private Faculty sampleFaculty;
    private Student student1;
    private Student student2;
    private PhysicalSeat seat1;
    private PhysicalSeat seat2;

    @BeforeEach
    void setUp() {
        sampleExam = new ExamSession(
                "EXAM-CS301-TEST",
                "Mid-Term Data Structures",
                null,
                "Computer Science",
                3,
                "ALL",
                LocalDate.now().plusDays(5),
                LocalTime.of(10, 0),
                LocalTime.of(13, 0),
                100,
                "Bring valid ID card"
        );
        sampleExam.setId(10L);
        sampleExam.setCourseCode("CS301");
        sampleExam.setCourseName("Data Structures");

        sampleRoom = new Room(null, "A-101", "Lecture Hall 101", RoomType.CLASSROOM, 40, "Projector");
        sampleRoom.setId(101L);

        User facUser = new User("meera", "meera@college.ac.in", "hash", "Dr. Meera", Role.FACULTY);
        facUser.setId(201L);
        sampleFaculty = new Faculty(facUser, "FAC001", "Computer Science", "Professor", "999", "A-305");
        sampleFaculty.setId(501L);

        User stuUser1 = new User("rahul", "rahul@college.ac.in", "hash", "Rahul Sharma", Role.STUDENT);
        stuUser1.setId(301L);
        student1 = new Student(stuUser1, "23CSE001", "Computer Science", "CSE-A", 2, 3, "9876543210");
        student1.setId(401L);

        User stuUser2 = new User("arjun", "arjun@college.ac.in", "hash", "Arjun Mehta", Role.STUDENT);
        stuUser2.setId(302L);
        student2 = new Student(stuUser2, "23CSE002", "Computer Science", "CSE-B", 2, 3, "9876543211");
        student2.setId(402L);

        seat1 = new PhysicalSeat(sampleRoom, "R1-C1", 1, 1, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        seat1.setId(601L);

        seat2 = new PhysicalSeat(sampleRoom, "R1-C2", 1, 2, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        seat2.setId(602L);
    }

    @Test
    @DisplayName("Should successfully create a new examination session")
    void testCreateExam() {
        CreateExamRequest req = new CreateExamRequest();
        req.setTitle("Operating Systems Midterm");
        req.setDepartment("Computer Science");
        req.setSemester(3);
        req.setExamDate(LocalDate.now().plusDays(7));
        req.setStartTime(LocalTime.of(9, 30));
        req.setEndTime(LocalTime.of(12, 30));
        req.setMaxMarks(100);

        when(examSessionRepository.save(any(ExamSession.class))).thenAnswer(inv -> {
            ExamSession s = inv.getArgument(0);
            s.setId(11L);
            return s;
        });
        when(examSessionRepository.findById(11L)).thenReturn(Optional.of(sampleExam));

        ExamDetailDto result = examService.createExam(req);
        assertNotNull(result);
        verify(examSessionRepository).save(any(ExamSession.class));
    }

    @Test
    @DisplayName("Should reject invalid exam times when end time is before start time")
    void testCreateExamInvalidTimes() {
        CreateExamRequest req = new CreateExamRequest();
        req.setTitle("Invalid Time Exam");
        req.setStartTime(LocalTime.of(14, 0));
        req.setEndTime(LocalTime.of(10, 0)); // invalid end time

        assertThrows(IllegalArgumentException.class, () -> examService.createExam(req));
    }

    @Test
    @DisplayName("Should detect room collision conflict when room is already allocated to another overlapping exam")
    void testAssignRoomCollisionConflict() {
        AssignExamRoomRequest req = new AssignExamRoomRequest(101L, null, 40, null);

        when(examSessionRepository.findById(10L)).thenReturn(Optional.of(sampleExam));
        when(roomRepository.findById(101L)).thenReturn(Optional.of(sampleRoom));

        // Simulate conflict
        ExamSession otherExam = new ExamSession();
        otherExam.setTitle("Physics Midterm");
        otherExam.setStartTime(LocalTime.of(9, 0));
        otherExam.setEndTime(LocalTime.of(11, 0));
        ExamRoomAllocation conflictingAlloc = new ExamRoomAllocation(otherExam, sampleRoom, null, 40, null);

        when(examRoomAllocationRepository.findConflictingRoomAllocations(anyLong(), any(), any(), any(), anyLong()))
                .thenReturn(List.of(conflictingAlloc));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                examService.assignRoomToExam(10L, req)
        );
        assertTrue(ex.getMessage().contains("Conflict detected: Room A-101 is already allocated"));
    }

    @Test
    @DisplayName("Should successfully assign hall to exam when no conflicts exist")
    void testAssignRoomSuccess() {
        AssignExamRoomRequest req = new AssignExamRoomRequest(101L, 501L, 40, "Room notes");

        when(examSessionRepository.findById(10L)).thenReturn(Optional.of(sampleExam));
        when(roomRepository.findById(101L)).thenReturn(Optional.of(sampleRoom));
        when(facultyRepository.findById(501L)).thenReturn(Optional.of(sampleFaculty));
        when(examRoomAllocationRepository.findConflictingRoomAllocations(anyLong(), any(), any(), any(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(examRoomAllocationRepository.findConflictingInvigilatorAllocations(anyLong(), any(), any(), any(), anyLong()))
                .thenReturn(Collections.emptyList());

        when(examRoomAllocationRepository.save(any(ExamRoomAllocation.class))).thenAnswer(inv -> {
            ExamRoomAllocation a = inv.getArgument(0);
            a.setId(901L);
            return a;
        });

        ExamRoomAllocationDto dto = examService.assignRoomToExam(10L, req);
        assertNotNull(dto);
        assertEquals("A-101", dto.getRoomCode());
        assertEquals("Dr. Meera", dto.getInvigilatorName());
    }

    @Test
    @DisplayName("Should generate anti-cheating alternating seating arrangement for students")
    void testGenerateSeatingArrangement() {
        when(examSessionRepository.findById(10L)).thenReturn(Optional.of(sampleExam));

        ExamRoomAllocation ra = new ExamRoomAllocation(sampleExam, sampleRoom, sampleFaculty, 40, null);
        when(examRoomAllocationRepository.findByExamSessionId(10L)).thenReturn(List.of(ra));

        // 2 candidates from different sections
        when(studentRepository.findAll()).thenReturn(List.of(student1, student2));
        // 2 physical seats
        when(physicalSeatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(101L))
                .thenReturn(List.of(seat1, seat2));

        GenerateExamSeatingRequest req = new GenerateExamSeatingRequest(
                SeatingStrategy.ALTERNATING_SECTION, null, null, 0
        );

        ExamDetailDto result = examService.generateSeatingArrangement(10L, req);
        assertNotNull(result);

        // Verify allocations saved
        verify(examSeatAllocationRepository).saveAll(anyList());
        assertEquals(ExamStatus.ALLOCATION_COMPLETE, sampleExam.getStatus());
    }

    @Test
    @DisplayName("Should retrieve student exam seat allocations formatted for student portal")
    void testGetStudentExaminations() {
        ExamSeatAllocation alloc = new ExamSeatAllocation(sampleExam, student1, sampleRoom, seat1);
        alloc.setId(701L);

        when(examSeatAllocationRepository.findByStudentUserId(301L)).thenReturn(List.of(alloc));

        List<StudentExamAllocationDto> studentExams = examService.getStudentExaminations(301L);
        assertNotNull(studentExams);
        assertEquals(1, studentExams.size());

        StudentExamAllocationDto dto = studentExams.get(0);
        assertEquals("Mid-Term Data Structures", dto.getExamName());
        assertEquals("CS301", dto.getCourseCode());
        assertEquals("A-101", dto.getRoomCode());
        assertEquals("R1-C1", dto.getSeatNumber());
    }

    @Test
    @DisplayName("Should update candidate attendance status")
    void testMarkAttendance() {
        ExamSeatAllocation alloc = new ExamSeatAllocation(sampleExam, student1, sampleRoom, seat1);
        alloc.setId(701L);

        when(examSeatAllocationRepository.findById(701L)).thenReturn(Optional.of(alloc));

        MarkExamAttendanceRequest req = new MarkExamAttendanceRequest(ExamAttendanceStatus.PRESENT, "Present on time");
        examService.markAttendance(701L, req);

        assertEquals(ExamAttendanceStatus.PRESENT, alloc.getAttendanceStatus());
        assertEquals("Present on time", alloc.getRemarks());
        verify(examSeatAllocationRepository).save(alloc);
    }

    @Test
    @DisplayName("Should reject examination creation when duplicate exam code is provided")
    void testCreateExamDuplicateCodeRejection() {
        CreateExamRequest req = new CreateExamRequest();
        req.setExamCode("EXAM-CS301-DUPE");
        req.setTitle("Duplicate Exam");
        req.setDepartment("Computer Science");
        req.setSemester(3);
        req.setExamDate(LocalDate.now().plusDays(5));
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(13, 0));

        when(examSessionRepository.existsByExamCode("EXAM-CS301-DUPE")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                examService.createExam(req)
        );
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should reject updating an examination that is in CANCELLED status")
    void testUpdateCancelledExamThrowsException() {
        sampleExam.setStatus(ExamStatus.CANCELLED);
        when(examSessionRepository.findById(10L)).thenReturn(Optional.of(sampleExam));

        UpdateExamRequest req = new UpdateExamRequest();
        req.setTitle("Updated Title");
        req.setDepartment("Computer Science");
        req.setSemester(3);
        req.setExamDate(LocalDate.now().plusDays(5));
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(13, 0));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                examService.updateExam(10L, req)
        );
        assertTrue(ex.getMessage().contains("Cannot update a cancelled examination session"));
    }

    @Test
    @DisplayName("Should free seat allocations, set CANCELLED status, and record reason upon cancellation")
    void testCancelExamFreesSeatsAndSetsReason() {
        when(examSessionRepository.findById(10L)).thenReturn(Optional.of(sampleExam));
        when(examSessionRepository.save(any(ExamSession.class))).thenAnswer(inv -> inv.getArgument(0));

        ExamDetailDto dto = examService.cancelExam(10L, "Inclement weather");

        assertNotNull(dto);
        assertEquals(ExamStatus.CANCELLED, sampleExam.getStatus());
        assertEquals("Inclement weather", sampleExam.getCancellationReason());
        verify(examSeatAllocationRepository).deleteByExamSessionId(10L);
    }

    @Test
    @DisplayName("Should strictly exclude inactive students from candidate eligibility")
    void testAllocateSeatsExcludesInactiveStudents() {
        when(examSessionRepository.findById(10L)).thenReturn(Optional.of(sampleExam));

        ExamRoomAllocation ra = new ExamRoomAllocation(sampleExam, sampleRoom, sampleFaculty, 40, null);
        when(examRoomAllocationRepository.findByExamSessionId(10L)).thenReturn(List.of(ra));

        // Mark student2 as inactive in User entity
        student2.getUser().setActive(false);
        when(studentRepository.findAll()).thenReturn(List.of(student1, student2));
        when(physicalSeatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(101L))
                .thenReturn(List.of(seat1, seat2));

        AllocateSeatsRequest req = new AllocateSeatsRequest();
        examService.allocateSeats(10L, req);

        // Verify only 1 active student was allocated
        verify(examSeatAllocationRepository).saveAll(argThat(list -> {
            List<?> l = (List<?>) list;
            return l.size() == 1;
        }));
    }

    @Test
    @DisplayName("Should rollback status to SCHEDULED when capacity is insufficient")
    void testAllocateSeatsCapacityShortageRevertsStatus() {
        when(examSessionRepository.findById(10L)).thenReturn(Optional.of(sampleExam));

        ExamRoomAllocation ra = new ExamRoomAllocation(sampleExam, sampleRoom, sampleFaculty, 40, null);
        when(examRoomAllocationRepository.findByExamSessionId(10L)).thenReturn(List.of(ra));

        // 2 candidates but only 1 seat
        when(studentRepository.findAll()).thenReturn(List.of(student1, student2));
        when(physicalSeatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(101L))
                .thenReturn(List.of(seat1));

        AllocateSeatsRequest req = new AllocateSeatsRequest();
        assertThrows(IllegalStateException.class, () -> examService.allocateSeats(10L, req));

        // Status should be reverted to SCHEDULED, not left as ALLOCATION_IN_PROGRESS
        assertEquals(ExamStatus.SCHEDULED, sampleExam.getStatus());
    }

    @Test
    @DisplayName("Should clear previous seat allocations before saving fresh allocation rerun")
    void testAllocateSeatsClearsPreviousAllocations() {
        when(examSessionRepository.findById(10L)).thenReturn(Optional.of(sampleExam));

        ExamRoomAllocation ra = new ExamRoomAllocation(sampleExam, sampleRoom, sampleFaculty, 40, null);
        when(examRoomAllocationRepository.findByExamSessionId(10L)).thenReturn(List.of(ra));

        when(studentRepository.findAll()).thenReturn(List.of(student1));
        when(physicalSeatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(101L))
                .thenReturn(List.of(seat1));

        examService.allocateSeats(10L, new AllocateSeatsRequest());

        // Verify deleteByExamSessionId was called prior to re-allocation
        verify(examSeatAllocationRepository).deleteByExamSessionId(10L);
    }
}
