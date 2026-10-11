package com.campusgrid.repository;

import com.campusgrid.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByUserId(Long userId);

    Optional<Faculty> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    List<Faculty> findByDepartment(String department);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT f.department FROM Faculty f WHERE f.department IS NOT NULL ORDER BY f.department")
    List<String> findDistinctDepartments();
}

