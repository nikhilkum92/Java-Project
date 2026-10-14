package com.campusgrid.dto;

public class TimetableStatsDto {

    private long totalEntries;
    private long scheduledEntries;
    private long cancelledEntries;
    private long allocatedRoomsCount;
    private long unallocatedEntriesCount;
    private double totalHoursScheduled;

    public TimetableStatsDto() {
    }

    public TimetableStatsDto(long totalEntries, long scheduledEntries, long cancelledEntries,
                             long allocatedRoomsCount, long unallocatedEntriesCount, double totalHoursScheduled) {
        this.totalEntries = totalEntries;
        this.scheduledEntries = scheduledEntries;
        this.cancelledEntries = cancelledEntries;
        this.allocatedRoomsCount = allocatedRoomsCount;
        this.unallocatedEntriesCount = unallocatedEntriesCount;
        this.totalHoursScheduled = totalHoursScheduled;
    }

    public long getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(long totalEntries) {
        this.totalEntries = totalEntries;
    }

    public long getScheduledEntries() {
        return scheduledEntries;
    }

    public void setScheduledEntries(long scheduledEntries) {
        this.scheduledEntries = scheduledEntries;
    }

    public long getCancelledEntries() {
        return cancelledEntries;
    }

    public void setCancelledEntries(long cancelledEntries) {
        this.cancelledEntries = cancelledEntries;
    }

    public long getAllocatedRoomsCount() {
        return allocatedRoomsCount;
    }

    public void setAllocatedRoomsCount(long allocatedRoomsCount) {
        this.allocatedRoomsCount = allocatedRoomsCount;
    }

    public long getUnallocatedEntriesCount() {
        return unallocatedEntriesCount;
    }

    public void setUnallocatedEntriesCount(long unallocatedEntriesCount) {
        this.unallocatedEntriesCount = unallocatedEntriesCount;
    }

    public double getTotalHoursScheduled() {
        return totalHoursScheduled;
    }

    public void setTotalHoursScheduled(double totalHoursScheduled) {
        this.totalHoursScheduled = totalHoursScheduled;
    }
}
