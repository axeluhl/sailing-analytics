package com.sap.sailing.windestimation.evaluation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.WindEstimator;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationEvaluatorImpl<T> implements WindEstimatorEvaluator<T> {

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
    public WindEstimatorEvaluationResult evaluateWindEstimator(WindEstimatorFactory<T> windEstimatorFactory,
            TargetWindFixesExtractor<T> targetWindFixesExtractor, Iterator<RaceWithEstimationData<T>> racesIterator,
            long numberOfRaces) {
        return StreamSupport
                .stream(new FixedBatchSpliteratorWrapper<>(
                        Spliterators.spliterator(racesIterator, numberOfRaces, Spliterator.NONNULL), numberOfRaces, 50),
                        true)
                .map(race -> evaluateWindEstimator(windEstimatorFactory.createNewEstimatorInstance(),
                        targetWindFixesExtractor, race))
                .reduce((one, two) -> one.mergeBySum(two)).orElse(new WindEstimatorEvaluationResult());
    }

    @Override
    public WindEstimatorEvaluationResult evaluateWindEstimator(WindEstimator<T> windEstimator,
            TargetWindFixesExtractor<T> targetWindFixesExtractor, RaceWithEstimationData<T> raceWithEstimationData) {
        LoggingUtil.logInfo("Evaluating on " + raceWithEstimationData.getRegattaName() + " Race "
                + raceWithEstimationData.getRaceName());
        Map<TimePoint, Wind> targetWindPerTimePoint = new HashMap<>();
        for (CompetitorTrackWithEstimationData<T> competitorTrackWithEstimationData : raceWithEstimationData
                .getCompetitorTracks()) {
            List<Wind> targetWindFixes = targetWindFixesExtractor
                    .extractTargetWindFixes(competitorTrackWithEstimationData);
            for (Wind wind : targetWindFixes) {
                targetWindPerTimePoint.put(wind.getTimePoint(), wind);
            }
        }
        List<WindWithConfidence<Void>> windTrack = windEstimator.estimateWind(raceWithEstimationData);
        WindEstimatorEvaluationResult result = new WindEstimatorEvaluationResult();
        for (WindWithConfidence<Void> windWithConfidence : windTrack) {
            Wind estimatedWind = windWithConfidence.getObject();
            Wind targetWind = targetWindPerTimePoint.get(estimatedWind.getTimePoint());
            if (targetWind.getBearing().getDegrees() > 0.001) {
                double windCourseDeviationInDegrees = targetWind.getBearing()
                        .getDifferenceTo(estimatedWind.getBearing()).getDegrees();
                boolean windCourseDeviationWithinTolerance = Math
                        .abs(windCourseDeviationInDegrees) <= maxWindCourseDeviationInDegrees;
                double confidence = windWithConfidence.getConfidence();
                if (targetWind.getKnots() > 2) {
                    double windSpeedDeviationInKnots = targetWind.getKnots() - estimatedWind.getKnots();
                    boolean windSpeedDeviationWithinTolerance = Math
                            .abs(windSpeedDeviationInKnots) <= maxWindSpeedDeviationInKnots;
                    result = result.mergeBySum(new WindEstimatorEvaluationResult(windCourseDeviationInDegrees,
                            windCourseDeviationWithinTolerance, windSpeedDeviationInKnots,
                            windSpeedDeviationWithinTolerance, confidence));
                } else {
                    result = result.mergeBySum(new WindEstimatorEvaluationResult(windCourseDeviationInDegrees,
                            windCourseDeviationWithinTolerance, confidence));
                }
            }
        }
        LoggingUtil.logInfo("Evaluating on " + raceWithEstimationData.getRegattaName() + " Race "
                + raceWithEstimationData.getRaceName() + " succeeded");
        result.printEvaluationStatistics(true);
        return result.getAvgAsSingleResult(minAccuracyPerRaceForCorrectEstimation - 0.00001);
    }

}
