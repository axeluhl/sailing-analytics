package com.sap.sailing.domain.polarsheets;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;

public class PolarFix {

    private SpeedWithBearing boatSpeed;
    private Speed windSpeed;
    private double angleToWind;

    public PolarFix(GPSFixMoving fix, TrackedRace race) {
        boatSpeed = fix.getSpeed();
        Bearing bearing = boatSpeed.getBearing();
        Position position = fix.getPosition();
        windSpeed = race.getWind(position, fix.getTimePoint());
        Set<WindSource> windSourcesToExclude = collectWindSourcesToIgnoreForBearing(race, true);
        
        Wind windEstimated = race.getWind(position, fix.getTimePoint(), windSourcesToExclude);
        if (windEstimated == null) {
            windSourcesToExclude = collectWindSourcesToIgnoreForBearing(race, false);
            windEstimated = race.getWind(position, fix.getTimePoint(), windSourcesToExclude);
        }
        Bearing windBearing = windEstimated.getFrom();
        angleToWind = bearing.getDifferenceTo(windBearing).getDegrees();
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

    public SpeedWithBearing getBoatSpeed() {
        return boatSpeed;
    }

    public Speed getWindSpeed() {
        return windSpeed;
    }

    public double getAngleToWind() {
        return angleToWind;
    }
    
    

}
