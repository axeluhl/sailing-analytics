package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.swisstimingadapter.Race;

public class RaceImpl implements Race {
    private final String description;
    private final String raceId;

    private TimePoint startTime;

    public RaceImpl(String raceId, String description) {
        super();
        this.raceId = raceId;
        this.description = description;
    }

    public RaceImpl(String raceId, String description, TimePoint startTime) {
        super();
        this.raceId = raceId;
        this.description = description;
        this.startTime = startTime;
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
    public TimePoint getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(TimePoint startTime) {
        this.startTime = startTime;
    }
}
