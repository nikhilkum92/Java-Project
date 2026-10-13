package com.campusgrid.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "idx_rooms_status", columnList = "status"),
        @Index(name = "idx_rooms_type", columnList = "room_type")
})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(name = "room_code", nullable = false, unique = true, length = 50)
    private String roomCode;

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 30)
    private RoomType roomType;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "system_count", nullable = false)
    private int systemCount = 0;

    @Column(name = "rows_count", nullable = false)
    private int rowsCount = 6;

    @Column(name = "columns_count", nullable = false)
    private int columnsCount = 6;

    @Enumerated(EnumType.STRING)
    @Column(name = "layout_template", nullable = false, length = 50)
    private LayoutTemplate layoutTemplate = LayoutTemplate.STANDARD_GRID;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column(name = "facilities", length = 500)
    private String facilities;

    @Column(name = "maintenance_reason", length = 255)
    private String maintenanceReason;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PhysicalSeat> seats = new ArrayList<>();

    public Room() {
    }

    public Room(Floor floor, String roomCode, String roomName, RoomType roomType, int capacity, LayoutTemplate layoutTemplate) {
        this.floor = floor;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.roomType = roomType;
        this.capacity = capacity;
        this.layoutTemplate = layoutTemplate != null ? layoutTemplate : LayoutTemplate.STANDARD_GRID;
        this.status = RoomStatus.AVAILABLE;
        this.active = true;
    }

    public Room(Floor floor, String roomCode, String roomName, RoomType roomType, int capacity, String facilities) {
        this(floor, roomCode, roomName, roomType, capacity, LayoutTemplate.STANDARD_GRID);
        this.facilities = facilities;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(int systemCount) {
        this.systemCount = systemCount;
    }

    public int getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(int rowsCount) {
        this.rowsCount = rowsCount;
    }

    public int getColumnsCount() {
        return columnsCount;
    }

    public void setColumnsCount(int columnsCount) {
        this.columnsCount = columnsCount;
    }

    public LayoutTemplate getLayoutTemplate() {
        return layoutTemplate;
    }

    public void setLayoutTemplate(LayoutTemplate layoutTemplate) {
        this.layoutTemplate = layoutTemplate;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getMaintenanceReason() {
        return maintenanceReason;
    }

    public void setMaintenanceReason(String maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<PhysicalSeat> getSeats() {
        return seats;
    }

    public void setSeats(List<PhysicalSeat> seats) {
        this.seats = seats;
    }
}
