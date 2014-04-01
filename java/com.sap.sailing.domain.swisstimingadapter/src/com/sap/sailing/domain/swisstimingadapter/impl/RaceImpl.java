package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.swisstimingadapter.Race;

public class RaceImpl implements Race {
    private final String description;
    private final String raceId;
    private final BoatClass boatClass;

    public RaceImpl(String raceId, String description, BoatClass boatClass) {
        this.raceId = raceId;
        this.description = description;
        this.boatClass = boatClass;
    }

    public RaceImpl(String raceId, String description) {
        this(raceId, description, null);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getRaceID() {
        return raceId;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }
}
