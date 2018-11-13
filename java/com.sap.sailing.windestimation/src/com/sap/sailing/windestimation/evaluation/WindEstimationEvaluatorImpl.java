package com.sap.sailing.windestimation.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.WindEstimationComponentWithInternals;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.preprocessing.DummyRacePreprocessingPipeline;
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
    private final Integer minManeuversPerRace;
    private final boolean randomCompetitorTrackClipping;
    private final Integer fixedNumberOfManeuvers;

    public WindEstimationEvaluatorImpl(double maxWindCourseDeviationInDegrees, double maxWindSpeedDeviationInPercent,
            double minAccuracyPerRaceForCorrectEstimation, boolean evaluationPerCompetitorTrack,
            int minManeuversPerRace, boolean randomCompetitorTrackClipping, Integer fixedNumberOfManeuvers) {
        this.maxWindCourseDeviationInDegrees = maxWindCourseDeviationInDegrees;
        this.maxWindSpeedDeviationInPercent = maxWindSpeedDeviationInPercent;
        this.minAccuracyPerRaceForCorrectEstimation = minAccuracyPerRaceForCorrectEstimation;
        this.evaluationPerCompetitorTrack = evaluationPerCompetitorTrack;
        this.minManeuversPerRace = minManeuversPerRace;
        this.randomCompetitorTrackClipping = randomCompetitorTrackClipping;
        this.fixedNumberOfManeuvers = fixedNumberOfManeuvers;
    }

    @Override
    public WindEstimatorEvaluationResult evaluateWindEstimator(
            WindEstimatorFactory<RaceWithEstimationData<T>> windEstimatorFactory,
            TargetWindFixesExtractor<T> targetWindFixesExtractor, Iterator<RaceWithEstimationData<T>> racesIterator,
            long numberOfRaces) {
        Stream<RaceWithEstimationData<T>> stream = StreamSupport.stream(
                new FixedBatchSpliteratorWrapper<>(
                        Spliterators.spliterator(racesIterator, numberOfRaces, Spliterator.NONNULL), numberOfRaces, 50),
                true);
        if (evaluationPerCompetitorTrack) {
            stream = stream.flatMap(race -> {
                List<RaceWithEstimationData<T>> racesToEvaluate = new ArrayList<>();
                if (race.getCompetitorTracks().size() <= 1) {
                    racesToEvaluate.add(race);
                } else {
                    for (CompetitorTrackWithEstimationData<T> competitorTrack : race.getCompetitorTracks()) {
                        RaceWithEstimationData<T> raceWithSingleCompetitorTrack = new RaceWithEstimationData<>(
                                race.getRegattaName(), race.getRaceName(), Collections.singletonList(competitorTrack));
                        racesToEvaluate.add(raceWithSingleCompetitorTrack);
                    }
                }
                return racesToEvaluate.stream();
            });
        }
        Stream<EvaluationCase<T>> preprocessingStream = stream.map(race -> {
            WindEstimationComponentWithInternals<RaceWithEstimationData<T>> windEstimator = (WindEstimationComponentWithInternals<RaceWithEstimationData<T>>) windEstimatorFactory
                    .createNewEstimatorInstance();
            RaceWithEstimationData<ManeuverForEstimation> preprocessedRace = windEstimator.getPreprocessingPipeline()
                    .preprocessRace(race);
            Map<TimePoint, Wind> targetWindPerTimePoint = new HashMap<>();
            for (CompetitorTrackWithEstimationData<T> competitorTrackWithEstimationData : race.getCompetitorTracks()) {
                List<Wind> targetWindFixes = targetWindFixesExtractor
                        .extractTargetWindFixes(competitorTrackWithEstimationData);
                for (Wind wind : targetWindFixes) {
                    targetWindPerTimePoint.put(wind.getTimePoint(), wind);
                }
            }
            return new EvaluationCase<>(windEstimator, preprocessedRace, targetWindPerTimePoint);
        });
        preprocessingStream = preprocessingStream.filter(evaluationCase -> evaluationCase.getRace()
                .getCompetitorTracks().stream().mapToInt(competitorTrack -> competitorTrack.getElements().size())
                .sum() >= minManeuversPerRace);
        if (randomCompetitorTrackClipping) {
            RaceWithRandomClippingPreprocessingPipelineImpl<ManeuverForEstimation, ManeuverForEstimation> clippingPipeline = new RaceWithRandomClippingPreprocessingPipelineImpl<>(
                    new DummyRacePreprocessingPipeline<>(), fixedNumberOfManeuvers == null ? 1 : fixedNumberOfManeuvers,
                    fixedNumberOfManeuvers == null ? Integer.MAX_VALUE : fixedNumberOfManeuvers);
            preprocessingStream = preprocessingStream
                    .map(evaluationCase -> new EvaluationCase<>(evaluationCase.getWindEstimator(),
                            clippingPipeline.preprocessRace(evaluationCase.getRace()),
                            evaluationCase.getTargetWindFixesPerTimePoint()));
        } else if (fixedNumberOfManeuvers != null) {
            throw new IllegalArgumentException(
                    "fixedNumberOfManeuver requires randomClippingOfCompetitorTracks to be true");
        }
        return preprocessingStream.map(evaluationCase -> evaluateWindEstimator(evaluationCase))
                .reduce((one, two) -> one.mergeBySum(two)).orElse(new WindEstimatorEvaluationResult());
    }

    @Override
    public WindEstimatorEvaluationResult evaluateWindEstimator(EvaluationCase<T> evaluationCase) {
        RaceWithEstimationData<ManeuverForEstimation> raceWithEstimationData = evaluationCase.getRace();
        LoggingUtil.logInfo("Evaluating on " + raceWithEstimationData.getRegattaName() + " Race "
                + raceWithEstimationData.getRaceName());
        Map<TimePoint, Wind> targetWindPerTimePoint = evaluationCase.getTargetWindFixesPerTimePoint();
        WindEstimationComponentWithInternals<RaceWithEstimationData<T>> windEstimator = evaluationCase
                .getWindEstimator();
        List<WindWithConfidence<Void>> windTrack = windEstimator
                .estimateWindTrackAfterPreprocessing(raceWithEstimationData);
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
        return result.getAvgAsSingleResult(minAccuracyPerRaceForCorrectEstimation - 0.00001);
    }

}
