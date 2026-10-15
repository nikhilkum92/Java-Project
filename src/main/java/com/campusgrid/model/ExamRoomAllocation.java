package com.campusgrid.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_room_allocations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_exam_room", columnNames = {"exam_id", "room_id"})
})
public class ExamRoomAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private ExamSession examSession;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invigilator_id")
    private Faculty invigilator;

    @Column(name = "allocated_capacity", nullable = false)
    private int allocatedCapacity;

    @Column(name = "notes", length = 300)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ExamRoomAllocation() {
    }

    public ExamRoomAllocation(ExamSession examSession, Room room, Faculty invigilator, int allocatedCapacity, String notes) {
        this.examSession = examSession;
        this.room = room;
        this.invigilator = invigilator;
        this.allocatedCapacity = allocatedCapacity > 0 ? allocatedCapacity : (room != null ? room.getCapacity() : 0);
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.allocatedCapacity <= 0 && this.room != null) {
            this.allocatedCapacity = this.room.getCapacity();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExamSession getExamSession() {
        return examSession;
    }

    public void setExamSession(ExamSession examSession) {
        this.examSession = examSession;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Faculty getInvigilator() {
        return invigilator;
    }

    public void setInvigilator(Faculty invigilator) {
        this.invigilator = invigilator;
    }

    public int getAllocatedCapacity() {
        return allocatedCapacity;
    }

    public void setAllocatedCapacity(int allocatedCapacity) {
        this.allocatedCapacity = allocatedCapacity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
