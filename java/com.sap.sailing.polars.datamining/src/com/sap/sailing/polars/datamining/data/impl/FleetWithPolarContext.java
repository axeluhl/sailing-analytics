package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.HasFleetPolarContext;

public class FleetWithPolarContext implements HasFleetPolarContext {
    
    private final Fleet fleet;
    private final RaceColumn raceColumn;

    public FleetWithPolarContext(Fleet fleet, RaceColumn raceColumn) {
        this.fleet = fleet;
        this.raceColumn = raceColumn;
    }

    @Override
    public Fleet getFleet() {
        return fleet;
    }

    @Override
    public RaceColumn getRaceColumn() {
        return raceColumn;
    }

    @Override
    public TrackedRace getTrackedRace() {
        return raceColumn.getTrackedRace(fleet);
    }

    @Override
    public BoatClass getBoatClass() {
        return getTrackedRace().getRace().getBoatClass();
    }

}
