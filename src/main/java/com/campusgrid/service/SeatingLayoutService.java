package com.campusgrid.service;

import com.campusgrid.dto.ConfigureLayoutRequest;
import com.campusgrid.dto.SeatDto;
import com.campusgrid.dto.SeatingGridDto;
import com.campusgrid.model.*;
import com.campusgrid.repository.PhysicalSeatRepository;
import com.campusgrid.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatingLayoutService {

    private final RoomRepository roomRepository;
    private final PhysicalSeatRepository seatRepository;

    public SeatingLayoutService(RoomRepository roomRepository, PhysicalSeatRepository seatRepository) {
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public SeatingGridDto configureAndGenerateLayout(Long roomId, ConfigureLayoutRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));

        int rows = request.getRowsCount();
        int cols = request.getColumnsCount();
        LayoutTemplate template = request.getLayoutTemplate();

        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("Rows count and columns count must both be at least 1.");
        }
        if (rows > 50 || cols > 50) {
            throw new IllegalArgumentException("Grid dimensions cannot exceed 50x50.");
        }

        List<Integer> aisleCols = determineAisleColumns(template, cols);
        List<Integer> aisleRows = determineAisleRows(template, rows);

        // Fetch existing physical seats for status preservation
        List<PhysicalSeat> existingSeats = seatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(room.getId());
        Map<String, PhysicalSeat> existingSeatMap = new HashMap<>();
        for (PhysicalSeat es : existingSeats) {
            existingSeatMap.put(es.getRowNumber() + "-" + es.getColumnNumber(), es);
        }

        // Pre-calculate physical seat coordinates, reusing existing seats where compatible
        List<PhysicalSeat> finalSeats = new ArrayList<>();
        Set<String> seatNumberSet = new HashSet<>();
        Set<Long> preservedSeatIds = new HashSet<>();

        int seatCounter = 1;
        for (int r = 1; r <= rows; r++) {
            if (aisleRows.contains(r)) {
                continue; // Gaps and horizontal walkways are not physical seats
            }
            String rowLabel = getRowLetter(r);

            for (int c = 1; c <= cols; c++) {
                if (aisleCols.contains(c)) {
                    continue; // Central or walkway aisles are not physical seats
                }

                String seatNumber = String.format("%s-%02d", rowLabel, c);
                if (seatNumberSet.contains(seatNumber)) {
                    seatNumber = String.format("%s-%02d-%d", rowLabel, c, seatCounter);
                }
                seatNumberSet.add(seatNumber);

                SeatType seatType = (template == LayoutTemplate.LABORATORY || room.getRoomType() == RoomType.LABORATORY)
                        ? SeatType.WORKSTATION
                        : SeatType.REGULAR;

                PhysicalSeat existing = existingSeatMap.get(r + "-" + c);
                if (existing != null) {
                    // Preserve existing seat entity, its ID, and custom status
                    existing.setSeatNumber(seatNumber);
                    existing.setRowLabel(rowLabel);
                    existing.setSeatType(seatType);
                    // Retain custom status (e.g. BLOCKED, MAINTENANCE)
                    finalSeats.add(existing);
                    preservedSeatIds.add(existing.getId());
                } else {
                    // Newly introduced seat
                    PhysicalSeat newSeat = new PhysicalSeat(room, seatNumber, r, c, rowLabel, seatType, SeatStatus.AVAILABLE);
                    finalSeats.add(newSeat);
                }
                seatCounter++;
            }
        }

        // Validate capacity rule
        if (finalSeats.size() > room.getCapacity()) {
            throw new IllegalArgumentException(String.format(
                    "Layout generation produced %d physical seats, which exceeds the declared room capacity of %d. " +
                    "Please increase room capacity or decrease grid dimensions.",
                    finalSeats.size(), room.getCapacity()
            ));
        }

        // Delete only orphaned/displaced seats that no longer fit in the new layout geometry
        List<PhysicalSeat> orphanedSeats = existingSeats.stream()
                .filter(es -> !preservedSeatIds.contains(es.getId()))
                .collect(Collectors.toList());
        if (!orphanedSeats.isEmpty()) {
            seatRepository.deleteAll(orphanedSeats);
        }

        // Save preserved and newly created seats
        seatRepository.saveAll(finalSeats);

        // Update room layout metadata
        room.setRowsCount(rows);
        room.setColumnsCount(cols);
        room.setLayoutTemplate(template);
        if (request.getSystemCount() != null && request.getSystemCount() >= 0) {
            room.setSystemCount(request.getSystemCount());
        }
        roomRepository.save(room);

        return getRoomSeatingGrid(roomId);
    }

    @Transactional(readOnly = true)
    public SeatingGridDto getRoomSeatingGrid(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));

        List<PhysicalSeat> seats = seatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(roomId);
        List<SeatDto> seatDtos = new ArrayList<>();
        int availableCount = 0;
        int blockedCount = 0;

        for (PhysicalSeat s : seats) {
            seatDtos.add(new SeatDto(
                    s.getId(),
                    s.getSeatNumber(),
                    s.getRowNumber(),
                    s.getColumnNumber(),
                    s.getRowLabel(),
                    s.getSeatType(),
                    s.getStatus(),
                    s.isActive()
            ));
            if (s.getStatus() == SeatStatus.AVAILABLE) {
                availableCount++;
            } else if (s.getStatus() == SeatStatus.BLOCKED || s.getStatus() == SeatStatus.MAINTENANCE) {
                blockedCount++;
            }
        }

        List<Integer> aisleCols = determineAisleColumns(room.getLayoutTemplate(), room.getColumnsCount());
        List<Integer> aisleRows = determineAisleRows(room.getLayoutTemplate(), room.getRowsCount());

        return new SeatingGridDto(
                room.getId(),
                room.getRoomCode(),
                room.getRoomName(),
                room.getRoomType(),
                room.getCapacity(),
                room.getLayoutTemplate(),
                room.getRowsCount(),
                room.getColumnsCount(),
                seatDtos.size(),
                availableCount,
                blockedCount,
                aisleCols,
                aisleRows,
                seatDtos
        );
    }

    @Transactional
    public SeatDto updateSeatStatus(Long roomId, Long seatId, SeatStatus newStatus) {
        PhysicalSeat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found with ID: " + seatId));

        if (!seat.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("Seat does not belong to specified room ID: " + roomId);
        }

        seat.setStatus(newStatus);
        PhysicalSeat saved = seatRepository.save(seat);

        return new SeatDto(
                saved.getId(),
                saved.getSeatNumber(),
                saved.getRowNumber(),
                saved.getColumnNumber(),
                saved.getRowLabel(),
                saved.getSeatType(),
                saved.getStatus(),
                saved.isActive()
        );
    }

    public List<Integer> determineAisleColumns(LayoutTemplate template, int cols) {
        List<Integer> aisles = new ArrayList<>();
        if (template == LayoutTemplate.CENTRAL_AISLE || template == LayoutTemplate.H_PATTERN) {
            if (cols >= 3) {
                // Central column walkway
                aisles.add((cols / 2) + 1);
            }
        } else if (template == LayoutTemplate.LABORATORY) {
            // Lab bench aisles every 3rd column
            for (int c = 3; c <= cols; c += 3) {
                aisles.add(c);
            }
        }
        return aisles;
    }

    public List<Integer> determineAisleRows(LayoutTemplate template, int rows) {
        List<Integer> aisles = new ArrayList<>();
        if (template == LayoutTemplate.H_PATTERN) {
            if (rows >= 4) {
                // Horizontal cross-walkway dividing upper and lower sections
                aisles.add((rows / 2) + 1);
            }
        }
        return aisles;
    }

    public int estimatePhysicalSeatCount(LayoutTemplate template, int rows, int cols) {
        List<Integer> aisleCols = determineAisleColumns(template, cols);
        List<Integer> aisleRows = determineAisleRows(template, rows);
        int validRows = rows - aisleRows.size();
        int validCols = cols - aisleCols.size();
        return Math.max(0, validRows) * Math.max(0, validCols);
    }

    private String getRowLetter(int rowNumber) {
        StringBuilder sb = new StringBuilder();
        int r = rowNumber;
        while (r > 0) {
            r--;
            sb.insert(0, (char) ('A' + (r % 26)));
            r = r / 26;
        }
        return sb.toString();
    }
}
