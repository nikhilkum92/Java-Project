package com.campusgrid.dto;

import com.campusgrid.model.RoomStatus;
import com.campusgrid.model.RoomType;

import java.util.ArrayList;
import java.util.List;

public class CampusTreeDto {
    private Long id;
    private String name;
    private String code;
    private String address;
    private List<BlockTreeDto> blocks = new ArrayList<>();

    public CampusTreeDto() {
    }

    public CampusTreeDto(Long id, String name, String code, String address) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<BlockTreeDto> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockTreeDto> blocks) {
        this.blocks = blocks;
    }

    public static class BlockTreeDto {
        private Long id;
        private String name;
        private String code;
        private String description;
        private List<FloorTreeDto> floors = new ArrayList<>();

        public BlockTreeDto() {
        }

        public BlockTreeDto(Long id, String name, String code, String description) {
            this.id = id;
            this.name = name;
            this.code = code;
            this.description = description;
        }

        public BlockTreeDto(Long id, String name, String code) {
            this(id, name, code, null);
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<FloorTreeDto> getFloors() {
            return floors;
        }

        public void setFloors(List<FloorTreeDto> floors) {
            this.floors = floors;
        }
    }

    public static class FloorTreeDto {
        private Long id;
        private String name;
        private int floorNumber;
        private int roomCount;
        private List<RoomTreeDto> rooms = new ArrayList<>();

        public FloorTreeDto() {
        }

        public FloorTreeDto(Long id, String name, int floorNumber) {
            this.id = id;
            this.name = name;
            this.floorNumber = floorNumber;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getFloorNumber() {
            return floorNumber;
        }

        public void setFloorNumber(int floorNumber) {
            this.floorNumber = floorNumber;
        }

        public int getRoomCount() {
            return roomCount;
        }

        public void setRoomCount(int roomCount) {
            this.roomCount = roomCount;
        }

        public List<RoomTreeDto> getRooms() {
            return rooms;
        }

        public void setRooms(List<RoomTreeDto> rooms) {
            this.rooms = rooms;
        }
    }

    public static class RoomTreeDto {
        private Long id;
        private String roomCode;
        private String roomName;
        private RoomType roomType;
        private int capacity;
        private RoomStatus status;

        public RoomTreeDto() {
        }

        public RoomTreeDto(Long id, String roomCode, String roomName, RoomType roomType, int capacity, RoomStatus status) {
            this.id = id;
            this.roomCode = roomCode;
            this.roomName = roomName;
            this.roomType = roomType;
            this.capacity = capacity;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public RoomStatus getStatus() {
            return status;
        }

        public void setStatus(RoomStatus status) {
            this.status = status;
        }
    }
}
