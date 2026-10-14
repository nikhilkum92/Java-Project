package com.campusgrid.repository;

import com.campusgrid.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findBySubjectCode(String subjectCode);

    boolean existsBySubjectCode(String subjectCode);

    List<Subject> findByDepartmentOrderBySubjectCodeAsc(String department);

    List<Subject> findByDepartmentAndSemesterOrderBySubjectCodeAsc(String department, int semester);

    List<Subject> findByActiveTrueOrderBySubjectCodeAsc();
}
