package com.campusgrid.repository;

import com.campusgrid.model.Room;
import com.campusgrid.model.RoomStatus;
import com.campusgrid.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByFloorIdOrderByRoomCodeAsc(Long floorId);

    boolean existsByRoomCode(String roomCode);

    Optional<Room> findByRoomCode(String roomCode);

    long countByFloorId(Long floorId);

    long countByRoomType(RoomType roomType);

    long countByStatus(RoomStatus status);

    @Query("SELECT COALESCE(SUM(r.capacity), 0) FROM Room r WHERE r.active = true")
    long sumTotalCapacity();

    @Query("SELECT r FROM Room r " +
            "JOIN FETCH r.floor f " +
            "JOIN FETCH f.block b " +
            "JOIN FETCH b.campus c " +
            "WHERE (:campusId IS NULL OR c.id = :campusId) " +
            "AND (:blockId IS NULL OR b.id = :blockId) " +
            "AND (:floorId IS NULL OR f.id = :floorId) " +
            "AND (:roomType IS NULL OR r.roomType = :roomType) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:minCapacity IS NULL OR r.capacity >= :minCapacity) " +
            "AND (:search IS NULL OR LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(r.roomName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(r.facilities) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY r.roomCode ASC")
    List<Room> filterRooms(
            @Param("campusId") Long campusId,
            @Param("blockId") Long blockId,
            @Param("floorId") Long floorId,
            @Param("roomType") RoomType roomType,
            @Param("status") RoomStatus status,
            @Param("minCapacity") Integer minCapacity,
            @Param("search") String search
    );
}
