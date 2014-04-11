package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;

public class GPSFixMovingWithPolarContext implements PolarClusterKey {

    private final GPSFixMoving fix;
    private final TrackedRace race;
    private final Competitor competitor;
    private final ClusterGroup<Speed> windSpeedClusterGroup;

    public GPSFixMovingWithPolarContext(GPSFixMoving fix, TrackedRace race, Competitor competitor,
            ClusterGroup<Speed> windSpeedClusterGroup) {
        this.fix = fix;
        this.race = race;
        this.competitor = competitor;
        this.windSpeedClusterGroup = windSpeedClusterGroup;
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

    @Override
    public RoundedAngleToTheWind getRoundedAngleToTheWind() {
        return new RoundedAngleToTheWind(getAngleToTheWind());
    }

    public Bearing getAngleToTheWind() {
        SpeedWithBearing boatSpeed = race.getTrack(competitor).getEstimatedSpeed(fix.getTimePoint());
        Wind wind = race.getWind(fix.getPosition(), fix.getTimePoint());
        Bearing bearing = boatSpeed.getBearing();
        return wind.getFrom().getDifferenceTo(bearing);
    }

    @Override
    public Cluster<Speed> getWindSpeedCluster() {
        Speed windSpeed = getWindSpeed();
        return windSpeedClusterGroup.getClusterFor(windSpeed);
    }

    public Speed getWindSpeed() {
        Wind wind = race.getWind(fix.getPosition(), fix.getTimePoint());
        return wind;
    }

    public Speed getBoatSpeed() {
        return race.getTrack(competitor).getEstimatedSpeed(fix.getTimePoint());
    }

    @Override
    public BoatClass getBoatClass() {
        return race.getRace().getBoatClass();
    }

}
