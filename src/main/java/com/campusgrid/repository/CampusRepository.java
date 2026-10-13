package com.campusgrid.repository;

import com.campusgrid.model.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {

    boolean existsByName(String name);

    boolean existsByCode(String code);

    Optional<Campus> findByCode(String code);

    List<Campus> findAllByOrderByCreatedAtDesc();

    List<Campus> findAllByActiveTrueOrderByNameAsc();
}
