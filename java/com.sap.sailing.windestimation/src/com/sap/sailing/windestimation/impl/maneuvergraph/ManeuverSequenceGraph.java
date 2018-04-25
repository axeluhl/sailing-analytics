package com.sap.sailing.windestimation.impl.maneuvergraph;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;
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
        newManeuverNodesLevel.computeDistancesFromPreviousLevelToThisLevel();
        newManeuverNodesLevel.computeBestPathsToThisLevel();
    }

    public void recomputePossiblePathsWithDistances() {
        T currentLevel = firstGraphLevel;
        while (currentLevel != null) {
            currentLevel.computeDistancesFromPreviousLevelToThisLevel();
            currentLevel.computeBestPathsToThisLevel();
            currentLevel = currentLevel.getNextLevel();
        }
    }
    
    public List<WindWithConfidence<TimePoint>> estimateWindTrack() {
        List<WindWithConfidence<TimePoint>> windTrack = new ArrayList<>();
        //TODO
        return windTrack;
    }
    
    // public Iterable<WindTrackCandidate> computeWindDirectionCandidates() {
    // List<WindTrackCandidate> possibleWindTracks = new ArrayList<>();
    // T lastGraphLevel = this.lastGraphLevel;
    // if (lastGraphLevel != null) {
    // double bestDistance = Double.MAX_VALUE;
    // for (int i = 0; i < lastGraphLevel.getBestDistancesFromStart().length; i++) {
    // double distance = lastGraphLevel.getBestDistancesFromStart()[i];
    // if (distance < bestDistance) {
    // bestDistance = distance;
    // }
    // }
    // // TODO rethink confidence computation
    // for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
    // double distance = lastGraphLevel.getDistanceToNodeFromStart(pointOfSail);
    // double confidence = 1 - ((bestDistance - distance) / bestDistance);
    // Iterable<WindWithConfidence<TimePoint>> windTrack = getWindTrackWithLastNode(pointOfSail);
    // WindTrackCandidate windTrackCandidate = new WindTrackCandidate(confidence, windTrack);
    // possibleWindTracks.add(windTrackCandidate);
    // }
    // }
    // return possibleWindTracks;
    // }

    // private Iterable<WindWithConfidence<TimePoint>> getWindTrackWithLastNode(FineGrainedPointOfSail lastNode) {
    // FineGrainedPointOfSail currentNode = lastNode;
    // T currentLevel = lastGraphLevel;
    // List<WindWithConfidence<TimePoint>> windTrack = new ArrayList<>();
    // while (currentNode != null) {
    // FineGrainedPointOfSail previousNode = currentLevel.getBestPreviousNode(currentNode);
    // T previousLevel = currentLevel.getPreviousLevel();
    // // TODO avoid previousLevel == null
    // if (previousNode.getCoarseGrainedPointOfSail().getLegType() == currentNode.getCoarseGrainedPointOfSail()
    // .getLegType() && previousNode.getTack() != currentNode.getTack()
    // && Math.abs(previousLevel.getManeuver().getMainCurve().getDirectionChangeInDegrees()) < 110) {
    // if (currentNode.getCoarseGrainedPointOfSail().getLegType() == LegType.UPWIND) {
    // SpeedWithBearingWithConfidence<Void> speedWithTwaIfTack = currentNode
    // .getManeuverClassificationResult().getSpeedWithTwaIfTack();
    // SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(
    // speedWithTwaIfTack.getObject().getKnots(), currentLevel.getManeuver()
    // .getCurveWithUnstableCourseAndSpeed().getMiddleCourse().reverse());
    // WindImpl wind = new WindImpl(currentLevel.getManeuver().getPosition(),
    // currentLevel.getManeuver().getTimePoint(), windSpeedWithBearing);
    // WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind,
    // speedWithTwaIfTack.getConfidence(), currentLevel.getManeuver().getTimePoint(), true);
    // windTrack.add(windWithConfidence);
    // } else if (currentNode.getCoarseGrainedPointOfSail().getLegType() == LegType.DOWNWIND) {
    // SpeedWithBearingWithConfidence<Void> speedWithTwaIfJibe = currentLevel
    // .getManeuverClassificationResult().getSpeedWithTwaIfJibe();
    // SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(
    // speedWithTwaIfJibe.getObject().getKnots(),
    // currentLevel.getManeuver().getCurveWithUnstableCourseAndSpeed().getMiddleCourse());
    // WindImpl wind = new WindImpl(currentLevel.getManeuver().getPosition(),
    // currentLevel.getManeuver().getTimePoint(), windSpeedWithBearing);
    // WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind,
    // speedWithTwaIfJibe.getConfidence(), currentLevel.getManeuver().getTimePoint(), true);
    // windTrack.add(windWithConfidence);
    // }
    // }
    // currentLevel = previousLevel;
    // currentNode = previousNode;
    // }
    // return windTrack;
    // }
    
    public T getFirstGraphLevel() {
        return firstGraphLevel;
    }
    
    public T getLastGraphLevel() {
        return lastGraphLevel;
    }

}
