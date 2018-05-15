package com.sap.sailing.windestimation.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.ManeuverAndPolarsBasedWindEstimator;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sse.common.TimePoint;

public class WindEstimationEvaluatorImpl implements WindEstimatorEvaluator {

    private final double maxWindCourseDeviationInDegrees;
    private final double maxWindSpeedDeviationInKnots;
    private final double minAccuracyPerRaceForCorrectEstimation;

    public WindEstimationEvaluatorImpl(double maxWindCourseDeviationInDegrees, double maxWindSpeedDeviationInKnots,
            double minAccuracyPerRaceForCorrectEstimation) {
        this.maxWindCourseDeviationInDegrees = maxWindCourseDeviationInDegrees;
        this.maxWindSpeedDeviationInKnots = maxWindSpeedDeviationInKnots;
        this.minAccuracyPerRaceForCorrectEstimation = minAccuracyPerRaceForCorrectEstimation;
    }

    @Override
    public WindEstimatorEvaluationResult evaluateWindEstimator(
            ManeuverAndPolarsBasedWindEstimatorFactory windEstimatorFactory, List<RaceWithEstimationData> testSet) {
        return testSet.parallelStream()
                .map(race -> evaluateOnRace(windEstimatorFactory.createNewEstimatorInstance(), race))
                .reduce((one, two) -> one.mergeBySum(two)).orElse(new WindEstimatorEvaluationResult());
    }

    private WindEstimatorEvaluationResult evaluateOnRace(ManeuverAndPolarsBasedWindEstimator windEstimator,
            RaceWithEstimationData raceWithEstimationData) {
        Map<TimePoint, Wind> targetWindPerTimePoint = new HashMap<>();
        for (CompetitorTrackWithEstimationData competitorTrackWithEstimationData : raceWithEstimationData
                .getCompetitorTracks()) {
            for (CompleteManeuverCurveWithEstimationData maneuver : competitorTrackWithEstimationData
                    .getManeuverCurves()) {
                targetWindPerTimePoint.put(maneuver.getTimePoint(), maneuver.getWind());
            }
        }
        List<WindWithConfidence<TimePoint>> windTrack = windEstimator
                .estimateWind(raceWithEstimationData.getCompetitorTracks());
        WindEstimatorEvaluationResult result = new WindEstimatorEvaluationResult();
        for (WindWithConfidence<TimePoint> windWithConfidence : windTrack) {
            Wind estimatedWind = windWithConfidence.getObject();
            Wind targetWind = targetWindPerTimePoint.get(estimatedWind.getTimePoint());
            if (targetWind.getBearing().getDegrees() > 0.00001) {
                double windCourseDeviationInDegrees = targetWind.getBearing()
                        .getDifferenceTo(estimatedWind.getBearing()).getDegrees();
                boolean windCourseDeviationWithinTolerance = Math
                        .abs(windCourseDeviationInDegrees) <= maxWindCourseDeviationInDegrees;
                if (targetWind.getKnots() > 0.00001) {
                    double windSpeedDeviationInKnots = targetWind.getKnots() - estimatedWind.getKnots();
                    boolean windSpeedDeviationWithinTolerance = Math
                            .abs(windSpeedDeviationInKnots) <= maxWindSpeedDeviationInKnots;
                    result = result.mergeBySum(new WindEstimatorEvaluationResult(windCourseDeviationInDegrees,
                            windCourseDeviationWithinTolerance, windSpeedDeviationInKnots,
                            windSpeedDeviationWithinTolerance));
                } else {
                    result = result.mergeBySum(new WindEstimatorEvaluationResult(windCourseDeviationInDegrees,
                            windCourseDeviationWithinTolerance));
                }
            }
        }
        return result.getAvgAsSingleResult(minAccuracyPerRaceForCorrectEstimation - 0.00001);
    }

}
