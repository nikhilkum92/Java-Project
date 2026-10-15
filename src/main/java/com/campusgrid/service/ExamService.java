package com.campusgrid.service;

import com.campusgrid.dto.*;
import com.campusgrid.model.*;
import com.campusgrid.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamService {

    private final ExamSessionRepository examSessionRepository;
    private final ExamRoomAllocationRepository examRoomAllocationRepository;
    private final ExamSeatAllocationRepository examSeatAllocationRepository;
    private final RoomRepository roomRepository;
    private final PhysicalSeatRepository physicalSeatRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final ExamSeatingEngine examSeatingEngine;

    public ExamService(
            ExamSessionRepository examSessionRepository,
            ExamRoomAllocationRepository examRoomAllocationRepository,
            ExamSeatAllocationRepository examSeatAllocationRepository,
            RoomRepository roomRepository,
            PhysicalSeatRepository physicalSeatRepository,
            StudentRepository studentRepository,
            FacultyRepository facultyRepository,
            SubjectRepository subjectRepository,
            ExamSeatingEngine examSeatingEngine
    ) {
        this.examSessionRepository = examSessionRepository;
        this.examRoomAllocationRepository = examRoomAllocationRepository;
        this.examSeatAllocationRepository = examSeatAllocationRepository;
        this.roomRepository = roomRepository;
        this.physicalSeatRepository = physicalSeatRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
        this.examSeatingEngine = examSeatingEngine;
    }

    // =========================================================================
    // EXAM SESSION CRUD & QUERY
    // =========================================================================

    public ExamDetailDto createExam(CreateExamRequest req) {
        validateExamTimes(req.getStartTime(), req.getEndTime());

        String examCode = (req.getExamCode() != null && !req.getExamCode().isBlank())
                ? req.getExamCode().trim().toUpperCase()
                : generateUniqueExamCode(req.getDepartment(), req.getCourseCode());

        if (examSessionRepository.existsByExamCode(examCode)) {
            throw new IllegalArgumentException("Exam code '" + examCode + "' already exists.");
        }

        Subject subject = null;
        if (req.getSubjectId() != null) {
            subject = subjectRepository.findById(req.getSubjectId()).orElse(null);
        }

        ExamSession session = new ExamSession(
                examCode,
                req.getTitle().trim(),
                subject,
                req.getDepartment().trim(),
                req.getSemester(),
                req.getTargetSection() != null ? req.getTargetSection().trim() : "ALL",
                req.getExamDate(),
                req.getStartTime(),
                req.getEndTime(),
                req.getMaxMarks() > 0 ? req.getMaxMarks() : 100,
                req.getInstructions()
        );

        if (req.getCourseCode() != null && !req.getCourseCode().isBlank()) {
            session.setCourseCode(req.getCourseCode().trim());
        }
        if (req.getCourseName() != null && !req.getCourseName().isBlank()) {
            session.setCourseName(req.getCourseName().trim());
        }

        session = examSessionRepository.save(session);

        // Optionally assign initial rooms
        if (req.getRoomIds() != null && !req.getRoomIds().isEmpty()) {
            for (int i = 0; i < req.getRoomIds().size(); i++) {
                Long roomId = req.getRoomIds().get(i);
                Long invigId = (req.getInvigilatorIds() != null && i < req.getInvigilatorIds().size())
                        ? req.getInvigilatorIds().get(i) : null;
                assignRoomToExam(session.getId(), new AssignExamRoomRequest(roomId, invigId, 0, null));
            }
        }

        return getExamById(session.getId());
    }

    public ExamDetailDto updateExam(Long id, UpdateExamRequest req) {
        ExamSession session = examSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + id));

        validateExamTimes(req.getStartTime(), req.getEndTime());

        // Check if date or time change causes conflict with already assigned rooms
        if (!session.getExamDate().equals(req.getExamDate()) ||
            !session.getStartTime().equals(req.getStartTime()) ||
            !session.getEndTime().equals(req.getEndTime())) {

            List<ExamRoomAllocation> roomAllocs = examRoomAllocationRepository.findByExamSessionId(id);
            for (ExamRoomAllocation ra : roomAllocs) {
                List<ExamRoomAllocation> conflicts = examRoomAllocationRepository.findConflictingRoomAllocations(
                        ra.getRoom().getId(), req.getExamDate(), req.getStartTime(), req.getEndTime(), id
                );
                if (!conflicts.isEmpty()) {
                    throw new IllegalStateException("Cannot reschedule exam: Room " + ra.getRoom().getRoomCode() +
                            " is already booked for another exam during the new time slot.");
                }
            }
        }

        session.setTitle(req.getTitle().trim());
        session.setDepartment(req.getDepartment().trim());
        session.setSemester(req.getSemester());
        if (req.getTargetSection() != null) {
            session.setTargetSection(req.getTargetSection().trim());
        }
        session.setExamDate(req.getExamDate());
        session.setStartTime(req.getStartTime());
        session.setEndTime(req.getEndTime());
        session.setDurationMinutes((int) java.time.Duration.between(req.getStartTime(), req.getEndTime()).toMinutes());
        session.setMaxMarks(req.getMaxMarks());
        session.setInstructions(req.getInstructions());

        if (req.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(req.getSubjectId()).orElse(null);
            session.setSubject(subject);
        }
        if (req.getCourseCode() != null && !req.getCourseCode().isBlank()) {
            session.setCourseCode(req.getCourseCode().trim());
        }
        if (req.getCourseName() != null && !req.getCourseName().isBlank()) {
            session.setCourseName(req.getCourseName().trim());
        }
        if (req.getStatus() != null) {
            session.setStatus(req.getStatus());
        }

        session = examSessionRepository.save(session);
        return getExamById(session.getId());
    }

    public void deleteExam(Long id) {
        if (!examSessionRepository.existsById(id)) {
            throw new IllegalArgumentException("Exam session not found with ID: " + id);
        }
        // Cascade delete seat allocations and room allocations
        examSeatAllocationRepository.deleteByExamSessionId(id);
        examRoomAllocationRepository.deleteByExamSessionId(id);
        examSessionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ExamDetailDto getExamById(Long id) {
        ExamSession session = examSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + id));
        return mapToDetailDto(session);
    }

    @Transactional(readOnly = true)
    public List<ExamDetailDto> filterExams(String department, Integer semester, ExamStatus status, String search) {
        List<ExamSession> sessions = examSessionRepository.filterExams(department, semester, status, search);
        return sessions.stream().map(this::mapToDetailDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExamStatsDto getExamStats() {
        long totalExams = examSessionRepository.count();
        long scheduled = examSessionRepository.findByStatus(ExamStatus.SCHEDULED).size();
        long seatingAllocated = examSessionRepository.findByStatus(ExamStatus.SEATING_ALLOCATED).size();
        long completed = examSessionRepository.findByStatus(ExamStatus.COMPLETED).size();
        long totalAllocatedSeats = examSeatAllocationRepository.count();
        long activeRooms = examRoomAllocationRepository.count();

        return new ExamStatsDto(
                totalExams,
                scheduled,
                seatingAllocated,
                completed,
                totalAllocatedSeats,
                activeRooms,
                totalAllocatedSeats
        );
    }

    // =========================================================================
    // ROOM ALLOCATION & CONFLICT CHECKING
    // =========================================================================

    public ExamRoomAllocationDto assignRoomToExam(Long examId, AssignExamRoomRequest req) {
        ExamSession session = examSessionRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + examId));

        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + req.getRoomId()));

        if (examRoomAllocationRepository.existsByExamSessionIdAndRoomId(examId, req.getRoomId())) {
            throw new IllegalArgumentException("Room " + room.getRoomCode() + " is already assigned to this exam session.");
        }

        // Room conflict check: Is room occupied by another exam at the same time?
        List<ExamRoomAllocation> roomConflicts = examRoomAllocationRepository.findConflictingRoomAllocations(
                room.getId(), session.getExamDate(), session.getStartTime(), session.getEndTime(), examId
        );
        if (!roomConflicts.isEmpty()) {
            ExamRoomAllocation conflict = roomConflicts.get(0);
            throw new IllegalStateException(String.format(
                    "Conflict detected: Room %s is already allocated for exam '%s' (%s - %s) on %s.",
                    room.getRoomCode(),
                    conflict.getExamSession().getTitle(),
                    conflict.getExamSession().getStartTime(),
                    conflict.getExamSession().getEndTime(),
                    session.getExamDate()
            ));
        }

        // Invigilator conflict check
        Faculty invigilator = null;
        if (req.getInvigilatorId() != null) {
            invigilator = facultyRepository.findById(req.getInvigilatorId())
                    .orElseThrow(() -> new IllegalArgumentException("Faculty invigilator not found with ID: " + req.getInvigilatorId()));

            List<ExamRoomAllocation> invigConflicts = examRoomAllocationRepository.findConflictingInvigilatorAllocations(
                    invigilator.getId(), session.getExamDate(), session.getStartTime(), session.getEndTime(), examId
            );
            if (!invigConflicts.isEmpty()) {
                ExamRoomAllocation conflict = invigConflicts.get(0);
                throw new IllegalStateException(String.format(
                        "Conflict detected: Faculty %s is already assigned to invigilate exam '%s' in room %s at this time.",
                        invigilator.getUser().getFullName(),
                        conflict.getExamSession().getTitle(),
                        conflict.getRoom().getRoomCode()
                ));
            }
        }

        int capacity = req.getAllocatedCapacity() > 0 ? req.getAllocatedCapacity() : room.getCapacity();
        ExamRoomAllocation alloc = new ExamRoomAllocation(session, room, invigilator, capacity, req.getNotes());
        alloc = examRoomAllocationRepository.save(alloc);

        return mapToRoomAllocationDto(alloc);
    }

    public void unassignRoomFromExam(Long examId, Long roomId) {
        ExamRoomAllocation alloc = examRoomAllocationRepository.findByExamSessionIdAndRoomId(examId, roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room allocation not found for exam ID: " + examId + " and room ID: " + roomId));

        // Delete any seat allocations in this room
        examSeatAllocationRepository.deleteByExamSessionIdAndRoomId(examId, roomId);
        examRoomAllocationRepository.delete(alloc);

        // Update exam status if all seats removed
        long remainingSeats = examSeatAllocationRepository.countByExamSessionId(examId);
        if (remainingSeats == 0) {
            ExamSession session = alloc.getExamSession();
            session.setStatus(ExamStatus.SCHEDULED);
            examSessionRepository.save(session);
        }
    }

    // =========================================================================
    // AUTOMATED SEATING ALLOCATION ENGINE
    // =========================================================================

    public ExamDetailDto generateSeatingArrangement(Long examId, GenerateExamSeatingRequest req) {
        ExamSession session = examSessionRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + examId));

        // 1. Clear any existing seating allocation for this exam session
        examSeatAllocationRepository.deleteByExamSessionId(examId);

        // 2. Fetch assigned rooms
        List<ExamRoomAllocation> roomAllocs;
        if (req.getRoomIds() != null && !req.getRoomIds().isEmpty()) {
            // Assign requested rooms if not yet assigned
            for (Long rId : req.getRoomIds()) {
                if (!examRoomAllocationRepository.existsByExamSessionIdAndRoomId(examId, rId)) {
                    assignRoomToExam(examId, new AssignExamRoomRequest(rId, null, 0, null));
                }
            }
            roomAllocs = examRoomAllocationRepository.findByExamSessionId(examId).stream()
                    .filter(ra -> req.getRoomIds().contains(ra.getRoom().getId()))
                    .collect(Collectors.toList());
        } else {
            roomAllocs = examRoomAllocationRepository.findByExamSessionId(examId);
        }

        if (roomAllocs.isEmpty()) {
            throw new IllegalStateException("Cannot generate seating: No exam rooms allocated to exam session '" + session.getTitle() + "'. Please allocate rooms first.");
        }

        // 3. Gather candidate students
        List<Student> students = getCandidateStudents(session, req.getSectionNames());
        if (students.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "No eligible students found in department '%s' (Semester %d) to seat for this exam.",
                    session.getDepartment(), session.getSemester()
            ));
        }

        // 4. Gather physical seats for each room in order
        Map<Room, List<PhysicalSeat>> roomSeatsMap = new LinkedHashMap<>();
        for (ExamRoomAllocation ra : roomAllocs) {
            Room room = ra.getRoom();
            List<PhysicalSeat> seats = physicalSeatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(room.getId());
            roomSeatsMap.put(room, seats);
        }

        // 5. Run seating engine
        SeatingStrategy strategy = req.getStrategy() != null ? req.getStrategy() : SeatingStrategy.ALTERNATING_SECTION;
        int spacingGap = req.getSpacingGap();
        List<ExamSeatingEngine.SeatingAssignment> assignments = examSeatingEngine.computeSeatingArrangement(
                students, roomSeatsMap, strategy, spacingGap
        );

        // 6. Persist allocations
        List<ExamSeatAllocation> toSave = new ArrayList<>();
        for (ExamSeatingEngine.SeatingAssignment assignment : assignments) {
            ExamSeatAllocation alloc = new ExamSeatAllocation(
                    session,
                    assignment.getStudent(),
                    assignment.getRoom(),
                    assignment.getSeat()
            );
            toSave.add(alloc);
        }
        examSeatAllocationRepository.saveAll(toSave);

        // 7. Update exam status
        session.setStatus(ExamStatus.SEATING_ALLOCATED);
        examSessionRepository.save(session);

        return getExamById(session.getId());
    }

    public void clearSeatingArrangement(Long examId) {
        ExamSession session = examSessionRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + examId));

        examSeatAllocationRepository.deleteByExamSessionId(examId);
        session.setStatus(ExamStatus.SCHEDULED);
        examSessionRepository.save(session);
    }

    // =========================================================================
    // VISUAL SEATING GRID & ATTENDANCE ROSTER
    // =========================================================================

    @Transactional(readOnly = true)
    public ExamSeatingGridDto getExamRoomSeatingGrid(Long examId, Long roomId) {
        ExamSession session = examSessionRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + examId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));

        List<PhysicalSeat> physicalSeats = physicalSeatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(roomId);
        List<ExamSeatAllocation> allocations = examSeatAllocationRepository.findByExamSessionIdAndRoomIdOrderByRowNumberAscColumnNumberAsc(examId, roomId);

        Map<Long, ExamSeatAllocation> seatAllocMap = allocations.stream()
                .collect(Collectors.toMap(a -> a.getPhysicalSeat().getId(), a -> a, (k1, k2) -> k1));

        Optional<ExamRoomAllocation> roomAllocOpt = examRoomAllocationRepository.findByExamSessionIdAndRoomId(examId, roomId);
        String invigilatorName = roomAllocOpt
                .filter(ra -> ra.getInvigilator() != null && ra.getInvigilator().getUser() != null)
                .map(ra -> ra.getInvigilator().getUser().getFullName())
                .orElse("Unassigned Invigilator");

        ExamSeatingGridDto gridDto = new ExamSeatingGridDto();
        gridDto.setExamId(session.getId());
        gridDto.setExamTitle(session.getTitle());
        gridDto.setExamCode(session.getExamCode());
        gridDto.setExamDate(session.getExamDate());
        gridDto.setTimeSlot(session.getStartTime() + " - " + session.getEndTime());
        gridDto.setRoomId(room.getId());
        gridDto.setRoomCode(room.getRoomCode());
        gridDto.setRoomName(room.getRoomName());
        gridDto.setBlockName((room.getFloor() != null && room.getFloor().getBlock() != null) ? room.getFloor().getBlock().getName() : "");
        gridDto.setRowsCount(room.getRowsCount());
        gridDto.setColumnsCount(room.getColumnsCount());
        gridDto.setTotalSeats(physicalSeats.size());
        gridDto.setAllocatedSeats(allocations.size());
        gridDto.setAvailableSeats(physicalSeats.size() - allocations.size());
        gridDto.setInvigilatorName(invigilatorName);

        List<ExamSeatCellDto> cellList = new ArrayList<>();
        for (PhysicalSeat ps : physicalSeats) {
            boolean isWalkway = (ps.getSeatType() == SeatType.AISLE_GAP || !ps.isActive() || ps.getStatus() == SeatStatus.MAINTENANCE);
            ExamSeatCellDto cell = new ExamSeatCellDto(
                    ps.getId(),
                    ps.getSeatNumber(),
                    ps.getRowNumber(),
                    ps.getColumnNumber(),
                    ps.getRowLabel(),
                    isWalkway
            );

            ExamSeatAllocation alloc = seatAllocMap.get(ps.getId());
            if (alloc != null) {
                cell.setAllocated(true);
                cell.setAllocationId(alloc.getId());
                cell.setAttendanceStatus(alloc.getAttendanceStatus());
                if (alloc.getStudent() != null) {
                    cell.setStudentId(alloc.getStudent().getId());
                    cell.setStudentRollNumber(alloc.getStudent().getRollNumber());
                    cell.setStudentName(alloc.getStudent().getUser() != null ? alloc.getStudent().getUser().getFullName() : "Student");
                    cell.setStudentDepartment(alloc.getStudent().getDepartment());
                    cell.setStudentSection(alloc.getStudent().getSectionName());
                }
            }
            cellList.add(cell);
        }
        gridDto.setSeats(cellList);

        return gridDto;
    }

    @Transactional(readOnly = true)
    public List<ExamRosterStudentDto> getExamRoomRoster(Long examId, Long roomId) {
        List<ExamSeatAllocation> allocations = examSeatAllocationRepository.findByExamSessionIdAndRoomIdOrderByRowNumberAscColumnNumberAsc(examId, roomId);

        return allocations.stream().map(a -> {
            Student s = a.getStudent();
            String name = (s != null && s.getUser() != null) ? s.getUser().getFullName() : "Student";
            String roll = s != null ? s.getRollNumber() : "TBA";
            String dept = s != null ? s.getDepartment() : "";
            String sec = s != null ? s.getSectionName() : "";

            return new ExamRosterStudentDto(
                    a.getId(),
                    a.getSeatNumber(),
                    a.getRowLabel(),
                    a.getRowNumber(),
                    a.getColumnNumber(),
                    s != null ? s.getId() : null,
                    roll,
                    name,
                    dept,
                    sec,
                    a.getAttendanceStatus(),
                    a.getRemarks()
            );
        }).collect(Collectors.toList());
    }

    public void markAttendance(Long allocationId, MarkExamAttendanceRequest req) {
        ExamSeatAllocation alloc = examSeatAllocationRepository.findById(allocationId)
                .orElseThrow(() -> new IllegalArgumentException("Seat allocation record not found with ID: " + allocationId));

        alloc.setAttendanceStatus(req.getAttendanceStatus());
        if (req.getRemarks() != null) {
            alloc.setRemarks(req.getRemarks().trim());
        }
        examSeatAllocationRepository.save(alloc);
    }

    // =========================================================================
    // STUDENT PORTAL INTEGRATION HOOK
    // =========================================================================

    @Transactional(readOnly = true)
    public List<StudentExamAllocationDto> getStudentExaminations(Long userId) {
        List<ExamSeatAllocation> list = examSeatAllocationRepository.findByStudentUserId(userId);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        List<StudentExamAllocationDto> result = new ArrayList<>();
        for (ExamSeatAllocation sa : list) {
            ExamSession session = sa.getExamSession();
            Room room = sa.getRoom();
            String blockName = (room != null && room.getFloor() != null && room.getFloor().getBlock() != null)
                    ? room.getFloor().getBlock().getName() : "";

            StudentExamAllocationDto dto = new StudentExamAllocationDto(
                    sa.getId(),
                    session.getTitle(),
                    session.getCourseCode(),
                    session.getCourseName(),
                    session.getExamDate().toString(),
                    session.getStartTime().toString(),
                    session.getEndTime().toString(),
                    room != null ? room.getRoomCode() : "TBA",
                    room != null ? room.getRoomName() : "To Be Announced",
                    blockName,
                    sa.getSeatNumber(),
                    session.getStatus().name(),
                    session.getInstructions()
            );
            result.add(dto);
        }
        return result;
    }

    // =========================================================================
    // INITIAL SEEDING FOR DEMO
    // =========================================================================

    public void seedInitialExams() {
        if (examSessionRepository.count() > 0) {
            return;
        }

        System.out.println("Seeding Module 4 Examination & Seating Allocation Data...");

        LocalDate today = LocalDate.now();
        LocalDate examDate1 = today.plusDays(7); // Next week
        LocalDate examDate2 = today.plusDays(9);
        LocalDate examDate3 = today.plusDays(12);

        // Subject links if available
        Subject dsa = subjectRepository.findBySubjectCode("CS301").orElse(null);
        Subject dbms = subjectRepository.findBySubjectCode("CS302").orElse(null);
        Subject dld = subjectRepository.findBySubjectCode("CS303").orElse(null);

        // 1. Exam Session 1: Data Structures Midterm
        ExamSession exam1 = new ExamSession(
                "EXAM-2026-CS301",
                "Mid-Term Exam: Data Structures & Algorithms",
                dsa,
                "Computer Science",
                3,
                "ALL",
                examDate1,
                LocalTime.of(9, 30),
                LocalTime.of(12, 30),
                100,
                "1. Scientific calculators permitted. 2. Mobile phones and smart watches strictly prohibited. 3. Arrive 15 minutes prior to scheduled start."
        );
        if (dsa == null) {
            exam1.setCourseCode("CS301");
            exam1.setCourseName("Data Structures & Algorithms");
        }
        exam1 = examSessionRepository.save(exam1);

        // 2. Exam Session 2: DBMS Midterm
        ExamSession exam2 = new ExamSession(
                "EXAM-2026-CS302",
                "Mid-Term Exam: Database Management Systems",
                dbms,
                "Computer Science",
                3,
                "ALL",
                examDate2,
                LocalTime.of(14, 0),
                LocalTime.of(17, 0),
                100,
                "1. Closed book examination. 2. SQL query sheets provided with exam paper."
        );
        if (dbms == null) {
            exam2.setCourseCode("CS302");
            exam2.setCourseName("Database Management Systems");
        }
        exam2 = examSessionRepository.save(exam2);

        // 3. Exam Session 3: Digital Logic
        ExamSession exam3 = new ExamSession(
                "EXAM-2026-CS303",
                "Mid-Term Exam: Digital Logic & Design",
                dld,
                "Computer Science",
                3,
                "ALL",
                examDate3,
                LocalTime.of(10, 0),
                LocalTime.of(13, 0),
                100,
                "1. Logic truth tables provided. 2. No programmable calculators allowed."
        );
        if (dld == null) {
            exam3.setCourseCode("CS303");
            exam3.setCourseName("Digital Logic & Design");
        }
        exam3 = examSessionRepository.save(exam3);

        // Assign Rooms and Faculty
        Optional<Room> roomA101 = roomRepository.findByRoomCode("A-101");
        Optional<Room> roomA102 = roomRepository.findByRoomCode("A-102");
        List<Faculty> facultyList = facultyRepository.findAll();
        Faculty invigilator1 = !facultyList.isEmpty() ? facultyList.get(0) : null;
        Faculty invigilator2 = facultyList.size() > 1 ? facultyList.get(1) : null;

        if (roomA101.isPresent()) {
            ExamRoomAllocation ra1 = new ExamRoomAllocation(exam1, roomA101.get(), invigilator1, roomA101.get().getCapacity(), "Main examination hall");
            examRoomAllocationRepository.save(ra1);

            ExamRoomAllocation ra2 = new ExamRoomAllocation(exam2, roomA101.get(), invigilator2, roomA101.get().getCapacity(), "Main examination hall");
            examRoomAllocationRepository.save(ra2);
        }

        if (roomA102.isPresent()) {
            ExamRoomAllocation ra3 = new ExamRoomAllocation(exam3, roomA102.get(), invigilator1, roomA102.get().getCapacity(), "Secondary hall");
            examRoomAllocationRepository.save(ra3);
        }

        // Generate automated seating arrangement for Exam 1 & Exam 2
        try {
            generateSeatingArrangement(exam1.getId(), new GenerateExamSeatingRequest(SeatingStrategy.ALTERNATING_SECTION, null, null, 0));
            generateSeatingArrangement(exam2.getId(), new GenerateExamSeatingRequest(SeatingStrategy.SPACED_GAP, null, null, 1));
            System.out.println("Module 4 Seeding Complete: 3 Exam sessions seeded with room allocations and automated anti-cheating seating!");
        } catch (Exception e) {
            System.out.println("Notice during exam seating generation: " + e.getMessage());
        }
    }

    // =========================================================================
    // PRIVATE HELPERS & MAPPERS
    // =========================================================================

    private void validateExamTimes(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Exam start time and end time are required.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Exam end time (" + end + ") must be after start time (" + start + ").");
        }
    }

    private String generateUniqueExamCode(String department, String courseCode) {
        String deptPrefix = (department != null && department.length() >= 2)
                ? department.substring(0, 2).toUpperCase() : "EX";
        String course = (courseCode != null && !courseCode.isBlank())
                ? courseCode.replaceAll("[^A-Za-z0-9]", "").toUpperCase() : "GEN";
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("EXAM-%s-%s-%s", deptPrefix, course, randomSuffix);
    }

    private List<Student> getCandidateStudents(ExamSession session, List<String> sectionNames) {
        if (sectionNames != null && !sectionNames.isEmpty()) {
            return studentRepository.findAll().stream()
                    .filter(s -> session.getDepartment().equalsIgnoreCase(s.getDepartment()))
                    .filter(s -> s.getCurrentSemester() == session.getSemester())
                    .filter(s -> sectionNames.stream().anyMatch(sec -> sec.equalsIgnoreCase(s.getSectionName())))
                    .collect(Collectors.toList());
        }

        if (session.getTargetSection() != null && !"ALL".equalsIgnoreCase(session.getTargetSection().trim())) {
            return studentRepository.findAll().stream()
                    .filter(s -> session.getDepartment().equalsIgnoreCase(s.getDepartment()))
                    .filter(s -> s.getCurrentSemester() == session.getSemester())
                    .filter(s -> session.getTargetSection().equalsIgnoreCase(s.getSectionName()))
                    .collect(Collectors.toList());
        }

        return studentRepository.findAll().stream()
                .filter(s -> session.getDepartment().equalsIgnoreCase(s.getDepartment()))
                .filter(s -> s.getCurrentSemester() == session.getSemester())
                .collect(Collectors.toList());
    }

    private ExamDetailDto mapToDetailDto(ExamSession s) {
        ExamDetailDto dto = new ExamDetailDto();
        dto.setId(s.getId());
        dto.setExamCode(s.getExamCode());
        dto.setTitle(s.getTitle());
        dto.setSubjectId(s.getSubject() != null ? s.getSubject().getId() : null);
        dto.setCourseCode(s.getCourseCode());
        dto.setCourseName(s.getCourseName());
        dto.setDepartment(s.getDepartment());
        dto.setSemester(s.getSemester());
        dto.setTargetSection(s.getTargetSection());
        dto.setExamDate(s.getExamDate());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        dto.setDurationMinutes(s.getDurationMinutes());
        dto.setMaxMarks(s.getMaxMarks());
        dto.setInstructions(s.getInstructions());
        dto.setStatus(s.getStatus());

        List<ExamRoomAllocation> roomAllocs = examRoomAllocationRepository.findByExamSessionId(s.getId());
        dto.setAllocatedRoomsCount(roomAllocs.size());
        dto.setAllocatedStudentsCount((int) examSeatAllocationRepository.countByExamSessionId(s.getId()));

        dto.setRooms(roomAllocs.stream().map(this::mapToRoomAllocationDto).collect(Collectors.toList()));
        return dto;
    }

    private ExamRoomAllocationDto mapToRoomAllocationDto(ExamRoomAllocation ra) {
        Room r = ra.getRoom();
        Faculty f = ra.getInvigilator();
        int seatedCount = (int) examSeatAllocationRepository.countByExamSessionIdAndRoomId(ra.getExamSession().getId(), r.getId());

        return new ExamRoomAllocationDto(
                ra.getId(),
                ra.getExamSession().getId(),
                r.getId(),
                r.getRoomCode(),
                r.getRoomName(),
                (r.getFloor() != null && r.getFloor().getBlock() != null) ? r.getFloor().getBlock().getName() : "",
                (r.getFloor() != null) ? r.getFloor().getFloorNumber() : 0,
                f != null ? f.getId() : null,
                (f != null && f.getUser() != null) ? f.getUser().getFullName() : "Not Assigned",
                f != null ? f.getEmployeeId() : "",
                ra.getAllocatedCapacity(),
                r.getCapacity(),
                seatedCount,
                ra.getNotes()
        );
    }
}
