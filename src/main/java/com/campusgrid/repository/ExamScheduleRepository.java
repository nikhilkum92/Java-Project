package com.campusgrid.repository;

import com.campusgrid.model.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    List<ExamSchedule> findBySectionNameIgnoreCaseOrderByExamDateAscStartTimeAsc(String sectionName);

    List<ExamSchedule> findAllByOrderByExamDateAscStartTimeAsc();

    @Query("SELECT DISTINCT e.sectionName FROM ExamSchedule e ORDER BY e.sectionName ASC")
    List<String> findDistinctSections();

    long countBySectionNameIgnoreCase(String sectionName);
}
