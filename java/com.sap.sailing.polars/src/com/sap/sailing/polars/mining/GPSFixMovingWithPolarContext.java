package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.datamining.shared.annotations.Dimension;

public class GPSFixMovingWithPolarContext {

    private final GPSFixMoving fix;
    private final TrackedRace race;
    private final Competitor competitor;

    public GPSFixMovingWithPolarContext(GPSFixMoving fix, TrackedRace race, Competitor competitor) {
        this.fix = fix;
        this.race = race;
        this.competitor = competitor;
    }

    public GPSFixMoving getFix() {
        return fix;
    }

    public TrackedRace getRace() {
        return race;
    }

    public Competitor getCompetitor() {
        return competitor;
    }

    @Dimension(messageKey = "roundedAngle")
    public int roundedAngleToTheWind() {
        SpeedWithBearing boatSpeed = race.getTrack(competitor).getEstimatedSpeed(fix.getTimePoint());
        Wind wind = race.getWind(fix.getPosition(), fix.getTimePoint());
        Bearing bearing = boatSpeed.getBearing();
        int roundedAngle = (int) Math.round(bearing.getDifferenceTo(wind.getBearing()).getDegrees());
        return roundedAngle;
    }

}
