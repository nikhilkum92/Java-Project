package com.campusgrid.service;

import com.campusgrid.dto.*;
import com.campusgrid.model.*;
import com.campusgrid.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TimetableService {

    private final TimetableEntryRepository timetableEntryRepository;
    private final SubjectRepository subjectRepository;
    private final RoomRepository roomRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    // Lock object for serializing room allocation operations across concurrent threads
    private final Object allocationLock = new Object();

    public TimetableService(
            TimetableEntryRepository timetableEntryRepository,
            SubjectRepository subjectRepository,
            RoomRepository roomRepository,
            FacultyRepository facultyRepository,
            StudentRepository studentRepository
    ) {
        this.timetableEntryRepository = timetableEntryRepository;
        this.subjectRepository = subjectRepository;
        this.roomRepository = roomRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Create a new timetable entry with conflict verification.
     */
    @Transactional
    public TimetableEntryDto createTimetableEntry(CreateTimetableRequest req) {
        validateTimeRange(req.getStartTime(), req.getEndTime());

        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + req.getSubjectId()));

        Faculty faculty = null;
        if (req.getFacultyId() != null) {
            faculty = facultyRepository.findById(req.getFacultyId())
                    .orElseThrow(() -> new IllegalArgumentException("Faculty not found with ID: " + req.getFacultyId()));
        }

        Room room = null;
        if (req.getRoomId() != null) {
            room = roomRepository.findById(req.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + req.getRoomId()));
        }

        // Synchronize allocation validation to guarantee concurrency protection
        synchronized (allocationLock) {
            // Verify all hard scheduling constraints
            verifySchedulingConstraints(
                    null, // new entry, no self ID to exclude
                    room,
                    faculty,
                    req.getDepartment().trim(),
                    req.getSectionName().trim(),
                    req.getDayOfWeek(),
                    req.getScheduleDate(),
                    req.getStartTime(),
                    req.getEndTime(),
                    req.getClassType(),
                    req.getExpectedCapacity(),
                    subject
            );

            TimetableEntry entry = new TimetableEntry();
            entry.setSubject(subject);
            entry.setFaculty(faculty);
            entry.setRoom(room);
            entry.setDepartment(req.getDepartment().trim());
            entry.setSectionName(req.getSectionName().trim().toUpperCase());
            entry.setAcademicYear(req.getAcademicYear() > 0 ? req.getAcademicYear() : 2);
            entry.setSemester(req.getSemester() > 0 ? req.getSemester() : 3);
            entry.setDayOfWeek(req.getDayOfWeek());
            entry.setScheduleDate(req.getScheduleDate());
            entry.setStartTime(req.getStartTime());
            entry.setEndTime(req.getEndTime());
            entry.setClassType(req.getClassType() != null ? req.getClassType() : ClassType.LECTURE);
            entry.setExpectedCapacity(req.getExpectedCapacity() > 0 ? req.getExpectedCapacity() : 40);
            entry.setStatus(TimetableStatus.SCHEDULED);
            entry.setActive(true);
            entry.setRemarks(req.getRemarks());

            TimetableEntry saved = timetableEntryRepository.save(entry);
            return mapToDto(saved);
        }
    }

    /**
     * Update an existing timetable entry with self-update immunity.
     */
    @Transactional
    public TimetableEntryDto updateTimetableEntry(Long id, UpdateTimetableRequest req) {
        TimetableEntry entry = timetableEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Timetable entry not found with ID: " + id));

        Subject subject = entry.getSubject();
        if (req.getSubjectId() != null) {
            subject = subjectRepository.findById(req.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + req.getSubjectId()));
        }

        Faculty faculty = entry.getFaculty();
        if (req.getFacultyId() != null) {
            faculty = facultyRepository.findById(req.getFacultyId())
                    .orElseThrow(() -> new IllegalArgumentException("Faculty not found with ID: " + req.getFacultyId()));
        }

        Room room = entry.getRoom();
        if (req.getRoomId() != null) {
            room = roomRepository.findById(req.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + req.getRoomId()));
        }

        String department = req.getDepartment() != null && !req.getDepartment().isBlank()
                ? req.getDepartment().trim() : entry.getDepartment();
        String sectionName = req.getSectionName() != null && !req.getSectionName().isBlank()
                ? req.getSectionName().trim().toUpperCase() : entry.getSectionName();

        DayOfWeek dayOfWeek = req.getDayOfWeek() != null ? req.getDayOfWeek() : entry.getDayOfWeek();
        LocalDate scheduleDate = req.getScheduleDate() != null ? req.getScheduleDate() : entry.getScheduleDate();
        LocalTime startTime = req.getStartTime() != null ? req.getStartTime() : entry.getStartTime();
        LocalTime endTime = req.getEndTime() != null ? req.getEndTime() : entry.getEndTime();
        ClassType classType = req.getClassType() != null ? req.getClassType() : entry.getClassType();
        int expectedCapacity = req.getExpectedCapacity() != null && req.getExpectedCapacity() > 0
                ? req.getExpectedCapacity() : entry.getExpectedCapacity();

        validateTimeRange(startTime, endTime);

        synchronized (allocationLock) {
            // Verify constraints excluding this entry's ID
            verifySchedulingConstraints(
                    id,
                    room,
                    faculty,
                    department,
                    sectionName,
                    dayOfWeek,
                    scheduleDate,
                    startTime,
                    endTime,
                    classType,
                    expectedCapacity,
                    subject
            );

            entry.setSubject(subject);
            entry.setFaculty(faculty);
            entry.setRoom(room);
            entry.setDepartment(department);
            entry.setSectionName(sectionName);
            entry.setDayOfWeek(dayOfWeek);
            entry.setScheduleDate(scheduleDate);
            entry.setStartTime(startTime);
            entry.setEndTime(endTime);
            entry.setClassType(classType);
            entry.setExpectedCapacity(expectedCapacity);

            if (req.getAcademicYear() != null && req.getAcademicYear() > 0) {
                entry.setAcademicYear(req.getAcademicYear());
            }
            if (req.getSemester() != null && req.getSemester() > 0) {
                entry.setSemester(req.getSemester());
            }
            if (req.getStatus() != null) {
                entry.setStatus(req.getStatus());
            }
            if (req.getRemarks() != null) {
                entry.setRemarks(req.getRemarks());
            }

            TimetableEntry updated = timetableEntryRepository.save(entry);
            return mapToDto(updated);
        }
    }

    /**
     * Allocate or reassign a physical room to a timetable entry.
     */
    @Transactional
    public TimetableEntryDto allocateRoom(Long entryId, Long roomId) {
        TimetableEntry entry = timetableEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable entry not found with ID: " + entryId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));

        synchronized (allocationLock) {
            verifyRoomConstraints(entry.getId(), room, entry.getDayOfWeek(), entry.getScheduleDate(),
                    entry.getStartTime(), entry.getEndTime(), entry.getClassType(),
                    entry.getExpectedCapacity(), entry.getSubject());

            entry.setRoom(room);
            TimetableEntry updated = timetableEntryRepository.save(entry);
            return mapToDto(updated);
        }
    }

    /**
     * Deallocate room from a timetable entry without cancelling the class.
     */
    @Transactional
    public TimetableEntryDto deallocateRoom(Long entryId) {
        TimetableEntry entry = timetableEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable entry not found with ID: " + entryId));

        entry.setRoom(null);
        TimetableEntry updated = timetableEntryRepository.save(entry);
        return mapToDto(updated);
    }

    /**
     * Cancel or deactivate a timetable entry while preserving its historical record.
     */
    @Transactional
    public TimetableEntryDto cancelTimetableEntry(Long id, String reason) {
        TimetableEntry entry = timetableEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Timetable entry not found with ID: " + id));

        entry.setStatus(TimetableStatus.CANCELLED);
        entry.setActive(false);
        if (reason != null && !reason.isBlank()) {
            String updatedRemarks = (entry.getRemarks() != null ? entry.getRemarks() + " | " : "") + "Cancelled: " + reason.trim();
            entry.setRemarks(updatedRemarks);
        }

        TimetableEntry updated = timetableEntryRepository.save(entry);
        return mapToDto(updated);
    }

    /**
     * View single entry by ID.
     */
    @Transactional(readOnly = true)
    public TimetableEntryDto getTimetableEntryById(Long id) {
        TimetableEntry entry = timetableEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Timetable entry not found with ID: " + id));
        return mapToDto(entry);
    }

    /**
     * Filter timetable entries by day, department, section, room, faculty, status, or search term.
     */
    @Transactional(readOnly = true)
    public List<TimetableEntryDto> filterTimetableEntries(
            DayOfWeek dayOfWeek,
            String department,
            String sectionName,
            Long roomId,
            Long facultyId,
            TimetableStatus status,
            String search
    ) {
        String deptClean = department != null && !department.isBlank() ? department.trim() : null;
        String secClean = sectionName != null && !sectionName.isBlank() ? sectionName.trim() : null;
        String searchClean = search != null && !search.isBlank() ? search.trim() : null;

        List<TimetableEntry> entries = timetableEntryRepository.filterTimetableEntries(
                dayOfWeek, deptClean, secClean, roomId, facultyId, status, searchClean
        );

        return entries.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Timetable schedule for a specific room.
     */
    @Transactional(readOnly = true)
    public List<TimetableEntryDto> getTimetableByRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Room not found with ID: " + roomId);
        }
        return timetableEntryRepository.findByRoomIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(roomId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Timetable schedule for a specific faculty member.
     */
    @Transactional(readOnly = true)
    public List<TimetableEntryDto> getTimetableByFaculty(Long facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new IllegalArgumentException("Faculty not found with ID: " + facultyId);
        }
        return timetableEntryRepository.findByFacultyIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(facultyId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Timetable schedule for a faculty member looked up by their user account ID.
     */
    @Transactional(readOnly = true)
    public List<TimetableEntryDto> getTimetableByFacultyUserId(Long userId) {
        return facultyRepository.findByUserId(userId)
                .map(f -> getTimetableByFaculty(f.getId()))
                .orElse(Collections.emptyList());
    }

    /**
     * Timetable schedule for a specific student cohort (department + section).
     */
    @Transactional(readOnly = true)
    public List<TimetableEntryDto> getTimetableByGroup(String department, String sectionName) {
        if (department == null || department.isBlank() || sectionName == null || sectionName.isBlank()) {
            throw new IllegalArgumentException("Department and section name are required.");
        }
        return timetableEntryRepository.findByDepartmentAndSectionNameAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
                department.trim(), sectionName.trim()
        ).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Identify unreserved, suitable physical rooms for a requested time interval.
     */
    @Transactional(readOnly = true)
    public List<AvailableRoomDto> findAvailableRooms(RoomAvailabilityRequest req) {
        validateTimeRange(req.getStartTime(), req.getEndTime());

        // 1. Retrieve all potential physical rooms matching basic criteria (capacity, type, campus, block)
        List<Room> candidates = roomRepository.filterRooms(
                req.getCampusId(),
                req.getBlockId(),
                null, // floorId
                req.getRoomType(),
                RoomStatus.AVAILABLE,
                req.getMinCapacity(),
                null // search
        );

        // 2. Identify all room IDs that have active conflicting allocations in this time interval
        List<Long> occupiedRoomIds = timetableEntryRepository.findOccupiedRoomIds(
                req.getDayOfWeek(),
                req.getScheduleDate(),
                req.getStartTime(),
                req.getEndTime()
        );

        // 3. Exclude occupied rooms and inactive/maintenance rooms
        return candidates.stream()
                .filter(Room::isActive)
                .filter(r -> r.getStatus() != RoomStatus.INACTIVE && r.getStatus() != RoomStatus.MAINTENANCE)
                .filter(r -> !occupiedRoomIds.contains(r.getId()))
                .map(this::mapToAvailableRoomDto)
                .collect(Collectors.toList());
    }

    /**
     * Compute summary statistics for timetable and room allocations.
     */
    @Transactional(readOnly = true)
    public TimetableStatsDto getTimetableStats() {
        List<TimetableEntry> allEntries = timetableEntryRepository.findAll();

        long totalEntries = allEntries.size();
        long scheduledEntries = allEntries.stream()
                .filter(e -> e.getStatus() == TimetableStatus.SCHEDULED && e.isActive()).count();
        long cancelledEntries = allEntries.stream()
                .filter(e -> e.getStatus() == TimetableStatus.CANCELLED || !e.isActive()).count();
        long allocatedRoomsCount = allEntries.stream()
                .filter(e -> e.getRoom() != null && e.getStatus() == TimetableStatus.SCHEDULED && e.isActive()).count();
        long unallocatedEntriesCount = allEntries.stream()
                .filter(e -> e.getRoom() == null && e.getStatus() == TimetableStatus.SCHEDULED && e.isActive()).count();

        double totalHours = allEntries.stream()
                .filter(e -> e.getStatus() == TimetableStatus.SCHEDULED && e.isActive())
                .mapToDouble(e -> Duration.between(e.getStartTime(), e.getEndTime()).toMinutes() / 60.0)
                .sum();

        return new TimetableStatsDto(
                totalEntries,
                scheduledEntries,
                cancelledEntries,
                allocatedRoomsCount,
                unallocatedEntriesCount,
                Math.round(totalHours * 10.0) / 10.0
        );
    }

    /**
     * Subject management: Create subject.
     */
    @Transactional
    public SubjectDto createSubject(CreateSubjectRequest req) {
        String code = req.getSubjectCode().trim().toUpperCase();
        if (subjectRepository.existsBySubjectCode(code)) {
            throw new IllegalArgumentException("Subject code '" + code + "' already exists.");
        }

        Subject subject = new Subject(
                code,
                req.getSubjectName().trim(),
                req.getDepartment().trim(),
                req.getSemester() > 0 ? req.getSemester() : 1,
                req.getSubjectType() != null ? req.getSubjectType() : SubjectType.THEORY,
                req.getCredits() > 0 ? req.getCredits() : 3
        );

        Subject saved = subjectRepository.save(subject);
        return mapSubjectToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SubjectDto> getAllSubjects() {
        return subjectRepository.findByActiveTrueOrderBySubjectCodeAsc()
                .stream().map(this::mapSubjectToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubjectDto> getSubjectsByDepartment(String department) {
        return subjectRepository.findByDepartmentOrderBySubjectCodeAsc(department)
                .stream().map(this::mapSubjectToDto).collect(Collectors.toList());
    }

    // =========================================================================
    // CONSTRAINT VERIFICATION ENGINE (HALF-OPEN INTERVAL [start, end))
    // =========================================================================

    public void verifySchedulingConstraints(
            Long currentEntryId,
            Room room,
            Faculty faculty,
            String department,
            String sectionName,
            DayOfWeek dayOfWeek,
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime,
            ClassType classType,
            int expectedCapacity,
            Subject subject
    ) {
        // 1. Time range check
        validateTimeRange(startTime, endTime);

        // 2. Room checks if room is assigned
        if (room != null) {
            verifyRoomConstraints(currentEntryId, room, dayOfWeek, scheduleDate, startTime, endTime, classType, expectedCapacity, subject);
        }

        // 3. Faculty conflict check if faculty is assigned
        if (faculty != null) {
            verifyFacultyConstraints(currentEntryId, faculty, dayOfWeek, scheduleDate, startTime, endTime);
        }

        // 4. Student Group conflict check
        if (department != null && !department.isBlank() && sectionName != null && !sectionName.isBlank()) {
            verifyGroupConstraints(currentEntryId, department, sectionName, dayOfWeek, scheduleDate, startTime, endTime);
        }
    }

    private void verifyRoomConstraints(
            Long currentEntryId,
            Room room,
            DayOfWeek dayOfWeek,
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime,
            ClassType classType,
            int expectedCapacity,
            Subject subject
    ) {
        if (!room.isActive() || room.getStatus() == RoomStatus.INACTIVE) {
            throw new IllegalStateException("Room '" + room.getRoomCode() + "' is inactive and cannot be allocated.");
        }
        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new IllegalStateException("Room '" + room.getRoomCode() + "' is currently under maintenance" +
                    (room.getMaintenanceReason() != null ? " (" + room.getMaintenanceReason() + ")" : "") + ".");
        }

        // Capacity validation
        if (room.getCapacity() < expectedCapacity) {
            throw new IllegalArgumentException("Selected room '" + room.getRoomCode() + "' capacity (" +
                    room.getCapacity() + ") is insufficient for expected student cohort size (" + expectedCapacity + ").");
        }

        // Room type validation: Laboratory sessions must use LABORATORY rooms
        boolean isLabSession = (classType == ClassType.LAB) ||
                (subject != null && subject.getSubjectType() == SubjectType.PRACTICAL);
        if (isLabSession && room.getRoomType() != RoomType.LABORATORY) {
            throw new IllegalArgumentException("A laboratory session requires a room of type LABORATORY. " +
                    "Selected room '" + room.getRoomCode() + "' is " + room.getRoomType() + ".");
        }

        // Interval overlap check: A_start < B_end AND B_start < A_end
        List<TimetableEntry> conflicts = timetableEntryRepository.findConflictingRoomEntries(
                room.getId(), dayOfWeek, scheduleDate, startTime, endTime, currentEntryId
        );

        if (!conflicts.isEmpty()) {
            TimetableEntry c = conflicts.get(0);
            throw new IllegalStateException(String.format(
                    "Room conflict: Room '%s' is already allocated for '%s' (%s - %s) on %s.",
                    room.getRoomCode(),
                    c.getSubject().getSubjectName(),
                    c.getStartTime(),
                    c.getEndTime(),
                    dayOfWeek
            ));
        }
    }

    private void verifyFacultyConstraints(
            Long currentEntryId,
            Faculty faculty,
            DayOfWeek dayOfWeek,
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        List<TimetableEntry> conflicts = timetableEntryRepository.findConflictingFacultyEntries(
                faculty.getId(), dayOfWeek, scheduleDate, startTime, endTime, currentEntryId
        );

        if (!conflicts.isEmpty()) {
            TimetableEntry c = conflicts.get(0);
            String roomDesc = c.getRoom() != null ? c.getRoom().getRoomCode() : "TBA";
            throw new IllegalStateException(String.format(
                    "Faculty conflict: %s is already assigned to another class ('%s' in %s, %s - %s) on %s.",
                    faculty.getUser().getFullName(),
                    c.getSubject().getSubjectName(),
                    roomDesc,
                    c.getStartTime(),
                    c.getEndTime(),
                    dayOfWeek
            ));
        }
    }

    private void verifyGroupConstraints(
            Long currentEntryId,
            String department,
            String sectionName,
            DayOfWeek dayOfWeek,
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        List<TimetableEntry> conflicts = timetableEntryRepository.findConflictingGroupEntries(
                department, sectionName, dayOfWeek, scheduleDate, startTime, endTime, currentEntryId
        );

        if (!conflicts.isEmpty()) {
            TimetableEntry c = conflicts.get(0);
            throw new IllegalStateException(String.format(
                    "Student group conflict: Section %s (%s) is already scheduled for '%s' (%s - %s) on %s.",
                    sectionName,
                    department,
                    c.getSubject().getSubjectName(),
                    c.getStartTime(),
                    c.getEndTime(),
                    dayOfWeek
            ));
        }
    }

    public static void validateTimeRange(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start time and end time are both required.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Schedule end time (" + end + ") must be after start time (" + start + ").");
        }
        if (Duration.between(start, end).toMinutes() < 15) {
            throw new IllegalArgumentException("Schedule slot must be at least 15 minutes long.");
        }
    }

    // =========================================================================
    // DTO MAPPERS
    // =========================================================================

    public TimetableEntryDto mapToDto(TimetableEntry e) {
        TimetableEntryDto dto = new TimetableEntryDto();
        dto.setId(e.getId());

        if (e.getSubject() != null) {
            dto.setSubjectId(e.getSubject().getId());
            dto.setSubjectCode(e.getSubject().getSubjectCode());
            dto.setSubjectName(e.getSubject().getSubjectName());
            dto.setCredits(e.getSubject().getCredits());
            dto.setSubjectType(e.getSubject().getSubjectType());
        }

        if (e.getFaculty() != null) {
            dto.setFacultyId(e.getFaculty().getId());
            dto.setFacultyName(e.getFaculty().getUser().getFullName());
            dto.setFacultyEmployeeId(e.getFaculty().getEmployeeId());
        }

        if (e.getRoom() != null) {
            Room r = e.getRoom();
            dto.setRoomId(r.getId());
            dto.setRoomCode(r.getRoomCode());
            dto.setRoomName(r.getRoomName());
            dto.setRoomType(r.getRoomType().name());
            dto.setRoomCapacity(r.getCapacity());

            if (r.getFloor() != null) {
                dto.setFloorNumber(r.getFloor().getFloorNumber());
                if (r.getFloor().getBlock() != null) {
                    dto.setBlockName(r.getFloor().getBlock().getName());
                    if (r.getFloor().getBlock().getCampus() != null) {
                        dto.setCampusName(r.getFloor().getBlock().getCampus().getName());
                    }
                }
            }
        }

        dto.setAcademicYear(e.getAcademicYear());
        dto.setSemester(e.getSemester());
        dto.setDepartment(e.getDepartment());
        dto.setSectionName(e.getSectionName());
        dto.setDayOfWeek(e.getDayOfWeek());
        dto.setScheduleDate(e.getScheduleDate());
        dto.setStartTime(e.getStartTime());
        dto.setEndTime(e.getEndTime());
        dto.setTimeSlot(e.getStartTime() + " - " + e.getEndTime());
        dto.setClassType(e.getClassType());
        dto.setStatus(e.getStatus());
        dto.setActive(e.isActive());
        dto.setExpectedCapacity(e.getExpectedCapacity());
        dto.setRemarks(e.getRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());

        return dto;
    }

    public AvailableRoomDto mapToAvailableRoomDto(Room r) {
        AvailableRoomDto dto = new AvailableRoomDto();
        dto.setId(r.getId());
        dto.setRoomCode(r.getRoomCode());
        dto.setRoomName(r.getRoomName());
        dto.setRoomType(r.getRoomType());
        dto.setCapacity(r.getCapacity());
        dto.setSystemCount(r.getSystemCount());
        dto.setStatus(r.getStatus());
        dto.setFacilities(r.getFacilities());

        if (r.getFloor() != null) {
            dto.setFloorId(r.getFloor().getId());
            dto.setFloorNumber(r.getFloor().getFloorNumber());
            if (r.getFloor().getBlock() != null) {
                dto.setBlockName(r.getFloor().getBlock().getName());
                if (r.getFloor().getBlock().getCampus() != null) {
                    dto.setCampusName(r.getFloor().getBlock().getCampus().getName());
                }
            }
        }

        return dto;
    }

    public SubjectDto mapSubjectToDto(Subject s) {
        SubjectDto dto = new SubjectDto();
        dto.setId(s.getId());
        dto.setSubjectCode(s.getSubjectCode());
        dto.setSubjectName(s.getSubjectName());
        dto.setDepartment(s.getDepartment());
        dto.setSemester(s.getSemester());
        dto.setSubjectType(s.getSubjectType());
        dto.setCredits(s.getCredits());
        dto.setActive(s.isActive());
        return dto;
    }

    /**
     * Seeds initial subjects and timetable entries if none exist.
     */
    @Transactional
    public void seedInitialSubjectsAndTimetable() {
        if (subjectRepository.count() > 0) {
            return;
        }

        // 1. Seed Core Academic Subjects
        Subject s1 = subjectRepository.save(new Subject("CS301", "Data Structures & Algorithms", "Computer Science", 3, SubjectType.THEORY, 4));
        Subject s2 = subjectRepository.save(new Subject("CS302", "Database Management Systems", "Computer Science", 3, SubjectType.THEORY, 4));
        Subject s3 = subjectRepository.save(new Subject("CS303", "Computer Networks", "Computer Science", 3, SubjectType.THEORY, 3));
        Subject s4 = subjectRepository.save(new Subject("CS304", "Operating Systems", "Computer Science", 3, SubjectType.THEORY, 4));
        Subject s5 = subjectRepository.save(new Subject("CS305P", "Database Management Lab", "Computer Science", 3, SubjectType.PRACTICAL, 2));
        Subject s6 = subjectRepository.save(new Subject("CS306P", "Data Structures Lab", "Computer Science", 3, SubjectType.PRACTICAL, 2));
        Subject s7 = subjectRepository.save(new Subject("EC201", "Digital Electronics", "Electronics & Communication", 3, SubjectType.THEORY, 4));
        Subject s8 = subjectRepository.save(new Subject("MA301", "Discrete Mathematics", "Computer Science", 3, SubjectType.THEORY, 4));

        // 2. Fetch seeded Faculty and Rooms
        List<Faculty> faculties = facultyRepository.findAll();
        Faculty fac1 = faculties.size() > 0 ? faculties.get(0) : null;
        Faculty fac2 = faculties.size() > 1 ? faculties.get(1) : fac1;

        List<Room> classrooms = roomRepository.filterRooms(null, null, null, RoomType.CLASSROOM, RoomStatus.AVAILABLE, 30, null);
        List<Room> labs = roomRepository.filterRooms(null, null, null, RoomType.LABORATORY, RoomStatus.AVAILABLE, 20, null);

        Room rClass1 = !classrooms.isEmpty() ? classrooms.get(0) : null;
        Room rClass2 = classrooms.size() > 1 ? classrooms.get(1) : rClass1;
        Room rLab1 = !labs.isEmpty() ? labs.get(0) : null;

        if (timetableEntryRepository.count() == 0 && rClass1 != null) {
            // Seed sample weekly timetable for Section CSE-A
            // Monday
            createEntryQuietly(s1, fac1, rClass1, "Computer Science", "CSE-A", 2, 3, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);
            createEntryQuietly(s2, fac2, rClass1, "Computer Science", "CSE-A", 2, 3, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), ClassType.LECTURE, 40);
            if (rLab1 != null) {
                createEntryQuietly(s5, fac1, rLab1, "Computer Science", "CSE-A", 2, 3, DayOfWeek.MONDAY, LocalTime.of(11, 30), LocalTime.of(13, 0), ClassType.LAB, 30);
            }

            // Tuesday
            createEntryQuietly(s3, fac2, rClass1, "Computer Science", "CSE-A", 2, 3, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);
            createEntryQuietly(s4, fac1, rClass1, "Computer Science", "CSE-A", 2, 3, DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), ClassType.LECTURE, 40);

            // Wednesday
            createEntryQuietly(s1, fac1, rClass2, "Computer Science", "CSE-A", 2, 3, DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);
            createEntryQuietly(s2, fac2, rClass2, "Computer Science", "CSE-A", 2, 3, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), ClassType.LECTURE, 40);

            // Thursday
            createEntryQuietly(s8, fac2, rClass1, "Computer Science", "CSE-A", 2, 3, DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);
            createEntryQuietly(s3, fac1, rClass1, "Computer Science", "CSE-A", 2, 3, DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), ClassType.LECTURE, 40);

            // Friday
            createEntryQuietly(s4, fac1, rClass2, "Computer Science", "CSE-A", 2, 3, DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);
            if (rLab1 != null) {
                createEntryQuietly(s6, fac2, rLab1, "Computer Science", "CSE-A", 2, 3, DayOfWeek.FRIDAY, LocalTime.of(11, 30), LocalTime.of(13, 0), ClassType.LAB, 30);
            }
        }
    }

    private void createEntryQuietly(Subject s, Faculty f, Room r, String dept, String sec, int year, int sem,
                                    DayOfWeek day, LocalTime start, LocalTime end, ClassType type, int cap) {
        try {
            TimetableEntry e = new TimetableEntry(s, f, r, dept, sec, year, sem, day, start, end, type, cap);
            timetableEntryRepository.save(e);
        } catch (Exception ex) {
            // Ignore seeding conflict if any
        }
    }
}
