package com.campusgrid.repository;

import com.campusgrid.model.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findBySectionNameIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(String sectionName);

    List<TimetableEntry> findAllByOrderByDayOfWeekAscStartTimeAsc();

    @Query("SELECT DISTINCT t.sectionName FROM TimetableEntry t ORDER BY t.sectionName ASC")
    List<String> findDistinctSections();

    long countBySectionNameIgnoreCase(String sectionName);
}
