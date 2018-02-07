package com.sap.sailing.windestimation.impl.maneuvergraph;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sailing.windestimation.impl.IManeuverSpeedRetriever;
import com.sap.sailing.windestimation.impl.WindTrackCandidate;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph {

    private final SingleManeuverClassifier singleManeuverClassifier;

    private ManeuverNodesLevel firstGraphLevel = null;
    private ManeuverNodesLevel lastGraphLevel = null;

    public ManeuverSequenceGraph(BoatClass boatClass, PolarDataService polarService,
            IManeuverSpeedRetriever maneuverSpeedRetriever, Iterable<Maneuver> maneuverSequence) {
        singleManeuverClassifier = new SingleManeuverClassifier(boatClass, polarService, maneuverSpeedRetriever);
        for (Maneuver maneuver : maneuverSequence) {
            appendManeuverAsGraphLevel(maneuver);
        }
    }

    private void appendManeuverAsGraphLevel(Maneuver maneuver) {
        lastGraphLevel = new ManeuverNodesLevel(maneuver, singleManeuverClassifier, lastGraphLevel);
        if (firstGraphLevel == null) {
            firstGraphLevel = lastGraphLevel;
        }
    }

    public void computePossiblePathsWithDistances() {
        ManeuverNodesLevel currentLevel = firstGraphLevel;
        while (currentLevel != null) {
            currentLevel.computeDistances();
            currentLevel = currentLevel.getNextLevel();
        }
    }

    public Iterable<WindTrackCandidate> computeWindDirectionCandidates() {
        List<WindTrackCandidate> possibleWindTracks = new ArrayList<>();
        if (this.lastGraphLevel != null) {
            ManeuverNodesLevel lastGraphLevel = new ManeuverNodesLevel(null, null, null);
            lastGraphLevel.setPreviousLevel(this.lastGraphLevel);
            lastGraphLevel.computeDistances();
            double bestDistance = Double.MAX_VALUE;
            for (int i = 0; i < lastGraphLevel.getBestDistancesFromStart().length; i++) {
                double distance = lastGraphLevel.getBestDistancesFromStart()[i];
                if (distance < bestDistance) {
                    bestDistance = distance;
                }
            }
            for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
                double distance = lastGraphLevel.getDistanceToNodeFromStart(pointOfSail);
                double confidence = 1 - ((bestDistance - distance) / bestDistance);
                Iterable<WindWithConfidence<TimePoint>> windTrack = getWindTrackWithLastNode(pointOfSail);
                WindTrackCandidate windTrackCandidate = new WindTrackCandidate(confidence, windTrack);
                possibleWindTracks.add(windTrackCandidate);
            }
        }
        return possibleWindTracks;
    }

    private Iterable<WindWithConfidence<TimePoint>> getWindTrackWithLastNode(FineGrainedPointOfSail lastNode) {
        FineGrainedPointOfSail currentNode = lastNode;
        ManeuverNodesLevel currentLevel = lastGraphLevel;
        List<WindWithConfidence<TimePoint>> windTrack = new ArrayList<>();
        while (currentNode != null) {
            FineGrainedPointOfSail previousNode = currentLevel.getBestPrecedingNode(currentNode);
            ManeuverNodesLevel previousLevel = currentLevel.getPreviousLevel();
            if (previousNode.getCoarseGrainedPointOfSail().getLegType() == currentNode.getCoarseGrainedPointOfSail()
                    .getLegType() && previousNode.getTack() != currentNode.getTack()
                    && Math.abs(previousLevel.getManeuver().getDirectionChangeInDegrees()) < 110) {
                if (previousNode.getCoarseGrainedPointOfSail().getLegType() == LegType.UPWIND) {
                    SpeedWithBearingWithConfidence<Void> speedWithTwaIfTack = previousLevel
                            .getManeuverClassificationResult().getSpeedWithTwaIfTack();
                    SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(
                            speedWithTwaIfTack.getObject().getKnots(),
                            previousLevel.getManeuverClassificationResult().getMiddleManeuverCourse().reverse());
                    WindImpl wind = new WindImpl(previousLevel.getManeuver().getPosition(),
                            previousLevel.getManeuver().getTimePoint(), windSpeedWithBearing);
                    WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind,
                            speedWithTwaIfTack.getConfidence(), previousLevel.getManeuver().getTimePoint(), true);
                    windTrack.add(windWithConfidence);
                } else if (previousNode.getCoarseGrainedPointOfSail().getLegType() == LegType.DOWNWIND) {
                    SpeedWithBearingWithConfidence<Void> speedWithTwaIfJibe = previousLevel
                            .getManeuverClassificationResult().getSpeedWithTwaIfJibe();
                    SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(
                            speedWithTwaIfJibe.getObject().getKnots(),
                            previousLevel.getManeuverClassificationResult().getMiddleManeuverCourse());
                    WindImpl wind = new WindImpl(previousLevel.getManeuver().getPosition(),
                            previousLevel.getManeuver().getTimePoint(), windSpeedWithBearing);
                    WindWithConfidenceImpl<TimePoint> windWithConfidence = new WindWithConfidenceImpl<TimePoint>(wind,
                            speedWithTwaIfJibe.getConfidence(), previousLevel.getManeuver().getTimePoint(), true);
                    windTrack.add(windWithConfidence);
                }
            }
            currentLevel = previousLevel;
            currentNode = previousNode;
        }
        return windTrack;
    }

}
