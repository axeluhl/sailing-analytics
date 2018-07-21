package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.HasFleetPolarContext;
import com.sap.sailing.polars.datamining.data.HasRaceColumnPolarContext;

public class FleetWithPolarContext implements HasFleetPolarContext {
    
    private final Fleet fleet;
    private final RaceColumn raceColumn;
    private final HasRaceColumnPolarContext raceColumnPolarContext;

    public FleetWithPolarContext(Fleet fleet, RaceColumn raceColumn, HasRaceColumnPolarContext raceColumnPolarContext) {
        this.fleet = fleet;
        this.raceColumn = raceColumn;
        this.raceColumnPolarContext = raceColumnPolarContext;
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
    public HasRaceColumnPolarContext getRaceColumnPolarContext() {
        return raceColumnPolarContext;
    }

}
