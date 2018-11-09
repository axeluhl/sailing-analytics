package com.sap.sailing.windestimation.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.WindEstimator;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.preprocessing.RaceWithRandomClippingPreprocessingPipelineImpl;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationEvaluatorImpl<T> implements WindEstimatorEvaluator<T> {

    private final double maxWindCourseDeviationInDegrees;
    private final double maxWindSpeedDeviationInPercent;
    private final double minAccuracyPerRaceForCorrectEstimation;
    private final boolean evaluationPerCompetitorTrack;
    private final boolean randomClippingOfCompetitorTracks;
    private final int fixedNumberOfManeuvers;

    public WindEstimationEvaluatorImpl(double maxWindCourseDeviationInDegrees, double maxWindSpeedDeviationInPercent,
            double minAccuracyPerRaceForCorrectEstimation, boolean evaluationPerCompetitorTrack,
            boolean randomClippingOfCompetitorTracks, int fixedNumberOfManeuversPerTrack) {
        if (fixedNumberOfManeuversPerTrack > 0 && !randomClippingOfCompetitorTracks) {
            throw new IllegalArgumentException(
                    "fixedNumberOfManeuversPerTrack requires randomClippingOfCompetitorTracks to be true");
        }
        this.maxWindCourseDeviationInDegrees = maxWindCourseDeviationInDegrees;
        this.maxWindSpeedDeviationInPercent = maxWindSpeedDeviationInPercent;
        this.minAccuracyPerRaceForCorrectEstimation = minAccuracyPerRaceForCorrectEstimation;
        this.evaluationPerCompetitorTrack = evaluationPerCompetitorTrack;
        this.randomClippingOfCompetitorTracks = randomClippingOfCompetitorTracks;
        this.fixedNumberOfManeuvers = fixedNumberOfManeuversPerTrack;
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
            TargetWindFixesExtractor<T> targetWindFixesExtractor, RaceWithEstimationData<T> raceToEvaluate) {
        LoggingUtil
                .logInfo("Evaluating on " + raceToEvaluate.getRegattaName() + " Race " + raceToEvaluate.getRaceName());
        List<RaceWithEstimationData<T>> racesToEvaluate = new ArrayList<>();
        if (raceToEvaluate.getCompetitorTracks().size() <= 1 || !evaluationPerCompetitorTrack) {
            racesToEvaluate.add(raceToEvaluate);
        } else {
            for (CompetitorTrackWithEstimationData<T> competitorTrack : raceToEvaluate.getCompetitorTracks()) {
                RaceWithEstimationData<T> raceWithSingleCompetitorTrack = new RaceWithEstimationData<>(
                        raceToEvaluate.getRegattaName(), raceToEvaluate.getRaceName(),
                        Collections.singletonList(competitorTrack));
                racesToEvaluate.add(raceWithSingleCompetitorTrack);
            }
        }
        WindEstimatorEvaluationResult mergedResult = new WindEstimatorEvaluationResult();
        for (RaceWithEstimationData<T> raceWithEstimationData : racesToEvaluate) {
            if (randomClippingOfCompetitorTracks) {
                if (fixedNumberOfManeuvers == -1) {
                    raceWithEstimationData = new RaceWithRandomClippingPreprocessingPipelineImpl<T>(1,
                            Integer.MAX_VALUE).preprocessRace(raceWithEstimationData);
                } else {
                    raceWithEstimationData = new RaceWithRandomClippingPreprocessingPipelineImpl<T>(
                            fixedNumberOfManeuvers, fixedNumberOfManeuvers).preprocessRace(raceWithEstimationData);
                    List<CompetitorTrackWithEstimationData<T>> newCompetitorTracks = raceWithEstimationData
                            .getCompetitorTracks().stream()
                            .filter(competitorTrack -> competitorTrack.getElements().size() == fixedNumberOfManeuvers)
                            .collect(Collectors.toList());
                    if (newCompetitorTracks.isEmpty()) {
                        continue;
                    }
                    raceWithEstimationData = raceWithEstimationData.constructWithElements(newCompetitorTracks);
                }
            }
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
                        double windSpeedDeviationInPercent = Math.abs(targetWind.getKnots() - estimatedWind.getKnots())
                                / targetWind.getKnots();
                        boolean windSpeedDeviationWithinTolerance = windSpeedDeviationInPercent <= maxWindSpeedDeviationInPercent;
                        result = result.mergeBySum(new WindEstimatorEvaluationResult(windCourseDeviationInDegrees,
                                windCourseDeviationWithinTolerance, windSpeedDeviationInPercent,
                                windSpeedDeviationWithinTolerance, confidence));
                    } else {
                        result = result.mergeBySum(new WindEstimatorEvaluationResult(windCourseDeviationInDegrees,
                                windCourseDeviationWithinTolerance, confidence));
                    }
                }
            }
            LoggingUtil.logInfo("Evaluating on " + raceWithEstimationData.getRegattaName() + " Race "
                    + raceWithEstimationData.getRaceName() + " succeeded");
            result.printEvaluationStatistics(false);
            mergedResult = mergedResult
                    .mergeBySum(result.getAvgAsSingleResult(minAccuracyPerRaceForCorrectEstimation - 0.00001));
        }
        return mergedResult;
    }

}
