package com.campusgrid.repository;

import com.campusgrid.model.ExamSession;
import com.campusgrid.model.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

    Optional<ExamSession> findByExamCode(String examCode);

    boolean existsByExamCode(String examCode);

    List<ExamSession> findByDepartmentAndSemesterOrderByExamDateAscStartTimeAsc(String department, int semester);

    List<ExamSession> findByExamDateOrderByStartTimeAsc(LocalDate examDate);

    List<ExamSession> findByStatus(ExamStatus status);

    @Query("SELECT e FROM ExamSession e WHERE e.examDate >= :currentDate ORDER BY e.examDate ASC, e.startTime ASC")
    List<ExamSession> findUpcomingExams(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT e FROM ExamSession e WHERE " +
           "(:department IS NULL OR e.department = :department) AND " +
           "(:semester IS NULL OR e.semester = :semester) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.examCode) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.courseCode) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY e.examDate ASC, e.startTime ASC")
    List<ExamSession> filterExams(
            @Param("department") String department,
            @Param("semester") Integer semester,
            @Param("status") ExamStatus status,
            @Param("search") String search
    );

    @Query("SELECT e FROM ExamSession e WHERE e.examDate = :examDate AND " +
           "e.id != :excludeId AND " +
           "((e.startTime < :endTime AND e.endTime > :startTime))")
    List<ExamSession> findOverlappingSessions(
            @Param("examDate") LocalDate examDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
