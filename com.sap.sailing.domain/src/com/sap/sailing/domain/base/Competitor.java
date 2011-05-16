package com.sap.sailing.domain.base;

public interface Competitor extends Named {
    Team getTeam();

    Boat getBoat();
}
