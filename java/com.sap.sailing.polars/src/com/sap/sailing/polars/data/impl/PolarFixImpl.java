package com.sap.sailing.polars.data.impl;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.polars.data.PolarFix;

public class PolarFixImpl implements PolarFix {

    private SpeedWithBearing boatSpeed;
    private Speed windSpeed;
    private double angleToWind;
    private String gaugeIdString;
    private String dayString;

    public PolarFixImpl(GPSFixMoving fix, TrackedRace race, GPSFixTrack<Competitor, GPSFixMoving> track, Wind windSpeed,
            PolarSheetGenerationSettings settings, String gaugeIdString) {
        boatSpeed = track.getEstimatedSpeed(fix.getTimePoint());
        Bearing bearing = boatSpeed.getBearing();
        
        Position position = fix.getPosition();
        this.gaugeIdString = gaugeIdString;
        dayString = createDayString(race);
        this.windSpeed = windSpeed;
        Set<WindSource> windSourcesToExclude;
        if (settings.useOnlyEstimatedForWindDirection()) {
            windSourcesToExclude = collectWindSourcesToIgnoreForBearing(race, /* exclude course based */ true);
        } else {
            windSourcesToExclude = new HashSet<WindSource>();
        }
        
        Wind windEstimated = race.getWind(position, fix.getTimePoint(), windSourcesToExclude);
        if (windEstimated == null) {
            // no estimated wind; try to include course layout
            windSourcesToExclude = collectWindSourcesToIgnoreForBearing(race, /* exclude course based */ false);
            windEstimated = race.getWind(position, fix.getTimePoint(), windSourcesToExclude);
        }
        if (windEstimated == null) {
            windEstimated = windSpeed; // maybe no upwind start; need to default to measured wind speed/direction
        }
        Bearing windBearing = windEstimated.getFrom();
        angleToWind = bearing.getDifferenceTo(windBearing).getDegrees();
    }

    private String createDayString(TrackedRace race) {
        TimePoint startTimePoint = race.getStartOfRace();
        String resultDateString = "NoDate";
        if (startTimePoint != null) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
            resultDateString = fmt.format(startTimePoint.asDate());
        }
        
        return resultDateString;
    }

    public Set<WindSource> collectWindSourcesToIgnoreForBearing(TrackedRace race, boolean excludeCourseBased) {
        Set<WindSource> windSourcesToExclude = new HashSet<WindSource>();
        Iterable<WindSource> combinedSources = race.getWindSources(WindSourceType.COMBINED);
        for (WindSource combinedSource : combinedSources) {
            windSourcesToExclude.add(combinedSource);
        }
        if (excludeCourseBased) {
            Iterable<WindSource> courseSources = race.getWindSources(WindSourceType.COURSE_BASED);
            for (WindSource courseSource : courseSources) {
                windSourcesToExclude.add(courseSource);
            }
        }
        Iterable<WindSource> expSources = race.getWindSources(WindSourceType.EXPEDITION);
        for (WindSource expSource : expSources) {
            windSourcesToExclude.add(expSource);
        }
        Iterable<WindSource> rcSources = race.getWindSources(WindSourceType.RACECOMMITTEE);
        for (WindSource rcSource : rcSources) {
            windSourcesToExclude.add(rcSource);
        }
        Iterable<WindSource> webSources = race.getWindSources(WindSourceType.WEB);
        for (WindSource webSource : webSources) {
            windSourcesToExclude.add(webSource);
        }
        return windSourcesToExclude;
    }

    @Override
    public SpeedWithBearing getBoatSpeed() {
        return boatSpeed;
    }

    @Override
    public Speed getWindSpeed() {
        return windSpeed;
    }

    @Override
    public double getAngleToWind() {
        return angleToWind;
    }

    @Override
    public String getGaugeIdString() {
        return gaugeIdString;
    }  
    
    @Override
    public String getDayString() {
        return dayString;
    }

}
