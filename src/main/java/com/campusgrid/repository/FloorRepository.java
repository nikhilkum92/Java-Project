package com.campusgrid.repository;

import com.campusgrid.model.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {

    List<Floor> findByBlockIdOrderByFloorNumberAsc(Long blockId);

    boolean existsByBlockIdAndFloorNumber(Long blockId, int floorNumber);

    Optional<Floor> findByBlockIdAndFloorNumber(Long blockId, int floorNumber);

    long countByBlockId(Long blockId);
}
