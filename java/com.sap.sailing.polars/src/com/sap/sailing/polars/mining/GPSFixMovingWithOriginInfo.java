package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;

public class GPSFixMovingWithOriginInfo {

    private final GPSFixMoving fix;
    private final TrackedRace trackedRace;
    private final Competitor competitor;

    public GPSFixMovingWithOriginInfo(GPSFixMoving fix, TrackedRace trackedRace, Competitor competitor) {
        this.fix = fix;
        this.trackedRace = trackedRace;
        this.competitor = competitor;
    }

    public GPSFixMoving getFix() {
        return fix;
    }

    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    public Competitor getCompetitor() {
        return competitor;
    }
}
