package com.campusgrid.service;

import com.campusgrid.model.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ExamSeatingEngine {

    public static class SeatingAssignment {
        private final Student student;
        private final Room room;
        private final PhysicalSeat seat;

        public SeatingAssignment(Student student, Room room, PhysicalSeat seat) {
            this.student = student;
            this.room = room;
            this.seat = seat;
        }

        public Student getStudent() {
            return student;
        }

        public Room getRoom() {
            return room;
        }

        public PhysicalSeat getSeat() {
            return seat;
        }
    }

    /**
     * Allocates students to physical seats across assigned exam rooms deterministically.
     *
     * @param students List of eligible candidate students.
     * @param roomSeatsMap Map of Room -> List of PhysicalSeats in that room.
     * @param strategy The seating strategy (ROLL_NUMBER_ASCENDING, ALTERNATING_SECTION, SPACED_GAP).
     * @param spacingGap 0 for consecutive, 1 for alternate seats.
     * @return List of deterministic SeatingAssignment pairings.
     */
    public List<SeatingAssignment> computeSeatingArrangement(
            List<Student> students,
            Map<Room, List<PhysicalSeat>> roomSeatsMap,
            SeatingStrategy strategy,
            int spacingGap
    ) {
        if (students == null || students.isEmpty() || roomSeatsMap == null || roomSeatsMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Order candidates stably and deterministically
        List<Student> orderedStudents = orderCandidates(students, strategy);

        // 2. Prepare the available usable physical seat sequence across all allocated rooms
        List<Map.Entry<Room, PhysicalSeat>> eligibleSeats = new ArrayList<>();
        boolean applySpacing = (strategy == SeatingStrategy.SPACED_GAP || spacingGap > 0);

        // Sort rooms stably by room code
        List<Map.Entry<Room, List<PhysicalSeat>>> sortedRooms = new ArrayList<>(roomSeatsMap.entrySet());
        sortedRooms.sort(Comparator.comparing(e -> e.getKey().getRoomCode() != null ? e.getKey().getRoomCode() : ""));

        int totalUnavailableInAssignedRooms = 0;

        for (Map.Entry<Room, List<PhysicalSeat>> entry : sortedRooms) {
            Room room = entry.getKey();
            List<PhysicalSeat> seats = new ArrayList<>(entry.getValue());

            // Stably sort seats by row, column, seatNumber
            seats.sort(Comparator.comparingInt(PhysicalSeat::getRowNumber)
                    .thenComparingInt(PhysicalSeat::getColumnNumber)
                    .thenComparing(s -> s.getSeatNumber() != null ? s.getSeatNumber() : ""));

            int seatCounter = 0;
            for (PhysicalSeat seat : seats) {
                // Strict check: only active seats with status AVAILABLE and NOT aisle walkways
                boolean isUsable = seat.isActive() &&
                                   seat.getStatus() == SeatStatus.AVAILABLE &&
                                   seat.getSeatType() != SeatType.AISLE_GAP;

                if (isUsable) {
                    if (!applySpacing || (seatCounter % 2 == 0)) {
                        eligibleSeats.add(Map.entry(room, seat));
                    }
                    seatCounter++;
                } else {
                    totalUnavailableInAssignedRooms++;
                }
            }
        }

        if (eligibleSeats.isEmpty()) {
            throw new IllegalStateException("No usable physical seats found in the assigned examination rooms. All seats are either unconfigured, blocked, or under maintenance.");
        }

        if (orderedStudents.size() > eligibleSeats.size()) {
            throw new IllegalStateException(String.format(
                    "Insufficient seating capacity! Eligible candidates: %d, Usable seats available: %d (%d seats in assigned rooms are blocked or unavailable). Please assign additional rooms.",
                    orderedStudents.size(), eligibleSeats.size(), totalUnavailableInAssignedRooms
            ));
        }

        // 3. Map students to seats deterministically
        List<SeatingAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < orderedStudents.size(); i++) {
            Student candidate = orderedStudents.get(i);
            Map.Entry<Room, PhysicalSeat> seatEntry = eligibleSeats.get(i);
            assignments.add(new SeatingAssignment(candidate, seatEntry.getKey(), seatEntry.getValue()));
        }

        return assignments;
    }

    /**
     * Stably orders candidate students.
     */
    private List<Student> orderCandidates(List<Student> rawList, SeatingStrategy strategy) {
        List<Student> candidates = new ArrayList<>(rawList);

        if (strategy == SeatingStrategy.ROLL_NUMBER_ASCENDING) {
            candidates.sort(Comparator.comparing(s -> s.getRollNumber() != null ? s.getRollNumber() : "", String::compareTo));
            return candidates;
        }

        if (strategy == SeatingStrategy.ALTERNATING_SECTION) {
            // Group students by section with stable roll number order inside each section
            candidates.sort(Comparator.comparing(s -> s.getRollNumber() != null ? s.getRollNumber() : "", String::compareTo));
            Map<String, Queue<Student>> sectionQueues = new LinkedHashMap<>();

            for (Student s : candidates) {
                String sec = s.getSectionName() != null ? s.getSectionName().trim().toUpperCase() : "GENERAL";
                sectionQueues.computeIfAbsent(sec, k -> new LinkedList<>()).add(s);
            }

            // Interleave sections deterministically: Sec A student 1, Sec B student 1, Sec C student 1...
            List<Student> interleaved = new ArrayList<>();
            boolean hasMore = true;
            while (hasMore) {
                hasMore = false;
                for (Queue<Student> queue : sectionQueues.values()) {
                    if (!queue.isEmpty()) {
                        interleaved.add(queue.poll());
                        hasMore = true;
                    }
                }
            }
            return interleaved;
        }

        // Default: Sort by roll number
        candidates.sort(Comparator.comparing(s -> s.getRollNumber() != null ? s.getRollNumber() : "", String::compareTo));
        return candidates;
    }
}
