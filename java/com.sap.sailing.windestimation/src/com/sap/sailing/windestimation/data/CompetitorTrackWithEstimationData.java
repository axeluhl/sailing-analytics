package com.sap.sailing.windestimation.data;

import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompetitorTrackWithEstimationData {

    private final String competitorName;
    private final BoatClass boatClass;
    private final List<CompleteManeuverCurveWithEstimationData> maneuverCurves;
    private final double avgIntervalBetweenFixesInSeconds;
    private final Distance distanceTravelled;
    private final TimePoint trackStartTimePoint;
    private final TimePoint trackEndTimePoint;

    public CompetitorTrackWithEstimationData(String competitorName, BoatClass boatClass,
            List<CompleteManeuverCurveWithEstimationData> maneuverCurves, double avgIntervalBetweenFixesInSeconds,
            Distance distanceTravelled, TimePoint trackStartTimePoint, TimePoint trackEndTimePoint) {
        this.competitorName = competitorName;
        this.boatClass = boatClass;
        this.maneuverCurves = maneuverCurves;
        this.avgIntervalBetweenFixesInSeconds = avgIntervalBetweenFixesInSeconds;
        this.distanceTravelled = distanceTravelled;
        this.trackStartTimePoint = trackStartTimePoint;
        this.trackEndTimePoint = trackEndTimePoint;
    }

    public String getCompetitorName() {
        return competitorName;
    }

    public BoatClass getBoatClass() {
        return boatClass;
    }

    public List<CompleteManeuverCurveWithEstimationData> getManeuverCurves() {
        return maneuverCurves;
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

}
