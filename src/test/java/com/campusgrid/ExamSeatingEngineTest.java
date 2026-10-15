package com.campusgrid;

import com.campusgrid.model.*;
import com.campusgrid.service.ExamSeatingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExamSeatingEngineTest {

    private ExamSeatingEngine engine;
    private Room room101;
    private Room room102;
    private Student studentA1;
    private Student studentA2;
    private Student studentB1;
    private Student studentB2;

    @BeforeEach
    void setUp() {
        engine = new ExamSeatingEngine();

        room101 = new Room(null, "A-101", "Hall 1", RoomType.CLASSROOM, 40, "Standard Hall");
        room101.setId(101L);

        room102 = new Room(null, "A-102", "Hall 2", RoomType.CLASSROOM, 40, "Standard Hall");
        room102.setId(102L);

        studentA1 = new Student(null, "23CSE001", "Computer Science", "CSE-A", 2, 3, "9000000001");
        studentA1.setId(1L);

        studentA2 = new Student(null, "23CSE003", "Computer Science", "CSE-A", 2, 3, "9000000002");
        studentA2.setId(2L);

        studentB1 = new Student(null, "23CSE002", "Computer Science", "CSE-B", 2, 3, "9000000003");
        studentB1.setId(3L);

        studentB2 = new Student(null, "23CSE004", "Computer Science", "CSE-B", 2, 3, "9000000004");
        studentB2.setId(4L);
    }

    @Test
    @DisplayName("Should return empty list when candidates or rooms are empty")
    void testEmptyInputsReturnEmptyList() {
        assertTrue(engine.computeSeatingArrangement(Collections.emptyList(), Map.of(room101, List.of()), SeatingStrategy.ROLL_NUMBER_ASCENDING, 0).isEmpty());
        assertTrue(engine.computeSeatingArrangement(List.of(studentA1), Collections.emptyMap(), SeatingStrategy.ROLL_NUMBER_ASCENDING, 0).isEmpty());
    }

    @Test
    @DisplayName("Should allocate candidates stably when candidates are fewer than seats")
    void testFewerCandidatesThanSeats() {
        PhysicalSeat s1 = new PhysicalSeat(room101, "R1-C1", 1, 1, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        PhysicalSeat s2 = new PhysicalSeat(room101, "R1-C2", 1, 2, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        PhysicalSeat s3 = new PhysicalSeat(room101, "R1-C3", 1, 3, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);

        var assignments = engine.computeSeatingArrangement(
                List.of(studentA1, studentB1),
                Map.of(room101, List.of(s1, s2, s3)),
                SeatingStrategy.ROLL_NUMBER_ASCENDING,
                0
        );

        assertEquals(2, assignments.size());
        assertEquals("23CSE001", assignments.get(0).getStudent().getRollNumber());
        assertEquals("R1-C1", assignments.get(0).getSeat().getSeatNumber());
        assertEquals("23CSE002", assignments.get(1).getStudent().getRollNumber());
        assertEquals("R1-C2", assignments.get(1).getSeat().getSeatNumber());
    }

    @Test
    @DisplayName("Should allocate all seats exactly when candidates equal usable seats")
    void testExactCapacityAllocation() {
        PhysicalSeat s1 = new PhysicalSeat(room101, "R1-C1", 1, 1, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        PhysicalSeat s2 = new PhysicalSeat(room101, "R1-C2", 1, 2, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);

        var assignments = engine.computeSeatingArrangement(
                List.of(studentA1, studentB1),
                Map.of(room101, List.of(s1, s2)),
                SeatingStrategy.ROLL_NUMBER_ASCENDING,
                0
        );

        assertEquals(2, assignments.size());
    }

    @Test
    @DisplayName("Should throw IllegalStateException with shortfall details when candidates exceed usable capacity")
    void testInsufficientCapacityShortfall() {
        PhysicalSeat s1 = new PhysicalSeat(room101, "R1-C1", 1, 1, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                engine.computeSeatingArrangement(
                        List.of(studentA1, studentB1, studentA2),
                        Map.of(room101, List.of(s1)),
                        SeatingStrategy.ROLL_NUMBER_ASCENDING,
                        0
                )
        );

        assertTrue(ex.getMessage().contains("Insufficient seating capacity!"));
        assertTrue(ex.getMessage().contains("Eligible candidates: 3"));
        assertTrue(ex.getMessage().contains("Usable seats available: 1"));
    }

    @Test
    @DisplayName("Should strictly exclude BLOCKED, MAINTENANCE, and AISLE_GAP seats")
    void testExcludesUnavailableSeats() {
        PhysicalSeat s1 = new PhysicalSeat(room101, "R1-C1", 1, 1, "R1", SeatType.REGULAR, SeatStatus.BLOCKED);
        PhysicalSeat s2 = new PhysicalSeat(room101, "R1-C2", 1, 2, "R1", SeatType.REGULAR, SeatStatus.MAINTENANCE);
        PhysicalSeat s3 = new PhysicalSeat(room101, "R1-C3", 1, 3, "R1", SeatType.AISLE_GAP, SeatStatus.AVAILABLE);
        PhysicalSeat s4 = new PhysicalSeat(room101, "R1-C4", 1, 4, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);

        var assignments = engine.computeSeatingArrangement(
                List.of(studentA1),
                Map.of(room101, List.of(s1, s2, s3, s4)),
                SeatingStrategy.ROLL_NUMBER_ASCENDING,
                0
        );

        assertEquals(1, assignments.size());
        assertEquals("R1-C4", assignments.get(0).getSeat().getSeatNumber());
        assertEquals(SeatStatus.AVAILABLE, assignments.get(0).getSeat().getStatus());
    }

    @Test
    @DisplayName("Should interleave sections deterministically for ALTERNATING_SECTION strategy to prevent cheating")
    void testAntiCheatingAlternatingSection() {
        PhysicalSeat s1 = new PhysicalSeat(room101, "R1-C1", 1, 1, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        PhysicalSeat s2 = new PhysicalSeat(room101, "R1-C2", 1, 2, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        PhysicalSeat s3 = new PhysicalSeat(room101, "R1-C3", 1, 3, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        PhysicalSeat s4 = new PhysicalSeat(room101, "R1-C4", 1, 4, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);

        // Raw list has A1, A2, B1, B2
        var assignments = engine.computeSeatingArrangement(
                List.of(studentA1, studentA2, studentB1, studentB2),
                Map.of(room101, List.of(s1, s2, s3, s4)),
                SeatingStrategy.ALTERNATING_SECTION,
                0
        );

        assertEquals(4, assignments.size());
        // Should alternate sections: CSE-A, CSE-B, CSE-A, CSE-B
        assertEquals("CSE-A", assignments.get(0).getStudent().getSectionName());
        assertEquals("CSE-B", assignments.get(1).getStudent().getSectionName());
        assertEquals("CSE-A", assignments.get(2).getStudent().getSectionName());
        assertEquals("CSE-B", assignments.get(3).getStudent().getSectionName());
    }

    @Test
    @DisplayName("Should apply spaced gap strategy by skipping alternate seats")
    void testSpacedGapStrategy() {
        PhysicalSeat s1 = new PhysicalSeat(room101, "R1-C1", 1, 1, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        PhysicalSeat s2 = new PhysicalSeat(room101, "R1-C2", 1, 2, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        PhysicalSeat s3 = new PhysicalSeat(room101, "R1-C3", 1, 3, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        PhysicalSeat s4 = new PhysicalSeat(room101, "R1-C4", 1, 4, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);

        var assignments = engine.computeSeatingArrangement(
                List.of(studentA1, studentA2),
                Map.of(room101, List.of(s1, s2, s3, s4)),
                SeatingStrategy.SPACED_GAP,
                1
        );

        assertEquals(2, assignments.size());
        // Seat index 0 (s1) and index 2 (s3) should be assigned, skipping index 1 (s2)
        assertEquals("R1-C1", assignments.get(0).getSeat().getSeatNumber());
        assertEquals("R1-C3", assignments.get(1).getSeat().getSeatNumber());
    }

    @Test
    @DisplayName("Should allocate seats stably across multiple rooms ordered by room code")
    void testMultiRoomAllocation() {
        PhysicalSeat s101 = new PhysicalSeat(room101, "R1-C1", 1, 1, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);
        PhysicalSeat s102 = new PhysicalSeat(room102, "R1-C1", 1, 1, "R1", SeatType.REGULAR, SeatStatus.AVAILABLE);

        Map<Room, List<PhysicalSeat>> map = new LinkedHashMap<>();
        map.put(room102, List.of(s102));
        map.put(room101, List.of(s101));

        var assignments = engine.computeSeatingArrangement(
                List.of(studentA1, studentB1),
                map,
                SeatingStrategy.ROLL_NUMBER_ASCENDING,
                0
        );

        assertEquals(2, assignments.size());
        // Room A-101 comes before A-102 alphabetically
        assertEquals("A-101", assignments.get(0).getRoom().getRoomCode());
        assertEquals("A-102", assignments.get(1).getRoom().getRoomCode());
    }
}
