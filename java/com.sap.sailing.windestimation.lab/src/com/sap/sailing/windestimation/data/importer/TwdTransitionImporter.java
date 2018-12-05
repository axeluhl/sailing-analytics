package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevel;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRangeBasedTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverClassifier;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.LabelledManeuverForEstimation;
import com.sap.sailing.windestimation.data.LabelledTwdTransition;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.maneuver.RaceWithCompleteManeuverCurvePersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.TwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.preprocessing.RaceElementsFilteringPreprocessingPipelineImpl;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class TwdTransitionImporter {

    public static void main(String[] args) throws UnknownHostException {
        LoggingUtil.logInfo("###################\r\nTWD transitions Import started");
        RaceWithCompleteManeuverCurvePersistenceManager racesPersistenceManager = new RaceWithCompleteManeuverCurvePersistenceManager();
        TwdTransitionPersistenceManager twdTransitionPersistenceManager = new TwdTransitionPersistenceManager();
        twdTransitionPersistenceManager.dropCollection();
        ManeuverClassifier maneuverClassifier = new DummyManeuverClassifier();
        RaceElementsFilteringPreprocessingPipelineImpl preprocessingPipeline = new RaceElementsFilteringPreprocessingPipelineImpl();
        long twdTransitionsCount = 0;
        for (PersistedElementsIterator<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> iterator = racesPersistenceManager
                .getIterator(null); iterator.hasNext();) {
            RaceWithEstimationData<CompleteManeuverCurveWithEstimationData> race = iterator.next();
            LoggingUtil.logInfo("Processing race " + race.getRaceName() + " of regatta " + race.getRegattaName());
            List<ManeuverWithProbabilisticTypeClassification> sortedManeuvers = getPreprocessedSortedManeuvers(
                    maneuverClassifier, preprocessingPipeline, race);
            if (sortedManeuvers.size() > 1) {
                List<TwdTransition> twdTransitions = getTwdTransitions(sortedManeuvers, race.getRegattaName());
                int numberOfTwdTransitions = twdTransitions.size();
                twdTransitionPersistenceManager.add(twdTransitions);
                twdTransitionsCount += numberOfTwdTransitions;
                LoggingUtil.logInfo(numberOfTwdTransitions + " TWD transitions imported");
            } else {
                LoggingUtil.logInfo("No TWD transitions to import");
            }
        }
        LoggingUtil.logInfo("###################\r\nTWD transitions Import finished");
        LoggingUtil.logInfo("Totally " + twdTransitionsCount + " TWD transitions imported");
    }

    private static List<ManeuverWithProbabilisticTypeClassification> getPreprocessedSortedManeuvers(
            ManeuverClassifier maneuverClassifier, RaceElementsFilteringPreprocessingPipelineImpl preprocessingPipeline,
            RaceWithEstimationData<CompleteManeuverCurveWithEstimationData> race) {
        RaceWithEstimationData<ManeuverForEstimation> preprocessedRace = preprocessingPipeline.preprocessRace(race);
        List<CompetitorTrackWithEstimationData<ManeuverWithProbabilisticTypeClassification>> competitorTracks = preprocessedRace
                .getCompetitorTracks().stream().map(competitorTrack -> {
                    List<ManeuverWithProbabilisticTypeClassification> maneuverClassifications = competitorTrack
                            .getElements().stream().map(maneuver -> maneuverClassifier.classifyManeuver(maneuver))
                            .collect(Collectors.toList());
                    return competitorTrack.constructWithElements(maneuverClassifications);
                }).collect(Collectors.toList());
        RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications = race
                .constructWithElements(competitorTracks);

        List<ManeuverWithProbabilisticTypeClassification> sortedManeuvers = raceWithManeuverClassifications
                .getCompetitorTracks().stream().flatMap(competitorTrack -> competitorTrack.getElements().stream())
                .sorted().collect(Collectors.toList());
        return sortedManeuvers;
    }

    private static List<TwdTransition> getTwdTransitions(
            List<ManeuverWithProbabilisticTypeClassification> sortedManeuvers, String regattaName) {
        IntersectedWindRangeBasedTransitionProbabilitiesCalculator intersectedWindRangeBasedTransitionProbabilitiesCalculator = new IntersectedWindRangeBasedTransitionProbabilitiesCalculator();
        SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator simpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator = new SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator();
        GraphLevel firstGraphLevel = null, lastGraphLevel = null;
        List<TwdTransition> result = new ArrayList<>(sortedManeuvers.size()
                * ManeuverTypeForClassification.values().length * ManeuverTypeForClassification.values().length);
        for (ManeuverWithProbabilisticTypeClassification maneuver : sortedManeuvers) {
            GraphLevel newManeuverNodesLevel = new GraphLevel(maneuver,
                    intersectedWindRangeBasedTransitionProbabilitiesCalculator);
            if (firstGraphLevel == null) {
                firstGraphLevel = newManeuverNodesLevel;
                lastGraphLevel = newManeuverNodesLevel;
            } else {
                lastGraphLevel.appendNextManeuverNodesLevel(newManeuverNodesLevel);
                Duration duration = lastGraphLevel.getManeuver().getManeuverTimePoint()
                        .until(newManeuverNodesLevel.getManeuver().getManeuverTimePoint());
                Distance distance = lastGraphLevel.getManeuver().getManeuverPosition()
                        .getDistance(newManeuverNodesLevel.getManeuver().getManeuverPosition());
                for (GraphNode previousNode : lastGraphLevel.getLevelNodes()) {
                    WindCourseRange previousWindCourseRange = simpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator
                            .getWindCourseRangeForManeuverType(lastGraphLevel.getManeuver(),
                                    previousNode.getManeuverType());
                    for (GraphNode currentNode : newManeuverNodesLevel.getLevelNodes()) {
                        WindCourseRange currentWindCourseRange = simpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator
                                .getWindCourseRangeForManeuverType(newManeuverNodesLevel.getManeuver(),
                                        currentNode.getManeuverType());
                        double twdChangeDegrees = previousWindCourseRange.intersect(currentWindCourseRange)
                                .getViolationRange();
                        double intersectedTwdChangeDegrees = intersectedWindRangeBasedTransitionProbabilitiesCalculator
                                .mergeWindRangeAndGetTransitionProbability(previousNode, lastGraphLevel, currentNode,
                                        newManeuverNodesLevel)
                                .getA().getViolationRange();
                        boolean correct = previousNode
                                .getManeuverType() == ((LabelledManeuverForEstimation) lastGraphLevel.getManeuver())
                                        .getManeuverType()
                                && currentNode
                                        .getManeuverType() == ((LabelledManeuverForEstimation) newManeuverNodesLevel
                                                .getManeuver()).getManeuverType();
                        LabelledTwdTransition labelledTwdTransition = new LabelledTwdTransition(distance, duration,
                                newManeuverNodesLevel.getManeuver().getBoatClass(),
                                new DegreeBearingImpl(twdChangeDegrees),
                                new DegreeBearingImpl(intersectedTwdChangeDegrees), correct,
                                previousNode.getManeuverType(), currentNode.getManeuverType(), regattaName);
                        result.add(labelledTwdTransition);
                    }
                }
                lastGraphLevel = newManeuverNodesLevel;
            }
        }
        return result;
    }

    private static class DummyManeuverClassifier implements ManeuverClassifier {

        @Override
        public ManeuverWithProbabilisticTypeClassification classifyManeuver(ManeuverForEstimation maneuver) {
            ManeuverWithProbabilisticTypeClassification maneuverWithProbabilisticTypeClassification = new ManeuverWithProbabilisticTypeClassification(
                    maneuver, new double[ManeuverTypeForClassification.values().length]);
            return maneuverWithProbabilisticTypeClassification;
        }

        @Override
        public double getTestScore() {
            return 0;
        }

        @Override
        public boolean hasSupportForProvidedFeatures() {
            return true;
        }

    }

}
