package com.campusgrid.service;

import com.campusgrid.dto.*;
import com.campusgrid.model.*;
import com.campusgrid.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpaceService {

    private final CampusRepository campusRepository;
    private final BlockRepository blockRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final PhysicalSeatRepository seatRepository;
    private final SeatingLayoutService seatingLayoutService;

    public SpaceService(
            CampusRepository campusRepository,
            BlockRepository blockRepository,
            FloorRepository floorRepository,
            RoomRepository roomRepository,
            PhysicalSeatRepository seatRepository,
            SeatingLayoutService seatingLayoutService
    ) {
        this.campusRepository = campusRepository;
        this.blockRepository = blockRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
        this.seatingLayoutService = seatingLayoutService;
    }

    // ==========================================
    // Campus CRUD
    // ==========================================

    @Transactional(readOnly = true)
    public List<CampusDto> getAllCampuses() {
        return campusRepository.findAll().stream()
                .map(this::mapToCampusDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CampusDto getCampusById(Long id) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campus not found with ID: " + id));
        return mapToCampusDto(campus);
    }

    @Transactional
    public CampusDto createCampus(CreateCampusRequest req) {
        if (campusRepository.existsByName(req.getName())) {
            throw new IllegalArgumentException("Campus with name '" + req.getName() + "' already exists.");
        }
        if (campusRepository.existsByCode(req.getCode().toUpperCase())) {
            throw new IllegalArgumentException("Campus with code '" + req.getCode() + "' already exists.");
        }

        Campus campus = new Campus(
                req.getName().trim(),
                req.getCode().trim().toUpperCase(),
                req.getAddress() != null ? req.getAddress().trim() : null
        );
        Campus saved = campusRepository.save(campus);
        return mapToCampusDto(saved);
    }

    @Transactional
    public CampusDto updateCampus(Long id, CreateCampusRequest req) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campus not found with ID: " + id));

        if (!campus.getName().equalsIgnoreCase(req.getName()) && campusRepository.existsByName(req.getName())) {
            throw new IllegalArgumentException("Campus with name '" + req.getName() + "' already exists.");
        }
        if (!campus.getCode().equalsIgnoreCase(req.getCode()) && campusRepository.existsByCode(req.getCode().toUpperCase())) {
            throw new IllegalArgumentException("Campus with code '" + req.getCode() + "' already exists.");
        }

        campus.setName(req.getName().trim());
        campus.setCode(req.getCode().trim().toUpperCase());
        campus.setAddress(req.getAddress() != null ? req.getAddress().trim() : null);

        Campus saved = campusRepository.save(campus);
        return mapToCampusDto(saved);
    }

    @Transactional
    public void deleteCampus(Long id) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campus not found with ID: " + id));

        if (blockRepository.countByCampusId(id) > 0) {
            throw new IllegalStateException("Cannot delete campus '" + campus.getName() + "' because it contains active blocks. Remove or reassign blocks first.");
        }
        campusRepository.delete(campus);
    }

    // ==========================================
    // Block CRUD
    // ==========================================

    @Transactional(readOnly = true)
    public List<BlockDto> getBlocksByCampus(Long campusId) {
        return blockRepository.findByCampusIdOrderByCodeAsc(campusId).stream()
                .map(this::mapToBlockDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BlockDto createBlock(CreateBlockRequest req) {
        Campus campus = campusRepository.findById(req.getCampusId())
                .orElseThrow(() -> new IllegalArgumentException("Campus not found with ID: " + req.getCampusId()));

        String code = req.getCode().trim().toUpperCase();
        if (blockRepository.existsByCampusIdAndCode(campus.getId(), code)) {
            throw new IllegalArgumentException("Block with code '" + code + "' already exists in campus '" + campus.getName() + "'.");
        }

        Block block = new Block(campus, req.getName().trim(), code);
        Block saved = blockRepository.save(block);
        return mapToBlockDto(saved);
    }

    @Transactional
    public BlockDto updateBlock(Long id, CreateBlockRequest req) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Block not found with ID: " + id));

        String code = req.getCode().trim().toUpperCase();
        if (!block.getCode().equalsIgnoreCase(code) && blockRepository.existsByCampusIdAndCode(block.getCampus().getId(), code)) {
            throw new IllegalArgumentException("Block with code '" + code + "' already exists in campus '" + block.getCampus().getName() + "'.");
        }

        block.setName(req.getName().trim());
        block.setCode(code);
        Block saved = blockRepository.save(block);
        return mapToBlockDto(saved);
    }

    @Transactional
    public void deleteBlock(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Block not found with ID: " + id));

        if (floorRepository.countByBlockId(id) > 0) {
            throw new IllegalStateException("Cannot delete block '" + block.getName() + "' because it contains floors. Remove floors first.");
        }
        blockRepository.delete(block);
    }

    // ==========================================
    // Floor CRUD
    // ==========================================

    @Transactional(readOnly = true)
    public List<FloorDto> getFloorsByBlock(Long blockId) {
        return floorRepository.findByBlockIdOrderByFloorNumberAsc(blockId).stream()
                .map(this::mapToFloorDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FloorDto createFloor(CreateFloorRequest req) {
        Block block = blockRepository.findById(req.getBlockId())
                .orElseThrow(() -> new IllegalArgumentException("Block not found with ID: " + req.getBlockId()));

        if (floorRepository.existsByBlockIdAndFloorNumber(block.getId(), req.getFloorNumber())) {
            throw new IllegalArgumentException("Floor number " + req.getFloorNumber() + " already exists in block '" + block.getName() + "'.");
        }

        Floor floor = new Floor(block, req.getFloorNumber(), req.getName().trim());
        Floor saved = floorRepository.save(floor);
        return mapToFloorDto(saved);
    }

    @Transactional
    public FloorDto updateFloor(Long id, CreateFloorRequest req) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Floor not found with ID: " + id));

        if (floor.getFloorNumber() != req.getFloorNumber() && floorRepository.existsByBlockIdAndFloorNumber(floor.getBlock().getId(), req.getFloorNumber())) {
            throw new IllegalArgumentException("Floor number " + req.getFloorNumber() + " already exists in block '" + floor.getBlock().getName() + "'.");
        }

        floor.setFloorNumber(req.getFloorNumber());
        floor.setName(req.getName().trim());
        Floor saved = floorRepository.save(floor);
        return mapToFloorDto(saved);
    }

    @Transactional
    public void deleteFloor(Long id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Floor not found with ID: " + id));

        if (roomRepository.countByFloorId(id) > 0) {
            throw new IllegalStateException("Cannot delete floor '" + floor.getName() + "' because it contains rooms. Remove rooms first.");
        }
        floorRepository.delete(floor);
    }

    // ==========================================
    // Room CRUD & Filtering
    // ==========================================

    @Transactional(readOnly = true)
    public List<RoomDto> filterRooms(Long campusId, Long blockId, Long floorId, RoomType roomType, RoomStatus status, Integer minCapacity, String search) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return roomRepository.filterRooms(campusId, blockId, floorId, roomType, status, minCapacity, cleanSearch)
                .stream()
                .map(this::mapToRoomDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + id));
        return mapToRoomDto(room);
    }

    @Transactional(readOnly = true)
    public RoomDetailDto getRoomDetail(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + id));

        RoomDto roomDto = mapToRoomDto(room);
        List<String> facilitiesList = new ArrayList<>();
        if (room.getFacilities() != null && !room.getFacilities().trim().isEmpty()) {
            facilitiesList = Arrays.stream(room.getFacilities().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        String hierarchyPath = String.format("%s > %s > %s > %s",
                room.getFloor().getBlock().getCampus().getName(),
                room.getFloor().getBlock().getName(),
                room.getFloor().getName(),
                room.getRoomCode());

        long configuredSeats = seatRepository.countByRoomId(id);
        long availableSeats = seatRepository.countByRoomIdAndStatus(id, SeatStatus.AVAILABLE);
        long blockedSeats = seatRepository.countByRoomIdAndStatus(id, SeatStatus.BLOCKED)
                + seatRepository.countByRoomIdAndStatus(id, SeatStatus.MAINTENANCE);

        return new RoomDetailDto(
                roomDto,
                facilitiesList,
                hierarchyPath,
                (int) configuredSeats,
                (int) availableSeats,
                (int) blockedSeats
        );
    }

    @Transactional
    public RoomDto createRoom(CreateRoomRequest req) {
        Floor floor = floorRepository.findById(req.getFloorId())
                .orElseThrow(() -> new IllegalArgumentException("Floor not found with ID: " + req.getFloorId()));

        String roomCode = req.getRoomCode().trim().toUpperCase();
        if (roomRepository.existsByRoomCode(roomCode)) {
            throw new IllegalArgumentException("Room with code '" + roomCode + "' already exists.");
        }

        Room room = new Room(
                floor,
                roomCode,
                req.getRoomName().trim(),
                req.getRoomType(),
                req.getCapacity(),
                req.getFacilities() != null ? req.getFacilities().trim() : null
        );

        if (req.getStatus() != null) {
            room.setStatus(req.getStatus());
        }
        if (req.getLayoutTemplate() != null) {
            room.setLayoutTemplate(req.getLayoutTemplate());
        }
        if (req.getSystemCount() != null) {
            room.setSystemCount(req.getSystemCount());
        }

        Room saved = roomRepository.save(room);

        // If template and dimensions are provided and within capacity, automatically generate initial seating layout
        if (req.getRowsCount() != null && req.getColumnsCount() != null && req.getRowsCount() > 0 && req.getColumnsCount() > 0) {
            LayoutTemplate template = req.getLayoutTemplate() != null ? req.getLayoutTemplate() : LayoutTemplate.STANDARD_GRID;
            int estSeats = seatingLayoutService.estimatePhysicalSeatCount(template, req.getRowsCount(), req.getColumnsCount());
            if (estSeats <= saved.getCapacity()) {
                ConfigureLayoutRequest layoutReq = new ConfigureLayoutRequest(
                        template,
                        req.getRowsCount(),
                        req.getColumnsCount(),
                        req.getSystemCount()
                );
                seatingLayoutService.configureAndGenerateLayout(saved.getId(), layoutReq);
            }
        }

        return mapToRoomDto(saved);
    }

    @Transactional
    public RoomDto updateRoom(Long id, CreateRoomRequest req) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + id));

        String roomCode = req.getRoomCode().trim().toUpperCase();
        if (!room.getRoomCode().equalsIgnoreCase(roomCode) && roomRepository.existsByRoomCode(roomCode)) {
            throw new IllegalArgumentException("Room with code '" + roomCode + "' already exists.");
        }

        if (req.getFloorId() != null && !room.getFloor().getId().equals(req.getFloorId())) {
            Floor floor = floorRepository.findById(req.getFloorId())
                    .orElseThrow(() -> new IllegalArgumentException("Floor not found with ID: " + req.getFloorId()));
            room.setFloor(floor);
        }

        long currentPhysicalSeats = seatRepository.countByRoomId(id);
        if (req.getCapacity() < currentPhysicalSeats) {
            throw new IllegalArgumentException(String.format(
                    "Cannot reduce room capacity to %d because room currently has %d physical seats configured. " +
                    "Reconfigure or reset seating layout first.",
                    req.getCapacity(), currentPhysicalSeats
            ));
        }

        room.setRoomCode(roomCode);
        room.setRoomName(req.getRoomName().trim());
        room.setRoomType(req.getRoomType());
        room.setCapacity(req.getCapacity());
        room.setFacilities(req.getFacilities() != null ? req.getFacilities().trim() : null);

        if (req.getStatus() != null) {
            room.setStatus(req.getStatus());
        }
        if (req.getLayoutTemplate() != null) {
            room.setLayoutTemplate(req.getLayoutTemplate());
        }
        if (req.getSystemCount() != null) {
            room.setSystemCount(req.getSystemCount());
        }

        Room saved = roomRepository.save(room);
        return mapToRoomDto(saved);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + id));

        // Clean up physical seats first
        seatRepository.deleteByRoomId(id);
        roomRepository.delete(room);
    }

    // ==========================================
    // Hierarchy Tree & Space Statistics
    // ==========================================

    @Transactional(readOnly = true)
    public List<CampusTreeDto> getCampusTree() {
        List<Campus> campuses = campusRepository.findAll();
        List<CampusTreeDto> tree = new ArrayList<>();

        for (Campus c : campuses) {
            CampusTreeDto cDto = new CampusTreeDto(c.getId(), c.getName(), c.getCode(), c.getAddress());
            List<Block> blocks = blockRepository.findByCampusIdOrderByCodeAsc(c.getId());

            for (Block b : blocks) {
                CampusTreeDto.BlockTreeDto bDto = new CampusTreeDto.BlockTreeDto(b.getId(), b.getName(), b.getCode());
                List<Floor> floors = floorRepository.findByBlockIdOrderByFloorNumberAsc(b.getId());

                for (Floor f : floors) {
                    CampusTreeDto.FloorTreeDto fDto = new CampusTreeDto.FloorTreeDto(f.getId(), f.getName(), f.getFloorNumber());
                    List<Room> rooms = roomRepository.findByFloorIdOrderByRoomCodeAsc(f.getId());
                    fDto.setRoomCount(rooms.size());

                    for (Room r : rooms) {
                        fDto.getRooms().add(new CampusTreeDto.RoomTreeDto(
                                r.getId(),
                                r.getRoomCode(),
                                r.getRoomName(),
                                r.getRoomType(),
                                r.getCapacity(),
                                r.getStatus()
                        ));
                    }
                    bDto.getFloors().add(fDto);
                }
                cDto.getBlocks().add(bDto);
            }
            tree.add(cDto);
        }
        return tree;
    }

    @Transactional(readOnly = true)
    public SpaceStatsDto getSpaceStats() {
        long totalCampuses = campusRepository.count();
        long totalBlocks = blockRepository.count();
        long totalFloors = floorRepository.count();
        long totalRooms = roomRepository.count();
        long totalCapacity = roomRepository.sumTotalCapacity();

        long classrooms = roomRepository.countByRoomType(RoomType.CLASSROOM);
        long labs = roomRepository.countByRoomType(RoomType.LABORATORY);
        long auditoriums = roomRepository.countByRoomType(RoomType.AUDITORIUM);

        long available = roomRepository.countByStatus(RoomStatus.AVAILABLE);
        long occupied = roomRepository.countByStatus(RoomStatus.OCCUPIED);
        long maintenance = roomRepository.countByStatus(RoomStatus.MAINTENANCE);

        return new SpaceStatsDto(
                totalCampuses,
                totalBlocks,
                totalFloors,
                totalRooms,
                totalCapacity,
                classrooms,
                labs,
                auditoriums,
                available,
                occupied,
                maintenance
        );
    }

    // ==========================================
    // Database Seeding for Module 2 Spaces
    // ==========================================

    @Transactional
    public void seedInitialSpaces() {
        if (campusRepository.count() > 0) {
            return; // Already initialized
        }

        System.out.println("Seeding Module 2 Campus & Space Data...");

        Campus mainCampus = new Campus("Main Campus", "MC", "100 University Avenue, Academic District");
        mainCampus = campusRepository.save(mainCampus);

        // Block A: Academic Block
        Block blockA = new Block(mainCampus, "Academic Block A", "BLK-A");
        blockA = blockRepository.save(blockA);

        // Block B: Science & Engineering Block
        Block blockB = new Block(mainCampus, "Science & Engineering Block B", "BLK-B");
        blockB = blockRepository.save(blockB);

        // Floors for Block A
        Floor groundFloorA = floorRepository.save(new Floor(blockA, 0, "Ground Floor"));
        Floor firstFloorA = floorRepository.save(new Floor(blockA, 1, "First Floor"));
        Floor secondFloorA = floorRepository.save(new Floor(blockA, 2, "Second Floor"));

        // Floors for Block B
        Floor firstFloorB = floorRepository.save(new Floor(blockB, 1, "First Floor"));

        // Rooms for Block A
        Room audA001 = new Room(groundFloorA, "A-001", "Main Auditorium", RoomType.AUDITORIUM, 120, "Projector, Sound System, Central AC, Stage Lights");
        audA001.setLayoutTemplate(LayoutTemplate.CENTRAL_AISLE);
        audA001 = roomRepository.save(audA001);
        seatingLayoutService.configureAndGenerateLayout(audA001.getId(), new ConfigureLayoutRequest(LayoutTemplate.CENTRAL_AISLE, 10, 13, 0));

        Room clsA101 = new Room(firstFloorA, "A-101", "Lecture Hall 101", RoomType.CLASSROOM, 40, "Projector, Whiteboard, Audio Amplifier");
        clsA101.setLayoutTemplate(LayoutTemplate.STANDARD_GRID);
        clsA101 = roomRepository.save(clsA101);
        seatingLayoutService.configureAndGenerateLayout(clsA101.getId(), new ConfigureLayoutRequest(LayoutTemplate.STANDARD_GRID, 6, 6, 0));

        Room clsA102 = new Room(firstFloorA, "A-102", "Interactive Classroom", RoomType.CLASSROOM, 36, "Smart Board, High-Speed Wi-Fi");
        clsA102.setLayoutTemplate(LayoutTemplate.STANDARD_GRID);
        clsA102 = roomRepository.save(clsA102);
        seatingLayoutService.configureAndGenerateLayout(clsA102.getId(), new ConfigureLayoutRequest(LayoutTemplate.STANDARD_GRID, 6, 6, 0));

        Room clsA201 = new Room(secondFloorA, "A-201", "Seminar Room", RoomType.CLASSROOM, 30, "Projector, Podium, Teleconferencing");
        clsA201.setLayoutTemplate(LayoutTemplate.H_PATTERN);
        clsA201 = roomRepository.save(clsA201);
        seatingLayoutService.configureAndGenerateLayout(clsA201.getId(), new ConfigureLayoutRequest(LayoutTemplate.H_PATTERN, 5, 7, 0));

        // Rooms for Block B
        Room labB101 = new Room(firstFloorB, "B-101", "Computer Systems Lab", RoomType.LABORATORY, 30, "30 Workstations, LAN Switch, Air Conditioning");
        labB101.setLayoutTemplate(LayoutTemplate.LABORATORY);
        labB101.setSystemCount(30);
        labB101 = roomRepository.save(labB101);
        seatingLayoutService.configureAndGenerateLayout(labB101.getId(), new ConfigureLayoutRequest(LayoutTemplate.LABORATORY, 5, 9, 30));

        System.out.println("Module 2 Space Seeding Complete: 1 Campus, 2 Blocks, 4 Floors, 5 Rooms with configured layouts.");
    }

    // ==========================================
    // DTO Mappers
    // ==========================================

    private CampusDto mapToCampusDto(Campus c) {
        return new CampusDto(c.getId(), c.getName(), c.getCode(), c.getAddress(), c.isActive());
    }

    private BlockDto mapToBlockDto(Block b) {
        return new BlockDto(b.getId(), b.getCampus().getId(), b.getCampus().getName(), b.getName(), b.getCode(), b.isActive());
    }

    private FloorDto mapToFloorDto(Floor f) {
        return new FloorDto(f.getId(), f.getBlock().getId(), f.getBlock().getName(), f.getFloorNumber(), f.getName(), f.isActive());
    }

    private RoomDto mapToRoomDto(Room r) {
        return new RoomDto(
                r.getId(),
                r.getFloor().getId(),
                r.getFloor().getBlock().getCampus().getId(),
                r.getFloor().getBlock().getCampus().getName(),
                r.getFloor().getBlock().getId(),
                r.getFloor().getBlock().getName(),
                r.getFloor().getName(),
                r.getRoomCode(),
                r.getRoomName(),
                r.getRoomType(),
                r.getCapacity(),
                r.getFacilities(),
                r.getStatus(),
                r.getLayoutTemplate(),
                r.getRowsCount(),
                r.getColumnsCount(),
                r.getSystemCount(),
                r.isActive()
        );
    }
}
