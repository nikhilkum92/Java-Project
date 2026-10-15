package com.campusgrid.service;

import com.campusgrid.dto.*;
import com.campusgrid.model.*;
import com.campusgrid.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
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

    private TimetableEntryRepository timetableEntryRepository;

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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setTimetableEntryRepository(TimetableEntryRepository timetableEntryRepository) {
        this.timetableEntryRepository = timetableEntryRepository;
    }

    // =========================================================================
    // EXAM SESSION CRUD & LIFECYCLE
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

        if (session.isCancelled()) {
            throw new IllegalStateException("Cannot update a cancelled examination session. Reschedule a new session instead.");
        }

        validateExamTimes(req.getStartTime(), req.getEndTime());

        // Check if date or time change causes conflict with already assigned rooms or timetable
        boolean timeChanged = !session.getExamDate().equals(req.getExamDate()) ||
                              !session.getStartTime().equals(req.getStartTime()) ||
                              !session.getEndTime().equals(req.getEndTime());

        if (timeChanged) {
            List<ExamRoomAllocation> roomAllocs = examRoomAllocationRepository.findByExamSessionId(id);
            for (ExamRoomAllocation ra : roomAllocs) {
                verifyRoomAvailability(ra.getRoom(), req.getExamDate(), req.getStartTime(), req.getEndTime(), id);
            }
            // Date/time changed: invalidate previous seating allocations
            examSeatAllocationRepository.deleteByExamSessionId(id);
            session.setStatus(ExamStatus.SCHEDULED);
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
        if (req.getStatus() != null && req.getStatus() != ExamStatus.CANCELLED) {
            session.setStatus(req.getStatus());
        }

        session = examSessionRepository.save(session);
        return getExamById(session.getId());
    }

    public ExamDetailDto cancelExam(Long id, String reason) {
        ExamSession session = examSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + id));

        // Cancelled exams must not continue to occupy seats or block resources
        examSeatAllocationRepository.deleteByExamSessionId(id);
        session.cancel(reason != null ? reason : "Cancelled by examination administrator");
        session = examSessionRepository.save(session);

        return mapToDetailDto(session);
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
        long allocationComplete = examSessionRepository.findByStatus(ExamStatus.ALLOCATION_COMPLETE).size();
        long cancelled = examSessionRepository.findByStatus(ExamStatus.CANCELLED).size();
        long totalAllocatedSeats = examSeatAllocationRepository.count();
        long activeRooms = examRoomAllocationRepository.count();

        return new ExamStatsDto(
                totalExams,
                scheduled,
                allocationComplete,
                cancelled,
                totalAllocatedSeats,
                activeRooms,
                totalAllocatedSeats
        );
    }

    // =========================================================================
    // ROOM ALLOCATION & AVAILABILITY
    // =========================================================================

    @Transactional(readOnly = true)
    public List<AvailableExamRoomDto> getAvailableRoomsForExam(Long examId) {
        ExamSession session = examSessionRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + examId));

        List<Room> allRooms = roomRepository.findAll();
        List<ExamRoomAllocation> currentAllocs = examRoomAllocationRepository.findByExamSessionId(examId);
        Set<Long> assignedRoomIds = currentAllocs.stream().map(a -> a.getRoom().getId()).collect(Collectors.toSet());

        List<AvailableExamRoomDto> result = new ArrayList<>();

        for (Room room : allRooms) {
            boolean isAssigned = assignedRoomIds.contains(room.getId());
            List<PhysicalSeat> physicalSeats = physicalSeatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(room.getId());
            int totalPhysical = physicalSeats.size();

            // Usable seats: active, status AVAILABLE, not aisle gap
            int usable = (int) physicalSeats.stream()
                    .filter(s -> s.isActive() && s.getStatus() == SeatStatus.AVAILABLE && s.getSeatType() != SeatType.AISLE_GAP)
                    .count();
            int unavailable = totalPhysical - usable;

            boolean ready = true;
            String reason = null;

            if (room.getStatus() == RoomStatus.MAINTENANCE) {
                ready = false;
                reason = "Room is under maintenance";
            } else if (usable == 0) {
                ready = false;
                reason = "Physical seating layout is not configured";
            } else if (!isAssigned) {
                // Check conflicts if not already assigned to this exam
                try {
                    verifyRoomAvailability(room, session.getExamDate(), session.getStartTime(), session.getEndTime(), examId);
                } catch (IllegalStateException e) {
                    ready = false;
                    reason = e.getMessage();
                }
            }

            String blockName = (room.getFloor() != null && room.getFloor().getBlock() != null)
                    ? room.getFloor().getBlock().getName() : "";
            int floorNum = (room.getFloor() != null) ? room.getFloor().getFloorNumber() : 0;

            AvailableExamRoomDto dto = new AvailableExamRoomDto(
                    room.getId(),
                    room.getRoomCode(),
                    room.getRoomName(),
                    room.getRoomType(),
                    blockName,
                    floorNum,
                    room.getCapacity(),
                    totalPhysical,
                    usable,
                    unavailable,
                    ready,
                    reason,
                    isAssigned
            );
            result.add(dto);
        }

        // Sort: ready rooms first, then by room code
        result.sort(Comparator.comparing(AvailableExamRoomDto::isReadyForSeating).reversed()
                .thenComparing(AvailableExamRoomDto::getRoomCode));

        return result;
    }

    public ExamRoomAllocationDto assignRoomToExam(Long examId, AssignExamRoomRequest req) {
        ExamSession session = examSessionRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + examId));

        if (session.isCancelled()) {
            throw new IllegalStateException("Cannot assign rooms to a cancelled examination session.");
        }

        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + req.getRoomId()));

        if (examRoomAllocationRepository.existsByExamSessionIdAndRoomId(examId, req.getRoomId())) {
            throw new IllegalArgumentException("Room " + room.getRoomCode() + " is already assigned to this exam session.");
        }

        // Verify room availability against overlapping exams & timetable entries
        verifyRoomAvailability(room, session.getExamDate(), session.getStartTime(), session.getEndTime(), examId);

        // Verify invigilator conflict
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
                        "Conflict detected: Faculty %s is already assigned to invigilate exam '%s' in room %s during this time.",
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
            if (!session.isCancelled()) {
                session.setStatus(ExamStatus.SCHEDULED);
                examSessionRepository.save(session);
            }
        }
    }

    // =========================================================================
    // PHYSICAL SEAT ALLOCATION ENGINE
    // =========================================================================

    public ExamDetailDto allocateSeats(Long examId, AllocateSeatsRequest req) {
        ExamSession session = examSessionRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + examId));

        if (session.isCancelled()) {
            throw new IllegalStateException("Cannot allocate seats for a cancelled examination session.");
        }

        session.setStatus(ExamStatus.ALLOCATION_IN_PROGRESS);
        examSessionRepository.save(session);

        // 1. Clear any previous seating allocations for this session cleanly
        examSeatAllocationRepository.deleteByExamSessionId(examId);

        // 2. Fetch assigned rooms
        List<ExamRoomAllocation> roomAllocs;
        if (req.getRoomIds() != null && !req.getRoomIds().isEmpty()) {
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
            session.setStatus(ExamStatus.SCHEDULED);
            examSessionRepository.save(session);
            throw new IllegalStateException("Cannot allocate seats: No examination halls assigned to exam session '" + session.getTitle() + "'. Please assign rooms first.");
        }

        // 3. Gather eligible active students
        List<Student> students = getCandidateStudents(session, req.getCandidateStudentIds(), req.getSectionNames());
        if (students.isEmpty()) {
            session.setStatus(ExamStatus.SCHEDULED);
            examSessionRepository.save(session);
            throw new IllegalStateException(String.format(
                    "No eligible active students found in department '%s' (Semester %d) to seat for this examination.",
                    session.getDepartment(), session.getSemester()
            ));
        }

        // 4. Gather physical seats from assigned rooms
        Map<Room, List<PhysicalSeat>> roomSeatsMap = new LinkedHashMap<>();
        for (ExamRoomAllocation ra : roomAllocs) {
            Room room = ra.getRoom();
            List<PhysicalSeat> seats = physicalSeatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(room.getId());
            roomSeatsMap.put(room, seats);
        }

        // 5. Run seating engine deterministically
        SeatingStrategy strategy = req.getStrategy() != null ? req.getStrategy() : SeatingStrategy.ROLL_NUMBER_ASCENDING;
        int spacingGap = req.getSpacingGap();

        List<ExamSeatingEngine.SeatingAssignment> assignments;
        try {
            assignments = examSeatingEngine.computeSeatingArrangement(
                    students, roomSeatsMap, strategy, spacingGap
            );
        } catch (Exception e) {
            // Rollback status
            session.setStatus(ExamStatus.SCHEDULED);
            examSessionRepository.save(session);
            throw e;
        }

        // 6. Persist allocations transactionally
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

        // 7. Complete allocation lifecycle
        session.setStatus(ExamStatus.ALLOCATION_COMPLETE);
        examSessionRepository.save(session);

        return getExamById(session.getId());
    }

    public ExamDetailDto generateSeatingArrangement(Long examId, GenerateExamSeatingRequest req) {
        AllocateSeatsRequest allocReq = new AllocateSeatsRequest(
                req.getStrategy(),
                null,
                req.getRoomIds(),
                req.getSectionNames(),
                req.getSpacingGap()
        );
        return allocateSeats(examId, allocReq);
    }

    public void clearSeatingArrangement(Long examId) {
        ExamSession session = examSessionRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam session not found with ID: " + examId));

        examSeatAllocationRepository.deleteByExamSessionId(examId);
        if (!session.isCancelled()) {
            session.setStatus(ExamStatus.SCHEDULED);
            examSessionRepository.save(session);
        }
    }

    // =========================================================================
    // VISUAL SEATING PLAN & ALLOCATION OUTPUTS
    // =========================================================================

    @Transactional(readOnly = true)
    public ExamSeatingGridDto getExamRoomSeatingPlan(Long examId, Long roomId) {
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

        int usableSeatsCount = (int) physicalSeats.stream()
                .filter(s -> s.isActive() && s.getStatus() == SeatStatus.AVAILABLE && s.getSeatType() != SeatType.AISLE_GAP)
                .count();
        int unavailableSeatsCount = physicalSeats.size() - usableSeatsCount;

        // Unallocated candidates count
        List<Student> eligibleStudents = getCandidateStudents(session, null, null);
        long totalAllocatedInExam = examSeatAllocationRepository.countByExamSessionId(examId);
        int unallocated = Math.max(0, eligibleStudents.size() - (int) totalAllocatedInExam);

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
        gridDto.setFloorNumber((room.getFloor() != null) ? room.getFloor().getFloorNumber() : 0);
        gridDto.setRowsCount(room.getRowsCount());
        gridDto.setColumnsCount(room.getColumnsCount());
        gridDto.setTotalSeats(physicalSeats.size());
        gridDto.setUsableSeatsCount(usableSeatsCount);
        gridDto.setUnavailableSeatsCount(unavailableSeatsCount);
        gridDto.setAllocatedSeats(allocations.size());
        gridDto.setAvailableSeats(usableSeatsCount - allocations.size());
        gridDto.setUnallocatedCandidatesCount(unallocated);
        gridDto.setInvigilatorName(invigilatorName);

        List<ExamSeatCellDto> cellList = new ArrayList<>();
        for (PhysicalSeat ps : physicalSeats) {
            boolean isWalkway = (ps.getSeatType() == SeatType.AISLE_GAP || !ps.isActive());
            ExamSeatCellDto cell = new ExamSeatCellDto(
                    ps.getId(),
                    ps.getSeatNumber(),
                    ps.getRowNumber(),
                    ps.getColumnNumber(),
                    ps.getRowLabel(),
                    ps.getStatus(),
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
    public ExamSeatingGridDto getExamRoomSeatingGrid(Long examId, Long roomId) {
        return getExamRoomSeatingPlan(examId, roomId);
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

    @Transactional(readOnly = true)
    public List<ExamRosterStudentDto> getExamAllocations(Long examId) {
        List<ExamSeatAllocation> allocations = examSeatAllocationRepository.findByExamSessionId(examId);

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
    // STUDENT PORTAL SECURE ENDPOINTS HOOKS
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

    @Transactional(readOnly = true)
    public StudentExamAllocationDto getStudentExamAllocation(Long userId, Long examId) {
        List<ExamSeatAllocation> list = examSeatAllocationRepository.findByStudentUserId(userId);
        return list.stream()
                .filter(a -> a.getExamSession().getId().equals(examId))
                .findFirst()
                .map(sa -> {
                    ExamSession session = sa.getExamSession();
                    Room room = sa.getRoom();
                    String blockName = (room != null && room.getFloor() != null && room.getFloor().getBlock() != null)
                            ? room.getFloor().getBlock().getName() : "";

                    return new StudentExamAllocationDto(
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
                })
                .orElseThrow(() -> new IllegalArgumentException("No allocated examination seat found for this student and exam."));
    }

    // =========================================================================
    // INITIAL SEEDING
    // =========================================================================

    public void seedInitialExams() {
        if (examSessionRepository.count() > 0) {
            return;
        }

        System.out.println("Seeding Module 4 Examination & Seating Allocation Data...");

        LocalDate today = LocalDate.now();
        LocalDate examDate1 = today.plusDays(7);
        LocalDate examDate2 = today.plusDays(9);
        LocalDate examDate3 = today.plusDays(12);

        Subject dsa = subjectRepository.findBySubjectCode("CS301").orElse(null);
        Subject dbms = subjectRepository.findBySubjectCode("CS302").orElse(null);
        Subject dld = subjectRepository.findBySubjectCode("CS303").orElse(null);

        // Exam 1
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
                "Scientific calculators permitted. No smart watches allowed. Arrive 15 minutes prior."
        );
        if (dsa == null) {
            exam1.setCourseCode("CS301");
            exam1.setCourseName("Data Structures & Algorithms");
        }
        exam1 = examSessionRepository.save(exam1);

        // Exam 2
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
                "Closed book examination. SQL reference sheets provided."
        );
        if (dbms == null) {
            exam2.setCourseCode("CS302");
            exam2.setCourseName("Database Management Systems");
        }
        exam2 = examSessionRepository.save(exam2);

        // Exam 3
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
                "Logic truth tables provided."
        );
        if (dld == null) {
            exam3.setCourseCode("CS303");
            exam3.setCourseName("Digital Logic & Design");
        }
        exam3 = examSessionRepository.save(exam3);

        // Assign Rooms
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

        // Generate automated seating arrangement
        try {
            allocateSeats(exam1.getId(), new AllocateSeatsRequest(SeatingStrategy.ROLL_NUMBER_ASCENDING, null, null, null, 0));
            allocateSeats(exam2.getId(), new AllocateSeatsRequest(SeatingStrategy.SPACED_GAP, null, null, null, 1));
            System.out.println("Module 4 Seeding Complete: 3 Exam sessions initialized with room allocations and automated seating!");
        } catch (Exception e) {
            System.out.println("Notice during exam seating generation: " + e.getMessage());
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private void verifyRoomAvailability(Room room, LocalDate examDate, LocalTime start, LocalTime end, Long excludeExamId) {
        // 1. Check overlapping exam room allocations
        List<ExamRoomAllocation> examConflicts = examRoomAllocationRepository.findConflictingRoomAllocations(
                room.getId(), examDate, start, end, excludeExamId
        );
        if (!examConflicts.isEmpty()) {
            ExamRoomAllocation conflict = examConflicts.get(0);
            throw new IllegalStateException(String.format(
                    "Conflict detected: Room %s is already allocated for exam '%s' (%s - %s) on %s.",
                    room.getRoomCode(),
                    conflict.getExamSession().getTitle(),
                    conflict.getExamSession().getStartTime(),
                    conflict.getExamSession().getEndTime(),
                    examDate
            ));
        }

        // 2. Check Module 3 Timetable conflicts
        if (timetableEntryRepository != null) {
            DayOfWeek day = examDate.getDayOfWeek();
            List<TimetableEntry> ttEntries = timetableEntryRepository.findConflictingRoomEntries(
                    room.getId(), day, examDate, start, end, null
            );
            if (!ttEntries.isEmpty()) {
                TimetableEntry tt = ttEntries.get(0);
                String subjectTitle = tt.getSubject() != null ? tt.getSubject().getSubjectName() : "Regular Class";
                throw new IllegalStateException(String.format(
                        "Conflict detected: Room %s is occupied by timetable schedule '%s' (%s - %s) on %s.",
                        room.getRoomCode(),
                        subjectTitle,
                        tt.getStartTime(),
                        tt.getEndTime(),
                        day
                ));
            }
        }
    }

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

    private List<Student> getCandidateStudents(ExamSession session, List<Long> candidateStudentIds, List<String> sectionNames) {
        List<Student> allStudents = studentRepository.findAll();

        // STRICT: filter only ACTIVE students (Section 6: inactive or cancelled students must not be allocated)
        List<Student> activeStudents = allStudents.stream()
                .filter(s -> s.getUser() != null && s.getUser().isActive())
                .collect(Collectors.toList());

        if (candidateStudentIds != null && !candidateStudentIds.isEmpty()) {
            Set<Long> idSet = new HashSet<>(candidateStudentIds);
            return activeStudents.stream()
                    .filter(s -> idSet.contains(s.getId()))
                    .collect(Collectors.toList());
        }

        if (sectionNames != null && !sectionNames.isEmpty()) {
            return activeStudents.stream()
                    .filter(s -> session.getDepartment().equalsIgnoreCase(s.getDepartment()))
                    .filter(s -> s.getCurrentSemester() == session.getSemester())
                    .filter(s -> sectionNames.stream().anyMatch(sec -> sec.equalsIgnoreCase(s.getSectionName())))
                    .collect(Collectors.toList());
        }

        if (session.getTargetSection() != null && !"ALL".equalsIgnoreCase(session.getTargetSection().trim())) {
            return activeStudents.stream()
                    .filter(s -> session.getDepartment().equalsIgnoreCase(s.getDepartment()))
                    .filter(s -> s.getCurrentSemester() == session.getSemester())
                    .filter(s -> session.getTargetSection().equalsIgnoreCase(s.getSectionName()))
                    .collect(Collectors.toList());
        }

        return activeStudents.stream()
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
        dto.setCancellationReason(s.getCancellationReason());

        List<ExamRoomAllocation> roomAllocs = examRoomAllocationRepository.findByExamSessionId(s.getId());
        dto.setAllocatedRoomsCount(roomAllocs.size());

        int totalAllocated = (int) examSeatAllocationRepository.countByExamSessionId(s.getId());
        dto.setTotalAllocatedCandidates(totalAllocated);

        // Calculate eligible candidates and unallocated
        List<Student> eligible = getCandidateStudents(s, null, null);
        dto.setTotalEligibleCandidates(eligible.size());
        dto.setTotalUnallocatedCandidates(Math.max(0, eligible.size() - totalAllocated));

        // Calculate total usable vs unavailable seats in assigned rooms
        int usableSeatsSum = 0;
        int unavailableSeatsSum = 0;
        for (ExamRoomAllocation ra : roomAllocs) {
            List<PhysicalSeat> pSeats = physicalSeatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(ra.getRoom().getId());
            int usable = (int) pSeats.stream()
                    .filter(ps -> ps.isActive() && ps.getStatus() == SeatStatus.AVAILABLE && ps.getSeatType() != SeatType.AISLE_GAP)
                    .count();
            usableSeatsSum += usable;
            unavailableSeatsSum += (pSeats.size() - usable);
        }
        dto.setTotalUsableSeats(usableSeatsSum);
        dto.setTotalUnavailableSeats(unavailableSeatsSum);

        dto.setRooms(roomAllocs.stream().map(this::mapToRoomAllocationDto).collect(Collectors.toList()));
        return dto;
    }

    private ExamRoomAllocationDto mapToRoomAllocationDto(ExamRoomAllocation ra) {
        Room r = ra.getRoom();
        Faculty f = ra.getInvigilator();
        int seatedCount = (int) examSeatAllocationRepository.countByExamSessionIdAndRoomId(ra.getExamSession().getId(), r.getId());

        List<PhysicalSeat> pSeats = physicalSeatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(r.getId());
        int usable = (int) pSeats.stream()
                .filter(ps -> ps.isActive() && ps.getStatus() == SeatStatus.AVAILABLE && ps.getSeatType() != SeatType.AISLE_GAP)
                .count();
        int unavailable = pSeats.size() - usable;

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
                usable,
                unavailable,
                seatedCount,
                ra.getNotes()
        );
    }

    /**
     * Retrieve examination room allocations where the given faculty member is assigned as invigilator.
     */
    @Transactional(readOnly = true)
    public List<ExamRoomAllocationDto> getFacultyInvigilationDuties(Long userId) {
        return facultyRepository.findByUserId(userId)
                .map(fac -> examRoomAllocationRepository.findByInvigilatorId(fac.getId())
                        .stream().map(this::mapToRoomAllocationDto).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
