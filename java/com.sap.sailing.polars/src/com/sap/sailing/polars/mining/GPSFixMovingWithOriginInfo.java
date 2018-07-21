package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Encapsulates a {@link GPSFixMoving} with more data needed for polar aggregation.
 * @author D054528 (Frederik Petersen)
 *
 */
public class GPSFixMovingWithOriginInfo {

    private final GPSFixMoving fix;
    private final TrackedRace trackedRace;
    private final Competitor competitor;
    private final Boat boat;

    public GPSFixMovingWithOriginInfo(GPSFixMoving fix, TrackedRace trackedRace, Competitor competitor) {
        this.fix = fix;
        this.trackedRace = trackedRace;
        this.competitor = competitor;
        this.boat = trackedRace.getBoatOfCompetitor(competitor);
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
    
    public Boat getBoat() {
        return boat;
    }
}
