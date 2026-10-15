package com.campusgrid.model;

public enum SeatingStrategy {
    ALTERNATING_SECTION,     // Interleaves students of different sections/branches (Anti-cheating)
    ROLL_NUMBER_ASCENDING,   // Sequential seat allocation by student roll number
    SPACED_GAP               // Leaves 1 empty seat gap between candidates (50% density spacing)
}
