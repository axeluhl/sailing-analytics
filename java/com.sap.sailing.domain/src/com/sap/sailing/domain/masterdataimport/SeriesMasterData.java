package com.sap.sailing.domain.masterdataimport;

import com.sap.sailing.domain.base.Fleet;

public class SeriesMasterData {

    private String name;
    private boolean isMedal;
    private Iterable<Fleet> fleets;
    private Iterable<RaceColumnMasterData> raceColumns;
    private int[] discardingRule;

    public SeriesMasterData(String name, boolean isMedal, Iterable<Fleet> fleets, Iterable<RaceColumnMasterData> raceColumns, int[] discardingRule) {
        this.name = name;
        this.isMedal = isMedal;
        this.fleets = fleets;
        this.raceColumns = raceColumns;
        this.discardingRule = discardingRule;
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

    public Iterable<RaceColumnMasterData> getRaceColumnNames() {
        return raceColumns;
    }

    public int[] getDiscardingRule() {
        return discardingRule;
    }

    
}
