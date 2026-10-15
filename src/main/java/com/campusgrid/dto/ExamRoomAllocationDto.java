package com.campusgrid.dto;

public class ExamRoomAllocationDto {

    private Long id;
    private Long examId;
    private Long roomId;
    private String roomCode;
    private String roomName;
    private String blockName;
    private int floorNumber;
    private Long invigilatorId;
    private String invigilatorName;
    private String invigilatorEmployeeId;
    private int allocatedCapacity;
    private int roomTotalCapacity;
    private int usableSeatsCount;
    private int unavailableSeatsCount;
    private int seatedStudentsCount;
    private String notes;

    public ExamRoomAllocationDto() {
    }

    public ExamRoomAllocationDto(Long id, Long examId, Long roomId, String roomCode, String roomName,
                                 String blockName, int floorNumber, Long invigilatorId, String invigilatorName,
                                 String invigilatorEmployeeId, int allocatedCapacity, int roomTotalCapacity,
                                 int seatedStudentsCount, String notes) {
        this.id = id;
        this.examId = examId;
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.blockName = blockName;
        this.floorNumber = floorNumber;
        this.invigilatorId = invigilatorId;
        this.invigilatorName = invigilatorName;
        this.invigilatorEmployeeId = invigilatorEmployeeId;
        this.allocatedCapacity = allocatedCapacity;
        this.roomTotalCapacity = roomTotalCapacity;
        this.usableSeatsCount = roomTotalCapacity;
        this.unavailableSeatsCount = 0;
        this.seatedStudentsCount = seatedStudentsCount;
        this.notes = notes;
    }

    public ExamRoomAllocationDto(Long id, Long examId, Long roomId, String roomCode, String roomName,
                                 String blockName, int floorNumber, Long invigilatorId, String invigilatorName,
                                 String invigilatorEmployeeId, int allocatedCapacity, int roomTotalCapacity,
                                 int usableSeatsCount, int unavailableSeatsCount,
                                 int seatedStudentsCount, String notes) {
        this.id = id;
        this.examId = examId;
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.blockName = blockName;
        this.floorNumber = floorNumber;
        this.invigilatorId = invigilatorId;
        this.invigilatorName = invigilatorName;
        this.invigilatorEmployeeId = invigilatorEmployeeId;
        this.allocatedCapacity = allocatedCapacity;
        this.roomTotalCapacity = roomTotalCapacity;
        this.usableSeatsCount = usableSeatsCount;
        this.unavailableSeatsCount = unavailableSeatsCount;
        this.seatedStudentsCount = seatedStudentsCount;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
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

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public Long getInvigilatorId() {
        return invigilatorId;
    }

    public void setInvigilatorId(Long invigilatorId) {
        this.invigilatorId = invigilatorId;
    }

    public String getInvigilatorName() {
        return invigilatorName;
    }

    public void setInvigilatorName(String invigilatorName) {
        this.invigilatorName = invigilatorName;
    }

    public String getInvigilatorEmployeeId() {
        return invigilatorEmployeeId;
    }

    public void setInvigilatorEmployeeId(String invigilatorEmployeeId) {
        this.invigilatorEmployeeId = invigilatorEmployeeId;
    }

    public int getAllocatedCapacity() {
        return allocatedCapacity;
    }

    public void setAllocatedCapacity(int allocatedCapacity) {
        this.allocatedCapacity = allocatedCapacity;
    }

    public int getRoomTotalCapacity() {
        return roomTotalCapacity;
    }

    public void setRoomTotalCapacity(int roomTotalCapacity) {
        this.roomTotalCapacity = roomTotalCapacity;
    }

    public int getUsableSeatsCount() {
        return usableSeatsCount;
    }

    public void setUsableSeatsCount(int usableSeatsCount) {
        this.usableSeatsCount = usableSeatsCount;
    }

    public int getUnavailableSeatsCount() {
        return unavailableSeatsCount;
    }

    public void setUnavailableSeatsCount(int unavailableSeatsCount) {
        this.unavailableSeatsCount = unavailableSeatsCount;
    }

    public int getSeatedStudentsCount() {
        return seatedStudentsCount;
    }

    public void setSeatedStudentsCount(int seatedStudentsCount) {
        this.seatedStudentsCount = seatedStudentsCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
