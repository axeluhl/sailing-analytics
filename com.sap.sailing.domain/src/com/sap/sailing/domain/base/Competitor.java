package com.sap.sailing.domain.base;

public interface Competitor extends Named {
    Team getTeam();

    Boat getBoat();

    /**
     * Something that uniquely identifies this competitor beyond his name
     */
    Object getId();
}
