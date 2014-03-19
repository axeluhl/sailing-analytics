package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.Race;

public class RaceImpl implements Race {
    private final String description;
    private final String raceId;

    public RaceImpl(String raceId, String description) {
        super();
        this.raceId = raceId;
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getRaceID() {
        return raceId;
    }
}
