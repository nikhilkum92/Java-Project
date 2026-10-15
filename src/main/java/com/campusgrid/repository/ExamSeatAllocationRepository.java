package com.campusgrid.repository;

import com.campusgrid.model.ExamSeatAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSeatAllocationRepository extends JpaRepository<ExamSeatAllocation, Long> {

    List<ExamSeatAllocation> findByExamSessionId(Long examSessionId);

    List<ExamSeatAllocation> findByExamSessionIdAndRoomIdOrderByRowNumberAscColumnNumberAsc(Long examSessionId, Long roomId);

    List<ExamSeatAllocation> findByStudentId(Long studentId);

    Optional<ExamSeatAllocation> findByStudentIdAndExamSessionId(Long studentId, Long examSessionId);

    @Query("SELECT s FROM ExamSeatAllocation s WHERE s.student.user.id = :userId ORDER BY s.examSession.examDate ASC, s.examSession.startTime ASC")
    List<ExamSeatAllocation> findByStudentUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM ExamSeatAllocation s WHERE s.student.rollNumber = :rollNumber ORDER BY s.examSession.examDate ASC, s.examSession.startTime ASC")
    List<ExamSeatAllocation> findByStudentRollNumber(@Param("rollNumber") String rollNumber);

    long countByExamSessionId(Long examSessionId);

    long countByExamSessionIdAndRoomId(Long examSessionId, Long roomId);

    void deleteByExamSessionId(Long examSessionId);

    void deleteByExamSessionIdAndRoomId(Long examSessionId, Long roomId);

    boolean existsByExamSessionIdAndStudentId(Long examSessionId, Long studentId);

    boolean existsByExamSessionIdAndPhysicalSeatId(Long examSessionId, Long physicalSeatId);
}
