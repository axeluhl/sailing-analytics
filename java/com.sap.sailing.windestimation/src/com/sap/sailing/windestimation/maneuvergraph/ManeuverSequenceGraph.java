package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph<T extends ManeuverNodesLevel<T>, R> {

    private T firstGraphLevel = null;
    private T lastGraphLevel = null;
    private final ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory;

    public ManeuverSequenceGraph(Iterable<R> maneuverSequence,
            ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory) {
        this.maneuverNodesLevelFactory = maneuverNodesLevelFactory;
        for (R maneuver : maneuverSequence) {
            appendManeuverAsGraphLevel(maneuver);
        }
    }

    public ManeuverSequenceGraph(ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory) {
        this.maneuverNodesLevelFactory = maneuverNodesLevelFactory;
    }

    protected void appendManeuverAsGraphLevel(R nodeLevelReference) {
        T newManeuverNodesLevel = maneuverNodesLevelFactory.createNewManeuverNodesLevel(nodeLevelReference);
        if (firstGraphLevel == null) {
            // TODO introduce first node as dummy with nodes representing course before the provided maneuver, or
            // introduce this logic in ManeuverNodesLevel
            firstGraphLevel = newManeuverNodesLevel;
            lastGraphLevel = newManeuverNodesLevel;

        } else {
            lastGraphLevel.appendNextManeuverNodesLevel(newManeuverNodesLevel);
            lastGraphLevel = newManeuverNodesLevel;
        }
        newManeuverNodesLevel.computeProbabilitiesFromPreviousLevelToThisLevel();
        newManeuverNodesLevel.computeBestPathsToThisLevel();
    }

    public void recomputePossiblePathsWithDistances() {
        T currentLevel = firstGraphLevel;
        while (currentLevel != null) {
            currentLevel.computeProbabilitiesFromPreviousLevelToThisLevel();
            currentLevel.computeBestPathsToThisLevel();
            currentLevel = currentLevel.getNextLevel();
        }
    }

    public List<WindWithConfidence<TimePoint>> estimateWindTrack() {
        List<WindWithConfidence<TimePoint>> windTrack = Collections.emptyList();
        T lastGraphLevel = this.lastGraphLevel;
        if (lastGraphLevel != null) {
            double[] probabilitiesOfBestPathToCoarseGrainedPointOfSail = new double[CoarseGrainedPointOfSail
                    .values().length];
            double maxProbability = 0;
            FineGrainedPointOfSail pointOfSailWithMaxProbability = null;
            for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
                double probability = lastGraphLevel.getProbabilityOfBestPathToNodeFromStart(pointOfSail);
                if (probability > probabilitiesOfBestPathToCoarseGrainedPointOfSail[pointOfSail
                        .getCoarseGrainedPointOfSail().ordinal()]) {
                    probabilitiesOfBestPathToCoarseGrainedPointOfSail[pointOfSail.getCoarseGrainedPointOfSail()
                            .ordinal()] = probability;
                    if (maxProbability < probability) {
                        maxProbability = probability;
                        pointOfSailWithMaxProbability = pointOfSail;
                    }
                }
            }
            double sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail = 0;
            for (CoarseGrainedPointOfSail coarseGrainedPointOfSail : CoarseGrainedPointOfSail.values()) {
                sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail = probabilitiesOfBestPathToCoarseGrainedPointOfSail[coarseGrainedPointOfSail
                        .ordinal()];
            }
            double bestPathConfidence = maxProbability / sumOfprobabilitiesOfBestPathToCoarseGrainedPointOfSail;
            windTrack = getWindTrackWithLastNodeOfLastLevelConsideringMiddleCoursesOfCleanTacksAndJibes(
                    pointOfSailWithMaxProbability, lastGraphLevel, bestPathConfidence);
            if (windTrack.isEmpty()) {
                windTrack = getWindTrackWithLastNodeOfLastLevelConsideringStableCourses(pointOfSailWithMaxProbability,
                        lastGraphLevel, bestPathConfidence * 0.5);
            }
        }
        return windTrack;
    }

    private List<WindWithConfidence<TimePoint>> getWindTrackWithLastNodeOfLastLevelConsideringMiddleCoursesOfCleanTacksAndJibes(
            FineGrainedPointOfSail lastNode, T lastLevel, double baseConfidence) {
        FineGrainedPointOfSail currentNode = lastNode;
        T currentLevel = lastLevel;
        List<WindWithConfidence<TimePoint>> windTrack = new ArrayList<>();
        while (currentLevel.getPreviousLevel() != null) {
            FineGrainedPointOfSail previousNode = currentLevel.getBestPreviousNode(currentNode);
            T previousLevel = currentLevel.getPreviousLevel();
            if (previousNode.getCoarseGrainedPointOfSail().getLegType() == currentNode.getCoarseGrainedPointOfSail()
                    .getLegType() && previousNode.getTack() != currentNode.getTack()
                    && Math.abs(previousLevel.getManeuver().getMainCurve().getDirectionChangeInDegrees()) < 110) {
                // TODO consider only clean maneuvers without mark passing and bad GPS-sampling rate at maneuver
                // boundaries
                if (currentNode.getCoarseGrainedPointOfSail().getLegType() == LegType.UPWIND) {
                    SpeedWithBearingWithConfidence<Void> speedWithTwaIfTack = getSpeedWithTwaIfTack(currentLevel);
                    SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(
                            speedWithTwaIfTack.getObject().getKnots(), currentLevel.getManeuver()
                                    .getCurveWithUnstableCourseAndSpeed().getMiddleCourse().reverse());
                    WindImpl wind = new WindImpl(currentLevel.getManeuver().getPosition(),
                            currentLevel.getManeuver().getTimePoint(), windSpeedWithBearing);
                    WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind,
                            speedWithTwaIfTack.getConfidence() * baseConfidence,
                            currentLevel.getManeuver().getTimePoint(), true);
                    windTrack.add(windWithConfidence);
                } else if (currentNode.getCoarseGrainedPointOfSail().getLegType() == LegType.DOWNWIND) {
                    SpeedWithBearingWithConfidence<Void> speedWithTwaIfJibe = getSpeedWithTwaIfJibe(currentLevel);
                    SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(
                            speedWithTwaIfJibe.getObject().getKnots(),
                            currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed().getMiddleCourse());
                    WindImpl wind = new WindImpl(currentLevel.getManeuver().getPosition(),
                            currentLevel.getManeuver().getTimePoint(), windSpeedWithBearing);
                    WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind,
                            speedWithTwaIfJibe.getConfidence() * baseConfidence,
                            currentLevel.getManeuver().getTimePoint(), true);
                    windTrack.add(windWithConfidence);
                }
            }
            currentLevel = previousLevel;
            currentNode = previousNode;
        }
        return windTrack;
    }

    private List<WindWithConfidence<TimePoint>> getWindTrackWithLastNodeOfLastLevelConsideringStableCourses(
            FineGrainedPointOfSail lastNode, T lastLevel, double baseConfidence) {
        // TODO Auto-generated method stub
        return null;
    }

    private SpeedWithBearingWithConfidence<Void> getSpeedWithTwaIfJibe(T currentLevel) {
        // TODO Auto-generated method stub
        return null;
    }

    private SpeedWithBearingWithConfidence<Void> getSpeedWithTwaIfTack(T currentLevel) {
        // TODO Auto-generated method stub
        return null;
    }

    public T getFirstGraphLevel() {
        return firstGraphLevel;
    }

    public T getLastGraphLevel() {
        return lastGraphLevel;
    }

}
