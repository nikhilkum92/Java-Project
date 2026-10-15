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
     * Allocates students to physical seats across one or more exam rooms according to the selected strategy.
     *
     * @param students List of enrolled candidate students eligible for this exam.
     * @param roomSeatsMap Map of Room -> List of active PhysicalSeats in that room (ordered by row & column).
     * @param strategy The seating strategy (ALTERNATING_SECTION, ROLL_NUMBER_ASCENDING, SPACED_GAP).
     * @param spacingGap Desired desk spacing gap (e.g., 0 for consecutive, 1 for every second seat).
     * @return List of computed SeatingAssignment pairings.
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

        // 1. Order candidates based on strategy
        List<Student> orderedStudents = orderCandidates(students, strategy);

        // 2. Prepare the available physical seat sequence across all allocated rooms
        List<Map.Entry<Room, PhysicalSeat>> eligibleSeats = new ArrayList<>();
        boolean applySpacing = (strategy == SeatingStrategy.SPACED_GAP || spacingGap > 0);

        for (Map.Entry<Room, List<PhysicalSeat>> entry : roomSeatsMap.entrySet()) {
            Room room = entry.getKey();
            List<PhysicalSeat> seats = entry.getValue();

            // Filter only available active seats that are not marked as BLOCKED or MAINTENANCE
            int seatCounter = 0;
            for (PhysicalSeat seat : seats) {
                if (seat.isActive() && seat.getStatus() == SeatStatus.AVAILABLE) {
                    if (!applySpacing || (seatCounter % 2 == 0)) {
                        eligibleSeats.add(Map.entry(room, seat));
                    }
                    seatCounter++;
                }
            }
        }

        if (eligibleSeats.isEmpty()) {
            throw new IllegalStateException("No available physical seats found in the assigned exam rooms.");
        }

        if (orderedStudents.size() > eligibleSeats.size()) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient seating capacity! Candidates to seat: %d, Available seats: %d. Please assign additional exam rooms or reduce spacing.",
                    orderedStudents.size(), eligibleSeats.size()
            ));
        }

        // 3. Map students to seats
        List<SeatingAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < orderedStudents.size(); i++) {
            Student candidate = orderedStudents.get(i);
            Map.Entry<Room, PhysicalSeat> seatEntry = eligibleSeats.get(i);
            assignments.add(new SeatingAssignment(candidate, seatEntry.getKey(), seatEntry.getValue()));
        }

        return assignments;
    }

    /**
     * Orders candidate students to achieve anti-cheating separation or roll number order.
     */
    private List<Student> orderCandidates(List<Student> rawList, SeatingStrategy strategy) {
        List<Student> candidates = new ArrayList<>(rawList);

        if (strategy == SeatingStrategy.ROLL_NUMBER_ASCENDING) {
            candidates.sort(Comparator.comparing(s -> s.getRollNumber() != null ? s.getRollNumber() : ""));
            return candidates;
        }

        if (strategy == SeatingStrategy.ALTERNATING_SECTION) {
            // Group students by section
            Map<String, Queue<Student>> sectionQueues = new LinkedHashMap<>();
            // Sort internally by roll number
            candidates.sort(Comparator.comparing(s -> s.getRollNumber() != null ? s.getRollNumber() : ""));

            for (Student s : candidates) {
                String sec = s.getSectionName() != null ? s.getSectionName() : "DEFAULT";
                sectionQueues.computeIfAbsent(sec, k -> new LinkedList<>()).add(s);
            }

            // Interleave sections round-robin: Sec A student, Sec B student, Sec C student, Sec A student...
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
        candidates.sort(Comparator.comparing(s -> s.getRollNumber() != null ? s.getRollNumber() : ""));
        return candidates;
    }
}
