package com.campusgrid.dto;

import com.campusgrid.model.SeatingStrategy;
import java.util.List;

public class GenerateExamSeatingRequest {

    private SeatingStrategy strategy = SeatingStrategy.ALTERNATING_SECTION;

    private List<Long> roomIds;

    private List<String> sectionNames;

    private int spacingGap = 0;

    public GenerateExamSeatingRequest() {
    }

    public GenerateExamSeatingRequest(SeatingStrategy strategy, List<Long> roomIds, List<String> sectionNames, int spacingGap) {
        this.strategy = strategy != null ? strategy : SeatingStrategy.ALTERNATING_SECTION;
        this.roomIds = roomIds;
        this.sectionNames = sectionNames;
        this.spacingGap = spacingGap;
    }

    public SeatingStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(SeatingStrategy strategy) {
        this.strategy = strategy;
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
