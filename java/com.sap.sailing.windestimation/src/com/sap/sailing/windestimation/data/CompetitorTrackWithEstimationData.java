package com.sap.sailing.windestimation.data;

import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

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

    public CompetitorTrackWithEstimationData(String competitorName, BoatClass boatClass,
            List<CompleteManeuverCurveWithEstimationData> maneuverCurves, double avgIntervalBetweenFixesInSeconds) {
        this.competitorName = competitorName;
        this.boatClass = boatClass;
        this.maneuverCurves = maneuverCurves;
        this.avgIntervalBetweenFixesInSeconds = avgIntervalBetweenFixesInSeconds;
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

}
