package com.campusgrid.repository;

import com.campusgrid.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByRollNumber(String rollNumber);

    boolean existsByRollNumber(String rollNumber);

    List<Student> findBySectionName(String sectionName);

    List<Student> findByDepartment(String department);

    List<Student> findByDepartmentAndSectionName(String department, String sectionName);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s.department FROM Student s WHERE s.department IS NOT NULL ORDER BY s.department")
    List<String> findDistinctDepartments();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s.sectionName FROM Student s WHERE s.sectionName IS NOT NULL ORDER BY s.sectionName")
    List<String> findDistinctSectionNames();
}

