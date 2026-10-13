package com.campusgrid;

import com.campusgrid.dto.*;
import com.campusgrid.model.*;
import com.campusgrid.repository.*;
import com.campusgrid.service.SeatingLayoutService;
import com.campusgrid.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private CampusRepository campusRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private FloorRepository floorRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PhysicalSeatRepository seatRepository;

    @Mock
    private SeatingLayoutService seatingLayoutService;

    @InjectMocks
    private SpaceService spaceService;

    private Campus sampleCampus;
    private Block sampleBlock;
    private Floor sampleFloor;
    private Room sampleRoom;

    @BeforeEach
    void setUp() {
        sampleCampus = new Campus("Engineering Campus", "ENG", "East Wing Road");
        sampleCampus.setId(1L);

        sampleBlock = new Block(sampleCampus, "Block B", "BLK-B");
        sampleBlock.setId(2L);

        sampleFloor = new Floor(sampleBlock, 1, "First Floor");
        sampleFloor.setId(3L);

        sampleRoom = new Room(sampleFloor, "B-101", "Hardware Lab", RoomType.LABORATORY, 30, "LAN, AC");
        sampleRoom.setId(4L);
    }

    @Test
    @DisplayName("Should create campus when code and name are unique")
    void testCreateCampusSuccess() {
        when(campusRepository.existsByName("Engineering Campus")).thenReturn(false);
        when(campusRepository.existsByCode("ENG")).thenReturn(false);
        when(campusRepository.save(any(Campus.class))).thenAnswer(i -> {
            Campus c = i.getArgument(0);
            c.setId(10L);
            return c;
        });

        CreateCampusRequest req = new CreateCampusRequest("Engineering Campus", "eng", "East Wing Road");
        CampusDto result = spaceService.createCampus(req);

        assertNotNull(result);
        assertEquals("ENG", result.getCode());
        assertEquals(10L, result.getId());
    }

    @Test
    @DisplayName("Should reject campus creation when code is duplicate")
    void testCreateCampusDuplicateCodeRejection() {
        when(campusRepository.existsByName("Engineering Campus")).thenReturn(false);
        when(campusRepository.existsByCode("ENG")).thenReturn(true);

        CreateCampusRequest req = new CreateCampusRequest("Engineering Campus", "ENG", "East Wing Road");
        assertThrows(IllegalArgumentException.class, () -> spaceService.createCampus(req));
    }

    @Test
    @DisplayName("Should reject campus deletion if it contains child blocks")
    void testDeleteCampusSafeGuard() {
        when(campusRepository.findById(1L)).thenReturn(Optional.of(sampleCampus));
        when(blockRepository.countByCampusId(1L)).thenReturn(2L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> spaceService.deleteCampus(1L));
        assertTrue(ex.getMessage().contains("contains active blocks"));
        verify(campusRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should reject block deletion if it contains child floors")
    void testDeleteBlockSafeGuard() {
        when(blockRepository.findById(2L)).thenReturn(Optional.of(sampleBlock));
        when(floorRepository.countByBlockId(2L)).thenReturn(3L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> spaceService.deleteBlock(2L));
        assertTrue(ex.getMessage().contains("contains floors"));
        verify(blockRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should reject floor deletion if it contains child rooms")
    void testDeleteFloorSafeGuard() {
        when(floorRepository.findById(3L)).thenReturn(Optional.of(sampleFloor));
        when(roomRepository.countByFloorId(3L)).thenReturn(4L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> spaceService.deleteFloor(3L));
        assertTrue(ex.getMessage().contains("contains rooms"));
        verify(floorRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should reject room capacity reduction below existing physical seat count")
    void testRoomCapacityReductionGuard() {
        when(roomRepository.findById(4L)).thenReturn(Optional.of(sampleRoom));
        when(seatRepository.countByRoomId(4L)).thenReturn(25L); // 25 physical seats exist

        CreateRoomRequest req = new CreateRoomRequest(3L, "B-101", "Hardware Lab", RoomType.LABORATORY, 20, "LAN, AC", RoomStatus.AVAILABLE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> spaceService.updateRoom(4L, req));
        assertTrue(ex.getMessage().contains("Cannot reduce room capacity to 20 because room currently has 25 physical seats"));
    }

    @Test
    @DisplayName("Should build campus hierarchy tree accurately")
    void testGetCampusTree() {
        when(campusRepository.findAll()).thenReturn(List.of(sampleCampus));
        when(blockRepository.findByCampusIdOrderByCodeAsc(1L)).thenReturn(List.of(sampleBlock));
        when(floorRepository.findByBlockIdOrderByFloorNumberAsc(2L)).thenReturn(List.of(sampleFloor));
        when(roomRepository.findByFloorIdOrderByRoomCodeAsc(3L)).thenReturn(List.of(sampleRoom));

        List<CampusTreeDto> tree = spaceService.getCampusTree();

        assertEquals(1, tree.size());
        assertEquals("ENG", tree.get(0).getCode());
        assertEquals(1, tree.get(0).getBlocks().size());
        assertEquals("BLK-B", tree.get(0).getBlocks().get(0).getCode());
        assertEquals(1, tree.get(0).getBlocks().get(0).getFloors().size());
        assertEquals(1, tree.get(0).getBlocks().get(0).getFloors().get(0).getRooms().size());
        assertEquals("B-101", tree.get(0).getBlocks().get(0).getFloors().get(0).getRooms().get(0).getRoomCode());
    }
}
