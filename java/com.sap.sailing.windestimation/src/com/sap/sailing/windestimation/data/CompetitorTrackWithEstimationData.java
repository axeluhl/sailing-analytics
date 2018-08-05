package com.sap.sailing.windestimation.data;

import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.impl.MeterPerSecondSpeedImpl;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompetitorTrackWithEstimationData<T> {

    private final String competitorName;
    private final BoatClass boatClass;
    private final List<T> elements;
    private final double avgIntervalBetweenFixesInSeconds;
    private final Distance distanceTravelled;
    private final TimePoint trackStartTimePoint;
    private final TimePoint trackEndTimePoint;
    private final long fixesCountForPolars;
    private final int markPassingsCount;
    private final int waypointsCount;

    public CompetitorTrackWithEstimationData(String competitorName, BoatClass boatClass, List<T> elements,
            double avgIntervalBetweenFixesInSeconds, Distance distanceTravelled, TimePoint trackStartTimePoint,
            TimePoint trackEndTimePoint, long fixesCountForPolars, int markPassingsCount, int waypointsCount) {
        this.competitorName = competitorName;
        this.boatClass = boatClass;
        this.elements = elements;
        this.avgIntervalBetweenFixesInSeconds = avgIntervalBetweenFixesInSeconds;
        this.distanceTravelled = distanceTravelled;
        this.trackStartTimePoint = trackStartTimePoint;
        this.trackEndTimePoint = trackEndTimePoint;
        this.fixesCountForPolars = fixesCountForPolars;
        this.markPassingsCount = markPassingsCount;
        this.waypointsCount = waypointsCount;
    }

    public String getCompetitorName() {
        return competitorName;
    }

    public BoatClass getBoatClass() {
        return boatClass;
    }

    public List<T> getElements() {
        return elements;
    }

    public double getAvgIntervalBetweenFixesInSeconds() {
        return avgIntervalBetweenFixesInSeconds;
    }

    public Distance getDistanceTravelled() {
        return distanceTravelled;
    }

    public Duration getDuration() {
        return trackStartTimePoint == null || trackEndTimePoint == null ? Duration.NULL
                : trackStartTimePoint.until(trackEndTimePoint);
    }

    public TimePoint getTrackStartTimePoint() {
        return trackStartTimePoint;
    }

    public TimePoint getTrackEndTimePoint() {
        return trackEndTimePoint;
    }

    public long getFixesCountForPolars() {
        return fixesCountForPolars;
    }

    public int getMarkPassingsCount() {
        return markPassingsCount;
    }

    public int getWaypointsCount() {
        return waypointsCount;
    }

    public boolean isClean() {
        return waypointsCount == markPassingsCount && markPassingsCount > 1 && avgIntervalBetweenFixesInSeconds < 8
                && new MeterPerSecondSpeedImpl(
                        distanceTravelled.getMeters() / trackEndTimePoint.until(trackStartTimePoint).asSeconds())
                                .getKnots() > 1;
    }

}
