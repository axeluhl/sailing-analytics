package com.sap.sailing.domain.base.racegroup.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.RaceRow;

public class RaceRowImpl implements RaceRow {

    private Fleet fleet;
    private Iterable<RaceCell> races;

    /**
     * @param races
     *            expected to be provided in the order in which the race columns appear in the series / leaderboard
     */
    public RaceRowImpl(Fleet fleet, Iterable<RaceCell> races) {
        this.fleet = fleet;
        this.races = races;
    }

    @Override
    public Fleet getFleet() {
        return fleet;
    }

    @Override
    public Iterable<RaceCell> getCells() {
        return races;
    }

    @Override
    public String toString() {
        return "RaceRowImpl [fleet=" + fleet + ", races=" + races + "]";
    }

}
