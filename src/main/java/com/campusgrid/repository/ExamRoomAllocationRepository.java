package com.campusgrid.repository;

import com.campusgrid.model.ExamRoomAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRoomAllocationRepository extends JpaRepository<ExamRoomAllocation, Long> {

    List<ExamRoomAllocation> findByExamSessionId(Long examSessionId);

    Optional<ExamRoomAllocation> findByExamSessionIdAndRoomId(Long examSessionId, Long roomId);

    boolean existsByExamSessionIdAndRoomId(Long examSessionId, Long roomId);

    void deleteByExamSessionId(Long examSessionId);

    List<ExamRoomAllocation> findByInvigilatorId(Long invigilatorId);

    @Query("SELECT r FROM ExamRoomAllocation r WHERE r.room.id = :roomId AND " +
           "r.examSession.id != :excludeExamId AND " +
           "r.examSession.examDate = :examDate AND " +
           "((r.examSession.startTime < :endTime AND r.examSession.endTime > :startTime))")
    List<ExamRoomAllocation> findConflictingRoomAllocations(
            @Param("roomId") Long roomId,
            @Param("examDate") LocalDate examDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeExamId") Long excludeExamId
    );

    @Query("SELECT r FROM ExamRoomAllocation r WHERE r.invigilator.id = :invigilatorId AND " +
           "r.examSession.id != :excludeExamId AND " +
           "r.examSession.examDate = :examDate AND " +
           "((r.examSession.startTime < :endTime AND r.examSession.endTime > :startTime))")
    List<ExamRoomAllocation> findConflictingInvigilatorAllocations(
            @Param("invigilatorId") Long invigilatorId,
            @Param("examDate") LocalDate examDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeExamId") Long excludeExamId
    );
}
