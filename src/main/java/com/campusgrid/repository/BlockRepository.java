package com.campusgrid.repository;

import com.campusgrid.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    List<Block> findByCampusIdOrderByCodeAsc(Long campusId);

    boolean existsByCampusIdAndCode(Long campusId, String code);

    boolean existsByCampusIdAndName(Long campusId, String name);

    Optional<Block> findByCampusIdAndCode(Long campusId, String code);

    long countByCampusId(Long campusId);
}
