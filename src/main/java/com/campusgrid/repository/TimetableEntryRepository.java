package com.campusgrid.repository;

import com.campusgrid.model.TimetableEntry;
import com.campusgrid.model.TimetableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {

    @Query("SELECT t FROM TimetableEntry t WHERE t.room.id = :roomId " +
            "AND t.status = com.campusgrid.model.TimetableStatus.SCHEDULED AND t.active = true " +
            "AND t.dayOfWeek = :dayOfWeek " +
            "AND (:scheduleDate IS NULL OR t.scheduleDate IS NULL OR t.scheduleDate = :scheduleDate) " +
            "AND t.startTime < :endTime AND :startTime < t.endTime " +
            "AND (:excludeId IS NULL OR t.id != :excludeId)")
    List<TimetableEntry> findConflictingRoomEntries(
            @Param("roomId") Long roomId,
            @Param("dayOfWeek") java.time.DayOfWeek dayOfWeek,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT t FROM TimetableEntry t WHERE t.faculty.id = :facultyId " +
            "AND t.status = com.campusgrid.model.TimetableStatus.SCHEDULED AND t.active = true " +
            "AND t.dayOfWeek = :dayOfWeek " +
            "AND (:scheduleDate IS NULL OR t.scheduleDate IS NULL OR t.scheduleDate = :scheduleDate) " +
            "AND t.startTime < :endTime AND :startTime < t.endTime " +
            "AND (:excludeId IS NULL OR t.id != :excludeId)")
    List<TimetableEntry> findConflictingFacultyEntries(
            @Param("facultyId") Long facultyId,
            @Param("dayOfWeek") java.time.DayOfWeek dayOfWeek,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT t FROM TimetableEntry t WHERE LOWER(t.department) = LOWER(:department) " +
            "AND LOWER(t.sectionName) = LOWER(:sectionName) " +
            "AND t.status = com.campusgrid.model.TimetableStatus.SCHEDULED AND t.active = true " +
            "AND t.dayOfWeek = :dayOfWeek " +
            "AND (:scheduleDate IS NULL OR t.scheduleDate IS NULL OR t.scheduleDate = :scheduleDate) " +
            "AND t.startTime < :endTime AND :startTime < t.endTime " +
            "AND (:excludeId IS NULL OR t.id != :excludeId)")
    List<TimetableEntry> findConflictingGroupEntries(
            @Param("department") String department,
            @Param("sectionName") String sectionName,
            @Param("dayOfWeek") java.time.DayOfWeek dayOfWeek,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT DISTINCT t.room.id FROM TimetableEntry t WHERE t.room IS NOT NULL " +
            "AND t.status = com.campusgrid.model.TimetableStatus.SCHEDULED AND t.active = true " +
            "AND t.dayOfWeek = :dayOfWeek " +
            "AND (:scheduleDate IS NULL OR t.scheduleDate IS NULL OR t.scheduleDate = :scheduleDate) " +
            "AND t.startTime < :endTime AND :startTime < t.endTime")
    List<Long> findOccupiedRoomIds(
            @Param("dayOfWeek") java.time.DayOfWeek dayOfWeek,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    List<TimetableEntry> findByDepartmentAndSectionNameAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
            String department, String sectionName
    );

    List<TimetableEntry> findByFacultyIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
            Long facultyId
    );

    List<TimetableEntry> findByRoomIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
            Long roomId
    );

    @Query("SELECT t FROM TimetableEntry t " +
            "JOIN FETCH t.subject s " +
            "LEFT JOIN FETCH t.faculty f " +
            "LEFT JOIN FETCH f.user fu " +
            "LEFT JOIN FETCH t.room r " +
            "LEFT JOIN FETCH r.floor fl " +
            "LEFT JOIN FETCH fl.block b " +
            "WHERE (:dayOfWeek IS NULL OR t.dayOfWeek = :dayOfWeek) " +
            "AND (:department IS NULL OR LOWER(t.department) = LOWER(:department)) " +
            "AND (:sectionName IS NULL OR LOWER(t.sectionName) = LOWER(:sectionName)) " +
            "AND (:roomId IS NULL OR t.room.id = :roomId) " +
            "AND (:facultyId IS NULL OR t.faculty.id = :facultyId) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:search IS NULL OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(t.sectionName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY t.dayOfWeek ASC, t.startTime ASC")
    List<TimetableEntry> filterTimetableEntries(
            @Param("dayOfWeek") java.time.DayOfWeek dayOfWeek,
            @Param("department") String department,
            @Param("sectionName") String sectionName,
            @Param("roomId") Long roomId,
            @Param("facultyId") Long facultyId,
            @Param("status") TimetableStatus status,
            @Param("search") String search
    );

    long countByStatusAndActiveTrue(TimetableStatus status);

    long countByRoomIsNotNullAndActiveTrue();

    long countByRoomIsNullAndActiveTrue();
}
