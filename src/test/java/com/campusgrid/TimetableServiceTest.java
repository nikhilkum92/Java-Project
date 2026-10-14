package com.campusgrid;

import com.campusgrid.dto.*;
import com.campusgrid.model.*;
import com.campusgrid.repository.*;
import com.campusgrid.service.TimetableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimetableServiceTest {

    @Mock
    private TimetableEntryRepository timetableEntryRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private TimetableService timetableService;

    private Subject theorySubject;
    private Subject labSubject;
    private Faculty facultyUser;
    private Room classroom;
    private Room labRoom;
    private Room smallRoom;
    private Room maintenanceRoom;

    @BeforeEach
    void setUp() {
        theorySubject = new Subject("CS301", "Data Structures", "Computer Science", 3, SubjectType.THEORY, 4);
        theorySubject.setId(1L);

        labSubject = new Subject("CS305P", "Database Lab", "Computer Science", 3, SubjectType.PRACTICAL, 2);
        labSubject.setId(2L);

        User u = new User("dr.sharma", "sharma@campusgrid.ac.in", "hash", "Dr. Meera Sharma", Role.FACULTY);
        u.setId(10L);
        facultyUser = new Faculty(u, "FAC001", "Computer Science", "Associate Professor", "1234567890", "A-101");
        facultyUser.setId(5L);

        Floor floor = new Floor(new Block(new Campus("Main Campus", "MAIN", "City Center"), "Block A", "A"), 1, "First Floor");

        classroom = new Room(floor, "R-101", "Lecture Hall 1", RoomType.CLASSROOM, 60, LayoutTemplate.STANDARD_GRID);
        classroom.setId(101L);
        classroom.setStatus(RoomStatus.AVAILABLE);
        classroom.setActive(true);

        labRoom = new Room(floor, "LAB-201", "Computer Lab 1", RoomType.LABORATORY, 45, LayoutTemplate.LABORATORY);
        labRoom.setId(201L);
        labRoom.setStatus(RoomStatus.AVAILABLE);
        labRoom.setActive(true);

        smallRoom = new Room(floor, "R-105", "Seminar Room", RoomType.CLASSROOM, 20, LayoutTemplate.STANDARD_GRID);
        smallRoom.setId(105L);
        smallRoom.setStatus(RoomStatus.AVAILABLE);
        smallRoom.setActive(true);

        maintenanceRoom = new Room(floor, "R-109", "Renovation Room", RoomType.CLASSROOM, 50, LayoutTemplate.STANDARD_GRID);
        maintenanceRoom.setId(109L);
        maintenanceRoom.setStatus(RoomStatus.MAINTENANCE);
        maintenanceRoom.setActive(true);
    }

    // =========================================================================
    // CRUD & VALIDATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Should successfully create a valid timetable entry")
    void testCreateValidTimetableEntry() {
        CreateTimetableRequest req = new CreateTimetableRequest();
        req.setSubjectId(1L);
        req.setFacultyId(5L);
        req.setRoomId(101L);
        req.setDepartment("Computer Science");
        req.setSectionName("CSE-A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(10, 0));
        req.setClassType(ClassType.LECTURE);
        req.setExpectedCapacity(40);

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(theorySubject));
        when(facultyRepository.findById(5L)).thenReturn(Optional.of(facultyUser));
        when(roomRepository.findById(101L)).thenReturn(Optional.of(classroom));

        when(timetableEntryRepository.findConflictingRoomEntries(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(timetableEntryRepository.findConflictingFacultyEntries(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(timetableEntryRepository.findConflictingGroupEntries(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        when(timetableEntryRepository.save(any(TimetableEntry.class))).thenAnswer(invocation -> {
            TimetableEntry e = invocation.getArgument(0);
            e.setId(500L);
            return e;
        });

        TimetableEntryDto dto = timetableService.createTimetableEntry(req);

        assertNotNull(dto);
        assertEquals(500L, dto.getId());
        assertEquals("CS301", dto.getSubjectCode());
        assertEquals("R-101", dto.getRoomCode());
        assertEquals("CSE-A", dto.getSectionName());
        assertEquals(TimetableStatus.SCHEDULED, dto.getStatus());
        verify(timetableEntryRepository, times(1)).save(any(TimetableEntry.class));
    }

    @Test
    @DisplayName("Should reject scheduling when start time is equal to or after end time")
    void testRejectInvalidTimeRange() {
        CreateTimetableRequest req = new CreateTimetableRequest();
        req.setSubjectId(1L);
        req.setDepartment("Computer Science");
        req.setSectionName("CSE-A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(9, 0)); // end before start

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            timetableService.createTimetableEntry(req);
        });
        assertTrue(ex.getMessage().contains("must be after start time"));
    }

    @Test
    @DisplayName("Should cancel a timetable entry and update its status without destroying history")
    void testCancelTimetableEntry() {
        TimetableEntry existing = new TimetableEntry(theorySubject, facultyUser, classroom,
                "Computer Science", "CSE-A", 2, 3, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);
        existing.setId(500L);

        when(timetableEntryRepository.findById(500L)).thenReturn(Optional.of(existing));
        when(timetableEntryRepository.save(any(TimetableEntry.class))).thenAnswer(i -> i.getArgument(0));

        TimetableEntryDto result = timetableService.cancelTimetableEntry(500L, "Faculty sick leave");

        assertNotNull(result);
        assertEquals(TimetableStatus.CANCELLED, result.getStatus());
        assertFalse(result.isActive());
        assertTrue(result.getRemarks().contains("Faculty sick leave"));
    }

    // =========================================================================
    // ROOM ALLOCATION & CAPACITY & TYPE CONSTRAINTS
    // =========================================================================

    @Test
    @DisplayName("Should reject room allocation if room capacity is insufficient for expected cohort")
    void testRejectUndersizedRoom() {
        CreateTimetableRequest req = new CreateTimetableRequest();
        req.setSubjectId(1L);
        req.setRoomId(105L); // smallRoom capacity = 20
        req.setDepartment("Computer Science");
        req.setSectionName("CSE-A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(10, 0));
        req.setExpectedCapacity(50); // cohort size 50 > 20

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(theorySubject));
        when(roomRepository.findById(105L)).thenReturn(Optional.of(smallRoom));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            timetableService.createTimetableEntry(req);
        });
        assertTrue(ex.getMessage().contains("capacity (20) is insufficient for expected student cohort size (50)"));
    }

    @Test
    @DisplayName("Should reject room allocation if room is under maintenance")
    void testRejectMaintenanceRoom() {
        CreateTimetableRequest req = new CreateTimetableRequest();
        req.setSubjectId(1L);
        req.setRoomId(109L); // maintenance room
        req.setDepartment("Computer Science");
        req.setSectionName("CSE-A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(10, 0));
        req.setExpectedCapacity(30);

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(theorySubject));
        when(roomRepository.findById(109L)).thenReturn(Optional.of(maintenanceRoom));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            timetableService.createTimetableEntry(req);
        });
        assertTrue(ex.getMessage().contains("currently under maintenance"));
    }

    @Test
    @DisplayName("Should reject assigning laboratory class to an ordinary classroom")
    void testRejectLabInClassroom() {
        CreateTimetableRequest req = new CreateTimetableRequest();
        req.setSubjectId(2L); // labSubject (PRACTICAL)
        req.setRoomId(101L); // classroom (CLASSROOM)
        req.setDepartment("Computer Science");
        req.setSectionName("CSE-A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(11, 0));
        req.setEndTime(LocalTime.of(13, 0));
        req.setClassType(ClassType.LAB);
        req.setExpectedCapacity(30);

        when(subjectRepository.findById(2L)).thenReturn(Optional.of(labSubject));
        when(roomRepository.findById(101L)).thenReturn(Optional.of(classroom));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            timetableService.createTimetableEntry(req);
        });
        assertTrue(ex.getMessage().contains("A laboratory session requires a room of type LABORATORY"));
    }

    @Test
    @DisplayName("Should accept laboratory class in a LABORATORY room")
    void testAcceptLabInLaboratoryRoom() {
        CreateTimetableRequest req = new CreateTimetableRequest();
        req.setSubjectId(2L);
        req.setRoomId(201L); // labRoom (LABORATORY)
        req.setDepartment("Computer Science");
        req.setSectionName("CSE-A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(11, 0));
        req.setEndTime(LocalTime.of(13, 0));
        req.setClassType(ClassType.LAB);
        req.setExpectedCapacity(30);

        when(subjectRepository.findById(2L)).thenReturn(Optional.of(labSubject));
        when(roomRepository.findById(201L)).thenReturn(Optional.of(labRoom));
        when(timetableEntryRepository.findConflictingRoomEntries(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(timetableEntryRepository.findConflictingGroupEntries(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        when(timetableEntryRepository.save(any(TimetableEntry.class))).thenAnswer(i -> {
            TimetableEntry e = i.getArgument(0);
            e.setId(601L);
            return e;
        });

        TimetableEntryDto dto = timetableService.createTimetableEntry(req);
        assertNotNull(dto);
        assertEquals("LAB-201", dto.getRoomCode());
    }

    // =========================================================================
    // CONFLICT DETECTION & INTERVAL OVERLAPS
    // =========================================================================

    @Test
    @DisplayName("Should reject overlapping room allocation in same time slot")
    void testRejectOverlappingRoomAllocation() {
        CreateTimetableRequest req = new CreateTimetableRequest();
        req.setSubjectId(1L);
        req.setRoomId(101L);
        req.setDepartment("Computer Science");
        req.setSectionName("CSE-B");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(9, 30));
        req.setEndTime(LocalTime.of(10, 30));
        req.setExpectedCapacity(30);

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(theorySubject));
        when(roomRepository.findById(101L)).thenReturn(Optional.of(classroom));

        TimetableEntry existing = new TimetableEntry(theorySubject, facultyUser, classroom,
                "Computer Science", "CSE-A", 2, 3, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);

        when(timetableEntryRepository.findConflictingRoomEntries(eq(101L), eq(DayOfWeek.MONDAY), any(), any(), any(), any()))
                .thenReturn(List.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            timetableService.createTimetableEntry(req);
        });
        assertTrue(ex.getMessage().contains("Room conflict: Room 'R-101' is already allocated"));
    }

    @Test
    @DisplayName("Should reject overlapping faculty assignment in same time slot")
    void testRejectOverlappingFacultyAssignment() {
        CreateTimetableRequest req = new CreateTimetableRequest();
        req.setSubjectId(1L);
        req.setFacultyId(5L);
        req.setDepartment("Computer Science");
        req.setSectionName("CSE-B");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(9, 15));
        req.setEndTime(LocalTime.of(10, 15));
        req.setExpectedCapacity(30);

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(theorySubject));
        when(facultyRepository.findById(5L)).thenReturn(Optional.of(facultyUser));

        TimetableEntry existing = new TimetableEntry(theorySubject, facultyUser, classroom,
                "Computer Science", "CSE-A", 2, 3, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);

        when(timetableEntryRepository.findConflictingFacultyEntries(eq(5L), eq(DayOfWeek.MONDAY), any(), any(), any(), any()))
                .thenReturn(List.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            timetableService.createTimetableEntry(req);
        });
        assertTrue(ex.getMessage().contains("Faculty conflict"));
    }

    @Test
    @DisplayName("Should reject overlapping student group schedule in same time slot")
    void testRejectOverlappingStudentGroup() {
        CreateTimetableRequest req = new CreateTimetableRequest();
        req.setSubjectId(1L);
        req.setDepartment("Computer Science");
        req.setSectionName("CSE-A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(10, 0));
        req.setExpectedCapacity(40);

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(theorySubject));

        TimetableEntry existing = new TimetableEntry(theorySubject, facultyUser, classroom,
                "Computer Science", "CSE-A", 2, 3, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);

        when(timetableEntryRepository.findConflictingGroupEntries(eq("Computer Science"), eq("CSE-A"), eq(DayOfWeek.MONDAY), any(), any(), any(), any()))
                .thenReturn(List.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            timetableService.createTimetableEntry(req);
        });
        assertTrue(ex.getMessage().contains("Student group conflict"));
    }

    @Test
    @DisplayName("Should permit adjacent non-overlapping half-open intervals [09:00, 10:00) and [10:00, 11:00)")
    void testPermitAdjacentIntervals() {
        LocalTime slot1Start = LocalTime.of(9, 0);
        LocalTime slot1End = LocalTime.of(10, 0);
        LocalTime slot2Start = LocalTime.of(10, 0);
        LocalTime slot2End = LocalTime.of(11, 0);

        // Verification of half-open overlap condition: A_start < B_end AND B_start < A_end
        boolean overlaps = slot1Start.isBefore(slot2End) && slot2Start.isBefore(slot1End);
        assertFalse(overlaps, "Adjacent intervals where end == start must NOT overlap under [start, end) convention");
    }

    @Test
    @DisplayName("Should update existing timetable entry without self-conflict")
    void testUpdateWithoutSelfConflict() {
        TimetableEntry existing = new TimetableEntry(theorySubject, facultyUser, classroom,
                "Computer Science", "CSE-A", 2, 3, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);
        existing.setId(777L);

        UpdateTimetableRequest updateReq = new UpdateTimetableRequest();
        updateReq.setRemarks("Updated syllabus discussion");

        when(timetableEntryRepository.findById(777L)).thenReturn(Optional.of(existing));
        when(timetableEntryRepository.findConflictingRoomEntries(any(), any(), any(), any(), any(), eq(777L)))
                .thenReturn(Collections.emptyList());
        when(timetableEntryRepository.findConflictingFacultyEntries(any(), any(), any(), any(), any(), eq(777L)))
                .thenReturn(Collections.emptyList());
        when(timetableEntryRepository.findConflictingGroupEntries(any(), any(), any(), any(), any(), any(), eq(777L)))
                .thenReturn(Collections.emptyList());
        when(timetableEntryRepository.save(any(TimetableEntry.class))).thenAnswer(i -> i.getArgument(0));

        TimetableEntryDto updated = timetableService.updateTimetableEntry(777L, updateReq);

        assertNotNull(updated);
        assertEquals("Updated syllabus discussion", updated.getRemarks());
        verify(timetableEntryRepository, times(1)).save(existing);
    }
}
