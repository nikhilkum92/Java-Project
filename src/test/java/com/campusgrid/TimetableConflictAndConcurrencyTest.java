package com.campusgrid;

import com.campusgrid.dto.CreateTimetableRequest;
import com.campusgrid.dto.TimetableEntryDto;
import com.campusgrid.model.*;
import com.campusgrid.repository.*;
import com.campusgrid.service.TimetableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimetableConflictAndConcurrencyTest {

    @Mock
    private TimetableEntryRepository timetableEntryRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private TimetableService timetableService;

    private Subject subject;
    private Room room;

    @BeforeEach
    void setUp() {
        subject = new Subject("CS301", "Data Structures", "Computer Science", 3, SubjectType.THEORY, 4);
        subject.setId(1L);

        Floor floor = new Floor(new Block(new Campus("Main", "M", "Center"), "B1", "1"), 1, "F1");
        room = new Room(floor, "R-101", "Lecture Room 1", RoomType.CLASSROOM, 60, LayoutTemplate.STANDARD_GRID);
        room.setId(101L);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setActive(true);
    }

    @Test
    @DisplayName("Should prevent race condition when concurrent requests attempt to allocate the same room")
    void testConcurrentRoomAllocationSafety() throws InterruptedException {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(roomRepository.findById(101L)).thenReturn(Optional.of(room));

        // State simulation: after first save, repository will return the conflict
        AtomicInteger callCount = new AtomicInteger(0);
        TimetableEntry savedEntry = new TimetableEntry(subject, null, room, "Computer Science", "CSE-A",
                2, 3, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), ClassType.LECTURE, 40);

        when(timetableEntryRepository.findConflictingRoomEntries(any(), any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    if (callCount.get() > 0) {
                        return List.of(savedEntry);
                    }
                    return Collections.emptyList();
                });

        when(timetableEntryRepository.save(any(TimetableEntry.class))).thenAnswer(invocation -> {
            callCount.incrementAndGet();
            TimetableEntry e = invocation.getArgument(0);
            e.setId(1001L);
            return e;
        });

        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    CreateTimetableRequest req = new CreateTimetableRequest();
                    req.setSubjectId(1L);
                    req.setRoomId(101L);
                    req.setDepartment("Computer Science");
                    req.setSectionName(index == 0 ? "CSE-A" : "CSE-B");
                    req.setDayOfWeek(DayOfWeek.MONDAY);
                    req.setStartTime(LocalTime.of(9, 0));
                    req.setEndTime(LocalTime.of(10, 0));
                    req.setClassType(ClassType.LECTURE);
                    req.setExpectedCapacity(40);

                    timetableService.createTimetableEntry(req);
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    if (e.getMessage().contains("Room conflict")) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Concurrent scheduling tasks did not complete in time");
        assertEquals(1, successCount.get(), "Exactly one allocation request must succeed");
        assertEquals(1, conflictCount.get(), "The concurrent conflicting request must be safely rejected");
    }
}
