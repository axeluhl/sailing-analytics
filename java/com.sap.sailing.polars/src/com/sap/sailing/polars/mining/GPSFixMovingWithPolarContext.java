package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.WindSteppingWithMaxDistance;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;

public class GPSFixMovingWithPolarContext implements PolarClusterKey {

    private final GPSFixMoving fix;
    private final TrackedRace race;
    private final Competitor competitor;
    private final PolarSheetGenerationSettings defaultPolarSheetGenerationSettings;

    public GPSFixMovingWithPolarContext(GPSFixMoving fix, TrackedRace race, Competitor competitor,
            PolarSheetGenerationSettings defaultPolarSheetGenerationSettings) {
        this.fix = fix;
        this.race = race;
        this.competitor = competitor;
        this.defaultPolarSheetGenerationSettings = defaultPolarSheetGenerationSettings;
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
    public WindSpeedLevel getWindSpeedLevel() {
        Speed windSpeed = getWindSpeed();
        WindSteppingWithMaxDistance stepping = defaultPolarSheetGenerationSettings.getWindStepping();
        return new WindSpeedLevel(windSpeed, stepping);
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
