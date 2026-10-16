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
                CampusTreeDto.BlockTreeDto bDto = new CampusTreeDto.BlockTreeDto(b.getId(), b.getName(), b.getCode(), b.getDescription());
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
        if (campusRepository.count() > 0 && blockRepository.count() >= 5) {
            return; // Already initialized with all 5 blocks
        }

        System.out.println("Seeding Campus & Space Directory (Blocks A to E)...");

        Campus mainCampus = campusRepository.findAll().stream().findFirst().orElse(null);
        if (mainCampus == null) {
            mainCampus = campusRepository.save(new Campus("Main Campus", "MC", "100 University Avenue, Academic District"));
        }

        // 1. Block A — Computer Science
        if (!blockRepository.existsByCode("BLK-A")) {
            Block blockA = blockRepository.save(new Block(mainCampus, "Block A — Computer Science", "BLK-A", "Computer Science"));
            Floor floorA1 = floorRepository.save(new Floor(blockA, 1, "First Floor"));
            createRoomWithLayout(floorA1, "A101", "Lecture Hall A101", RoomType.CLASSROOM, 50, "Smart Projector, Whiteboard, Audio Amplifier", LayoutTemplate.STANDARD_GRID, 5, 10, 0);
            createRoomWithLayout(floorA1, "A102", "Interactive Classroom A102", RoomType.CLASSROOM, 40, "Interactive Display, High-Speed Wi-Fi", LayoutTemplate.STANDARD_GRID, 5, 8, 0);
            createRoomWithLayout(floorA1, "A103", "Classroom A103", RoomType.CLASSROOM, 40, "Digital Board, Ceiling Audio", LayoutTemplate.STANDARD_GRID, 5, 8, 0);
            createRoomWithLayout(floorA1, "A104", "Software Development Lab A104", RoomType.LABORATORY, 35, "35 Workstations, LAN Switch, Air Conditioning", LayoutTemplate.LABORATORY, 5, 7, 35);
            createRoomWithLayout(floorA1, "A105", "CS Seminar Hall A105", RoomType.AUDITORIUM, 70, "Central AC, Stage Podium, Dual Projectors", LayoutTemplate.CENTRAL_AISLE, 7, 10, 0);
        }

        // 2. Block B — Artificial Intelligence & Data Science
        if (!blockRepository.existsByCode("BLK-B")) {
            Block blockB = blockRepository.save(new Block(mainCampus, "Block B — Artificial Intelligence & Data Science", "BLK-B", "Artificial Intelligence & Data Science"));
            Floor floorB1 = floorRepository.save(new Floor(blockB, 1, "First Floor"));
            createRoomWithLayout(floorB1, "B101", "AI & Robotics Lab B101", RoomType.LABORATORY, 35, "GPU Workstations, Robotics Kits, Sensor Stations", LayoutTemplate.LABORATORY, 5, 7, 35);
            createRoomWithLayout(floorB1, "B102", "Classroom B102", RoomType.CLASSROOM, 40, "Smart Board, Acoustic Panels", LayoutTemplate.STANDARD_GRID, 5, 8, 0);
            createRoomWithLayout(floorB1, "B103", "Classroom B103", RoomType.CLASSROOM, 40, "Projector, High-Speed Wi-Fi", LayoutTemplate.STANDARD_GRID, 5, 8, 0);
            createRoomWithLayout(floorB1, "B104", "Data Analytics Lab B104", RoomType.LABORATORY, 35, "35 High-Performance Terminals, Gigabit Network", LayoutTemplate.LABORATORY, 5, 7, 35);
            createRoomWithLayout(floorB1, "B105", "Machine Learning Studio B105", RoomType.CLASSROOM, 40, "Collaborative Display Hubs, Telepresence", LayoutTemplate.H_PATTERN, 5, 8, 0);
        }

        // 3. Block C — Electronics
        if (!blockRepository.existsByCode("BLK-C")) {
            Block blockC = blockRepository.save(new Block(mainCampus, "Block C — Electronics", "BLK-C", "Electronics"));
            Floor floorC1 = floorRepository.save(new Floor(blockC, 1, "First Floor"));
            createRoomWithLayout(floorC1, "C101", "VLSI Design Lab C101", RoomType.LABORATORY, 35, "Oscilloscopes, FPGA Kits, Embedded Dev Boards", LayoutTemplate.LABORATORY, 5, 7, 35);
            createRoomWithLayout(floorC1, "C102", "Classroom C102", RoomType.CLASSROOM, 40, "Digital Whiteboard, Sound System", LayoutTemplate.STANDARD_GRID, 5, 8, 0);
            createRoomWithLayout(floorC1, "C103", "Classroom C103", RoomType.CLASSROOM, 40, "Lecture Projection, Air Conditioning", LayoutTemplate.STANDARD_GRID, 5, 8, 0);
            createRoomWithLayout(floorC1, "C104", "Embedded Systems Lab C104", RoomType.LABORATORY, 35, "Microcontroller Units, Logic Analyzers", LayoutTemplate.LABORATORY, 5, 7, 35);
            createRoomWithLayout(floorC1, "C105", "Circuit Theory Hall C105", RoomType.CLASSROOM, 50, "Tiered Seating, Dual Screens", LayoutTemplate.STANDARD_GRID, 5, 10, 0);
        }

        // 4. Block D — Mechanical Engineering
        if (!blockRepository.existsByCode("BLK-D")) {
            Block blockD = blockRepository.save(new Block(mainCampus, "Block D — Mechanical Engineering", "BLK-D", "Mechanical Engineering"));
            Floor floorD1 = floorRepository.save(new Floor(blockD, 1, "First Floor"));
            createRoomWithLayout(floorD1, "D101", "CAD/CAM Center D101", RoomType.LABORATORY, 35, "CAD Workstations, 3D Plotters, SolidWorks License", LayoutTemplate.LABORATORY, 5, 7, 35);
            createRoomWithLayout(floorD1, "D102", "Classroom D102", RoomType.CLASSROOM, 40, "Overhead Projection, Ergonomic Desks", LayoutTemplate.STANDARD_GRID, 5, 8, 0);
            createRoomWithLayout(floorD1, "D103", "Classroom D103", RoomType.CLASSROOM, 40, "Interactive Display, Audio Amplifier", LayoutTemplate.STANDARD_GRID, 5, 8, 0);
            createRoomWithLayout(floorD1, "D104", "Thermodynamics Lab D104", RoomType.LABORATORY, 30, "Simulation Terminals, Thermal Imaging", LayoutTemplate.LABORATORY, 5, 6, 30);
            createRoomWithLayout(floorD1, "D105", "Mechatronics Workshop D105", RoomType.CLASSROOM, 40, "Smart Board, Robotic Arm Demonstrator", LayoutTemplate.H_PATTERN, 5, 8, 0);
        }

        // 5. Block E — Examination and Academic Facilities
        if (!blockRepository.existsByCode("BLK-E")) {
            Block blockE = blockRepository.save(new Block(mainCampus, "Block E — Examination and Academic Facilities", "BLK-E", "Examination and Academic Facilities"));
            Floor floorE1 = floorRepository.save(new Floor(blockE, 1, "First Floor"));
            createRoomWithLayout(floorE1, "E101", "Central Examination Hall E101", RoomType.AUDITORIUM, 80, "Spaced Seating Layout, CCTV Surveillance, Exam Monitoring", LayoutTemplate.CENTRAL_AISLE, 8, 10, 0);
            createRoomWithLayout(floorE1, "E102", "Central Examination Hall E102", RoomType.AUDITORIUM, 80, "Isolated Desk Configuration, High Luminance, Secure Entry", LayoutTemplate.CENTRAL_AISLE, 8, 10, 0);
            createRoomWithLayout(floorE1, "E103", "Academic Evaluation Center E103", RoomType.CLASSROOM, 50, "Evaluation Desks, Document Scanners, Biometric Access", LayoutTemplate.STANDARD_GRID, 5, 10, 0);
            createRoomWithLayout(floorE1, "E104", "Academic Council Hall E104", RoomType.AUDITORIUM, 60, "Conference Audio System, Delegate Mic Units", LayoutTemplate.CENTRAL_AISLE, 6, 10, 0);
            createRoomWithLayout(floorE1, "E105", "Main University Auditorium E105", RoomType.AUDITORIUM, 100, "Acoustic Walls, Central AC, Stage Lights, Sound System", LayoutTemplate.CENTRAL_AISLE, 10, 10, 0);
        }

        System.out.println("Campus & Space Seeding Complete: 1 Campus, 5 Blocks (A-E), 25 Rooms with physical seating layouts.");
    }

    private void createRoomWithLayout(Floor floor, String roomCode, String roomName, RoomType type, int capacity, String facilities, LayoutTemplate template, int rows, int cols, int systems) {
        Room room = new Room(floor, roomCode, roomName, type, capacity, facilities);
        room.setLayoutTemplate(template);
        if (systems > 0) {
            room.setSystemCount(systems);
        }
        room = roomRepository.save(room);
        try {
            seatingLayoutService.configureAndGenerateLayout(room.getId(), new ConfigureLayoutRequest(template, rows, cols, systems));
        } catch (Exception e) {
            System.err.println("Notice: Seating layout generation for " + roomCode + ": " + e.getMessage());
        }
    }

    // ==========================================
    // DTO Mappers
    // ==========================================

    private CampusDto mapToCampusDto(Campus c) {
        return new CampusDto(c.getId(), c.getName(), c.getCode(), c.getAddress(), c.isActive());
    }

    private BlockDto mapToBlockDto(Block b) {
        return new BlockDto(b.getId(), b.getCampus().getId(), b.getCampus().getName(), b.getName(), b.getCode(), b.getDescription(), b.isActive());
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
