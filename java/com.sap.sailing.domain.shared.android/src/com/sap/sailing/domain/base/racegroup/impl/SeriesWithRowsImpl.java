package com.sap.sailing.domain.base.racegroup.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.racegroup.RaceRow;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;

public class SeriesWithRowsImpl implements SeriesWithRows {
    private static final long serialVersionUID = 8825402393444809944L;

    private String name;
    private Iterable<RaceRow> raceRows;
    private boolean isMedal;
    private boolean isFleetsCanRunInParallel;
    
    public SeriesWithRowsImpl(String name, boolean isMedal, boolean isFleetsCanRunInParallel, Iterable<RaceRow> raceRows) {
        this.name = name;
        this.raceRows = raceRows;
        this.isMedal = isMedal;
        this.isFleetsCanRunInParallel = isFleetsCanRunInParallel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isMedal() {
        return isMedal;
    }

    @Override
    public Iterable<RaceRow> getRaceRows() {
        return raceRows;
    }

    @Override
    public RaceRow getRaceRow(Fleet fleet) {
        for (final RaceRow row : getRaceRows()) {
            if (row.getFleet() == fleet) {
                return row;
            }
        }
        return null;
    }

    @Override
    public Iterable<? extends Fleet> getFleets() {
        Collection<Fleet> fleets = new ArrayList<Fleet>();
        for (RaceRow row : raceRows) {
            fleets.add(row.getFleet());
        }
        return fleets;
    }

    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    @Override
    public boolean isFleetsCanRunInParallel() {
        return isFleetsCanRunInParallel;
    }

    @Override
    public String toString() {
        return "SeriesWithRowsImpl [name=" + name + ", raceRows=" + raceRows + ", isMedal=" + isMedal
                + ", isFleetsCanRunInParallel=" + isFleetsCanRunInParallel + "]";
    }
}
