package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.polars.datamining.data.HasRaceColumnPolarContext;

public class RaceColumnWithPolarContext implements HasRaceColumnPolarContext {
    
    private final RaceColumn raceColumn;

    public RaceColumnWithPolarContext(RaceColumn raceColumn) {
        this.raceColumn = raceColumn;
    }

    @Override
    public RaceColumn getRaceColumn() {
        return raceColumn;
    }

}
