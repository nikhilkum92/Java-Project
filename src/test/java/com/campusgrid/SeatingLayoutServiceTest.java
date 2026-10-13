package com.campusgrid;

import com.campusgrid.dto.ConfigureLayoutRequest;
import com.campusgrid.dto.SeatDto;
import com.campusgrid.dto.SeatingGridDto;
import com.campusgrid.model.*;
import com.campusgrid.repository.PhysicalSeatRepository;
import com.campusgrid.repository.RoomRepository;
import com.campusgrid.service.SeatingLayoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatingLayoutServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PhysicalSeatRepository seatRepository;

    @InjectMocks
    private SeatingLayoutService seatingLayoutService;

    private Room sampleRoom;

    @BeforeEach
    void setUp() {
        Floor dummyFloor = new Floor(new Block(new Campus("Main", "MC", "Addr"), "Block A", "BLK-A"), 1, "Floor 1");
        sampleRoom = new Room(dummyFloor, "A-101", "Lecture Room", RoomType.CLASSROOM, 40, "Projector");
        sampleRoom.setId(10L);
    }

    @Test
    @DisplayName("Should generate standard grid layout successfully within room capacity")
    void testStandardGridGeneration() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(sampleRoom));
        when(seatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(10L)).thenReturn(new ArrayList<>());

        ConfigureLayoutRequest req = new ConfigureLayoutRequest(LayoutTemplate.STANDARD_GRID, 5, 6, null);
        SeatingGridDto result = seatingLayoutService.configureAndGenerateLayout(10L, req);

        assertNotNull(result);
        assertEquals(5, sampleRoom.getRowsCount());
        assertEquals(6, sampleRoom.getColumnsCount());
        assertEquals(LayoutTemplate.STANDARD_GRID, sampleRoom.getLayoutTemplate());

        verify(seatRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should preserve compatible seat IDs, identifiers, and custom statuses when regenerating layout")
    void testPreserveExistingSeatsOnRegeneration() {
        PhysicalSeat existingBlockedSeat = new PhysicalSeat(sampleRoom, "A-01", 1, 1, "A", SeatType.REGULAR, SeatStatus.BLOCKED);
        existingBlockedSeat.setId(201L);

        PhysicalSeat existingMaintSeat = new PhysicalSeat(sampleRoom, "A-02", 1, 2, "A", SeatType.REGULAR, SeatStatus.MAINTENANCE);
        existingMaintSeat.setId(202L);

        List<PhysicalSeat> currentSeats = List.of(existingBlockedSeat, existingMaintSeat);

        when(roomRepository.findById(10L)).thenReturn(Optional.of(sampleRoom));
        when(seatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(10L)).thenReturn(currentSeats);

        // Regenerate 2 rows x 3 cols Standard Grid
        ConfigureLayoutRequest req = new ConfigureLayoutRequest(LayoutTemplate.STANDARD_GRID, 2, 3, null);
        seatingLayoutService.configureAndGenerateLayout(10L, req);

        verify(seatRepository).saveAll(argThat(seats -> {
            List<PhysicalSeat> seatList = (List<PhysicalSeat>) seats;
            assertEquals(6, seatList.size()); // 2x3 = 6 seats

            // Verify existing seats preserved
            PhysicalSeat preserved1 = seatList.stream().filter(s -> s.getRowNumber() == 1 && s.getColumnNumber() == 1).findFirst().orElse(null);
            assertNotNull(preserved1);
            assertEquals(201L, preserved1.getId());
            assertEquals(SeatStatus.BLOCKED, preserved1.getStatus());

            PhysicalSeat preserved2 = seatList.stream().filter(s -> s.getRowNumber() == 1 && s.getColumnNumber() == 2).findFirst().orElse(null);
            assertNotNull(preserved2);
            assertEquals(202L, preserved2.getId());
            assertEquals(SeatStatus.MAINTENANCE, preserved2.getStatus());

            // Verify newly added seat has AVAILABLE status
            PhysicalSeat newSeat = seatList.stream().filter(s -> s.getRowNumber() == 2 && s.getColumnNumber() == 3).findFirst().orElse(null);
            assertNotNull(newSeat);
            assertNull(newSeat.getId());
            assertEquals(SeatStatus.AVAILABLE, newSeat.getStatus());

            return true;
        }));
    }

    @Test
    @DisplayName("Should exclude central aisle column from physical seats for CENTRAL_AISLE template")
    void testCentralAisleGapExclusion() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(sampleRoom));
        when(seatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(10L)).thenReturn(new ArrayList<>());

        // 4 rows x 5 cols with CENTRAL_AISLE -> central col is (5/2)+1 = 3
        // Non-aisle cols per row: 4 cols. Total physical seats: 4 * 4 = 16
        ConfigureLayoutRequest req = new ConfigureLayoutRequest(LayoutTemplate.CENTRAL_AISLE, 4, 5, null);
        seatingLayoutService.configureAndGenerateLayout(10L, req);

        verify(seatRepository).saveAll(argThat(seats -> {
            List<PhysicalSeat> seatList = (List<PhysicalSeat>) seats;
            assertEquals(16, seatList.size());
            // Confirm none has columnNumber == 3
            assertTrue(seatList.stream().noneMatch(s -> s.getColumnNumber() == 3));
            return true;
        }));
    }

    @Test
    @DisplayName("Should exclude walkways from physical seats for H_PATTERN template")
    void testHPatternGapExclusion() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(sampleRoom));
        when(seatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(10L)).thenReturn(new ArrayList<>());

        // 4 rows x 5 cols H_PATTERN:
        // Aisle row = (4/2)+1 = 3 (skips row 3)
        // Aisle col = (5/2)+1 = 3 (skips col 3)
        // Valid rows: 1, 2, 4 (3 rows). Valid cols: 1, 2, 4, 5 (4 cols). Total: 3 * 4 = 12 seats.
        ConfigureLayoutRequest req = new ConfigureLayoutRequest(LayoutTemplate.H_PATTERN, 4, 5, null);
        seatingLayoutService.configureAndGenerateLayout(10L, req);

        verify(seatRepository).saveAll(argThat(seats -> {
            List<PhysicalSeat> seatList = (List<PhysicalSeat>) seats;
            assertEquals(12, seatList.size());
            assertTrue(seatList.stream().noneMatch(s -> s.getRowNumber() == 3));
            assertTrue(seatList.stream().noneMatch(s -> s.getColumnNumber() == 3));
            return true;
        }));
    }

    @Test
    @DisplayName("Should reject layout configuration when generated physical seats exceed declared room capacity")
    void testExceedCapacityRejection() {
        sampleRoom.setCapacity(15); // Room capacity is only 15
        when(roomRepository.findById(10L)).thenReturn(Optional.of(sampleRoom));

        // 5 rows x 5 cols standard grid = 25 seats > 15 capacity
        ConfigureLayoutRequest req = new ConfigureLayoutRequest(LayoutTemplate.STANDARD_GRID, 5, 5, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                seatingLayoutService.configureAndGenerateLayout(10L, req)
        );

        assertTrue(ex.getMessage().contains("exceeds the declared room capacity of 15"));
        verify(seatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should update individual seat status safely")
    void testUpdateSeatStatus() {
        PhysicalSeat seat = new PhysicalSeat(sampleRoom, "A-01", 1, 1, "A", SeatType.REGULAR, SeatStatus.AVAILABLE);
        seat.setId(101L);

        when(seatRepository.findById(101L)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(PhysicalSeat.class))).thenAnswer(i -> i.getArgument(0));

        SeatDto updated = seatingLayoutService.updateSeatStatus(10L, 101L, SeatStatus.BLOCKED);

        assertEquals(SeatStatus.BLOCKED, updated.getStatus());
        assertEquals("A-01", updated.getSeatNumber());
    }
}
