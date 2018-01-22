package com.sap.sailing.windestimation.impl.graph;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.Maneuver;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph {

    private final PolarDataService polarService;
    private final BoatClass boatClass;
    private final IManeuverSpeedRetriever maneuverSpeedRetriever;
    private final SingleManeuverClassifier singleManeuverClassifier;

    private final Map<GraphNode, Double> distance = new HashMap<>();
    private final Map<GraphNode, GraphNode> predecessor = new HashMap<>();

    private GraphLevel firstGraphLevel = null;
    private GraphLevel lastGraphLevel = null;

    public ManeuverSequenceGraph(BoatClass boatClass, PolarDataService polarService,
            IManeuverSpeedRetriever maneuverSpeedRetriever, Iterable<Maneuver> maneuverSequence) {
        this.boatClass = boatClass;
        this.polarService = polarService;
        this.maneuverSpeedRetriever = maneuverSpeedRetriever;
        singleManeuverClassifier = new SingleManeuverClassifier(boatClass, polarService, maneuverSpeedRetriever);
        for (Maneuver maneuver : maneuverSequence) {
            // if (checkManeuverEligibleForGraphLevel(maneuver)) {
            appendManeuverAsGraphLevel(maneuver);
            // }
        }
    }

    // /**
    // * Checks whether the maneuver is a head up or a bear away with spurious
    // * {@link Maneuver#getManeuverCurveWithStableSpeedAndCourseBoundaries()} due to collision with another important
    // * maneuver like tack/jibe/penalty circle. Mark passing maneuvers are not considered as eligible.
    // */
    // private boolean checkManeuverEligibleForGraphLevel(Maneuver maneuver) {
    // double directionChangeInDegreesWithinManeuverBoundaries = maneuver
    // .getManeuverCurveWithStableSpeedAndCourseBoundaries().getDirectionChangeInDegrees();
    // double directionChangeInDegreesWithinMainCurve = maneuver.getMainCurveBoundaries()
    // .getDirectionChangeInDegrees();
    // if (directionChangeInDegreesWithinMainCurve * directionChangeInDegreesWithinManeuverBoundaries < 0
    // && Math.abs(directionChangeInDegreesWithinMainCurve) < 20) {
    // return false;
    // }
    // if(Math.abs(directionChangeInDegreesWithinMainCurve) < 15 && Math.abs(directionChangeInDegreesWithinMainCurve) *
    // 2 < Math.abs(directionChangeInDegreesWithinManeuverBoundaries)) {
    // return false;
    // }
    // return true;
    // }

    private void appendManeuverAsGraphLevel(Maneuver maneuver) {
        lastGraphLevel = new GraphLevel(maneuver, singleManeuverClassifier, lastGraphLevel);
        if (firstGraphLevel == null) {
            firstGraphLevel = lastGraphLevel;
        }
    }

    public void computePossiblePathsWithDistances() {
        GraphLevel currentLevel = firstGraphLevel;
        while (currentLevel != null) {
            GraphLevel previousLevel = currentLevel.getPreviousLevel();
            GraphLevel nextLevel = currentLevel.getNextLevel();
            boolean markPassingIsNeighbour = false;
            if (previousLevel != null && previousLevel.getManeuver().getType() == ManeuverType.MARK_PASSING
                    || nextLevel != null && nextLevel.getManeuver().getType() == ManeuverType.MARK_PASSING) {
                markPassingIsNeighbour = true;
            }
            boolean collisionWithOtherManeuver = false;
            if (previousLevel != null
                    && previousLevel.getManeuver().getManeuverCurveWithStableSpeedAndCourseBoundaries()
                            .getTimePointAfter()
                            .until(currentLevel.getManeuver().getManeuverCurveWithStableSpeedAndCourseBoundaries()
                                    .getTimePointBefore())
                            .asSeconds() < 2
                    || nextLevel != null
                            && currentLevel.getManeuver().getManeuverCurveWithStableSpeedAndCourseBoundaries()
                                    .getTimePointAfter().until(nextLevel.getManeuver()
                                            .getManeuverCurveWithStableSpeedAndCourseBoundaries().getTimePointBefore())
                                    .asSeconds() < 2) {
                collisionWithOtherManeuver = true;
            }

            for (GraphNode graphNode : currentLevel.getNodes()) {
                PointOfSail pointOfSailBeforeManeuver = graphNode.getPointOfSailBeforeManeuver();

            }

            currentLevel = currentLevel.getNextLevel();
        }
    }

}
