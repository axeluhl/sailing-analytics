package com.sap.sailing.domain.base;

public interface Competitor extends Named, WithID {
    Team getTeam();

    Boat getBoat();

}
