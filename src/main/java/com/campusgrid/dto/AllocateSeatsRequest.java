package com.campusgrid.dto;

import com.campusgrid.model.SeatingStrategy;
import java.util.List;

public class AllocateSeatsRequest {

    private SeatingStrategy strategy = SeatingStrategy.ROLL_NUMBER_ASCENDING;

    private List<Long> candidateStudentIds;

    private List<Long> roomIds;

    private List<String> sectionNames;

    private int spacingGap = 0;

    public AllocateSeatsRequest() {
    }

    public AllocateSeatsRequest(SeatingStrategy strategy, List<Long> candidateStudentIds, List<Long> roomIds, List<String> sectionNames, int spacingGap) {
        this.strategy = strategy != null ? strategy : SeatingStrategy.ROLL_NUMBER_ASCENDING;
        this.candidateStudentIds = candidateStudentIds;
        this.roomIds = roomIds;
        this.sectionNames = sectionNames;
        this.spacingGap = spacingGap;
    }

    // Getters and Setters
    public SeatingStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(SeatingStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Long> getCandidateStudentIds() {
        return candidateStudentIds;
    }

    public void setCandidateStudentIds(List<Long> candidateStudentIds) {
        this.candidateStudentIds = candidateStudentIds;
    }

    public List<Long> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<Long> roomIds) {
        this.roomIds = roomIds;
    }

    public List<String> getSectionNames() {
        return sectionNames;
    }

    public void setSectionNames(List<String> sectionNames) {
        this.sectionNames = sectionNames;
    }

    public int getSpacingGap() {
        return spacingGap;
    }

    public void setSpacingGap(int spacingGap) {
        this.spacingGap = spacingGap;
    }
}
