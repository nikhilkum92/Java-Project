package com.campusgrid.repository;

import com.campusgrid.model.PhysicalSeat;
import com.campusgrid.model.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhysicalSeatRepository extends JpaRepository<PhysicalSeat, Long> {

    List<PhysicalSeat> findByRoomIdOrderByRowNumberAscColumnNumberAsc(Long roomId);

    @Modifying
    @Query("DELETE FROM PhysicalSeat s WHERE s.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);

    long countByRoomId(Long roomId);

    long countByRoomIdAndStatus(Long roomId, SeatStatus status);

    boolean existsByRoomIdAndSeatNumber(Long roomId, String seatNumber);
}
