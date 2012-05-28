package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class SeriesImpl extends NamedImpl implements Series {
    private static final long serialVersionUID = -1640404303144907381L;
    private final Map<String, Fleet> fleetsByName;
    private final List<Fleet> fleetsInAscendingOrder;
    private final Iterable<RaceColumnInSeriesImpl> raceColumns;
    private boolean isMedal;
    
    public SeriesImpl(String name, boolean isMedal, Iterable<? extends Fleet> fleets, Iterable<String> raceColumnNames) {
        super(name);
        this.fleetsByName = new HashMap<String, Fleet>();
        for (Fleet fleet : fleets) {
            this.fleetsByName.put(fleet.getName(), fleet);
        }
        fleetsInAscendingOrder = new ArrayList<Fleet>(fleetsByName.values());
        Collections.sort(fleetsInAscendingOrder);
        List<RaceColumnInSeriesImpl> myRaceColumns = new ArrayList<RaceColumnInSeriesImpl>();
        for (String raceColumnName : raceColumnNames) {
            RaceColumnInSeriesImpl raceColumn = new RaceColumnInSeriesImpl(raceColumnName, this);
            myRaceColumns.add(raceColumn);
        }
        this.raceColumns = myRaceColumns;
        this.isMedal = isMedal;
    }

    public Iterable<? extends Fleet> getFleets() {
        return fleetsInAscendingOrder;
    }

    @Override
    public Fleet getFleetByName(String fleetName) {
        return fleetsByName.get(fleetName);
    }

    @Override
    public Iterable<? extends RaceColumn> getRaceColumns() {
        return raceColumns;
    }

    @Override
    public RaceColumn getRaceColumnByName(String columnName) {
        for (RaceColumn raceColumn : raceColumns) {
            if (raceColumn.getName().equals(columnName)) {
                return raceColumn;
            }
        }
        return null;
    }

    @Override
    public boolean isMedal() {
        return isMedal;
    }

    @Override
    public void setIsMedal(boolean isMedal) {
        this.isMedal = isMedal;
    }
}
