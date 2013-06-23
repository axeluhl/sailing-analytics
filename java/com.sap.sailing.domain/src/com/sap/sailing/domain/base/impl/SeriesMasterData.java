package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Fleet;

public class SeriesMasterData {

    private String name;
    private boolean isMedal;
    private Iterable<Fleet> fleets;
    private Iterable<String> raceColumnNames;

    public SeriesMasterData(String name, boolean isMedal, Iterable<Fleet> fleets, Iterable<String> raceColumnNames) {
        this.name = name;
        this.isMedal = isMedal;
        this.fleets = fleets;
        this.raceColumnNames = raceColumnNames;
    }

    public String getName() {
        return name;
    }

    public boolean isMedal() {
        return isMedal;
    }

    public Iterable<Fleet> getFleets() {
        return fleets;
    }

    public Iterable<String> getRaceColumnNames() {
        return raceColumnNames;
    }

}
