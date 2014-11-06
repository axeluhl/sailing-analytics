package com.sap.sailing.polars.mining;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
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
    private final Set<WindSource> windSourcesToExcludeForBearing;
    private final Set<WindSource> windSourcesToExcludeForSpeed;

    public GPSFixMovingWithPolarContext(GPSFixMoving fix, TrackedRace race, Competitor competitor,
            ClusterGroup<Speed> windSpeedClusterGroup) {
        this.fix = fix;
        this.race = race;
        this.competitor = competitor;
        this.windSpeedClusterGroup = windSpeedClusterGroup;
        this.windSourcesToExcludeForBearing = collectWindSourcesToIgnoreForBearing();
        this.windSourcesToExcludeForSpeed = collectWindSourcesToIgnoreForSpeed();
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
        Wind wind = race.getWind(fix.getPosition(), fix.getTimePoint(), windSourcesToExcludeForBearing);
        Bearing result = null;
        if (wind != null && boatSpeed != null) {
            Bearing bearing = boatSpeed.getBearing();
            result = wind.getFrom().getDifferenceTo(bearing);
        }
        return result;
    }

    @Override
    public Cluster<Speed> getWindSpeedCluster() {
        Speed windSpeed = getWindSpeed();
        return windSpeedClusterGroup.getClusterFor(windSpeed);
    }

    public Speed getWindSpeed() {
        Wind wind = race.getWind(fix.getPosition(), fix.getTimePoint(), windSourcesToExcludeForSpeed);
        return wind;
    }

    public Speed getBoatSpeed() {
        return race.getTrack(competitor).getEstimatedSpeed(fix.getTimePoint());
    }

    @Override
    public BoatClass getBoatClass() {
        return race.getRace().getBoatClass();
    }
    
    private Set<WindSource> collectWindSourcesToIgnoreForBearing() {
        Set<WindSource> windSourcesToExclude = new HashSet<WindSource>();
        Iterable<WindSource> combinedSources = race.getWindSources(WindSourceType.COMBINED);
        for (WindSource combinedSource : combinedSources) {
            windSourcesToExclude.add(combinedSource);
        }
        Iterable<WindSource> courseSources = race.getWindSources(WindSourceType.COURSE_BASED);
        for (WindSource courseSource : courseSources) {
            windSourcesToExclude.add(courseSource);
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
    
    private Set<WindSource> collectWindSourcesToIgnoreForSpeed() {
        Set<WindSource> windSourcesToExclude = new HashSet<WindSource>();
        Iterable<WindSource> combinedSources = race.getWindSources(WindSourceType.COMBINED);
        for (WindSource combinedSource : combinedSources) {
            windSourcesToExclude.add(combinedSource);
        }
        Iterable<WindSource> courseSources = race.getWindSources(WindSourceType.COURSE_BASED);
        for (WindSource courseSource : courseSources) {
            windSourcesToExclude.add(courseSource);
        }
        Iterable<WindSource> trackBasedSources = race.getWindSources(WindSourceType.TRACK_BASED_ESTIMATION);
        for (WindSource trackBasedSource : trackBasedSources) {
            windSourcesToExclude.add(trackBasedSource);
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

}
