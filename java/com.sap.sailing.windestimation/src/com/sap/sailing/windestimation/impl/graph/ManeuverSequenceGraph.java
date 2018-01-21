package com.sap.sailing.windestimation.impl.graph;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph {

    private final PolarDataService polarService;
    private final BoatClass boatClass;

    private final Map<GraphNode, Double> distance = new HashMap<>();
    private final Map<GraphNode, GraphNode> predecessor = new HashMap<>();

    private GraphLevel firstGraphLevel = null;
    private GraphLevel lastGraphLevel = null;
    private ILowestSpeedWithinManeuverMainCurveRetriever lowestSpeedWithinManeuverMainCurveRetriever;

    public ManeuverSequenceGraph(BoatClass boatClass, PolarDataService polarService,
            ILowestSpeedWithinManeuverMainCurveRetriever lowestSpeedWithinManeuverMainCurveRetriever,
            Iterable<Maneuver> maneuverSequence) {
        this.boatClass = boatClass;
        this.polarService = polarService;
        this.lowestSpeedWithinManeuverMainCurveRetriever = lowestSpeedWithinManeuverMainCurveRetriever;
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
        SpeedWithBearing lowestSpeedWithinMainCurve = lowestSpeedWithinManeuverMainCurveRetriever
                .getLowestSpeedWithinManeuverMainCurve(maneuver);
        lastGraphLevel = new GraphLevel(maneuver, lowestSpeedWithinMainCurve, lastGraphLevel);
        if (firstGraphLevel == null) {
            firstGraphLevel = lastGraphLevel;
        }
    }

    public void computePossiblePathsWithDistances() {
        GraphLevel currentLevel = firstGraphLevel;
        while (currentLevel != null) {
            Maneuver maneuver = currentLevel.getManeuver();
            SpeedWithBearing speedWithBearingBefore = maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                    .getSpeedWithBearingBefore();
            SpeedWithBearing speedWithBearingAfter = maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                    .getSpeedWithBearingAfter();
            double courseChangeDeg = maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                    .getDirectionChangeInDegrees();
            GraphLevel previousLevel = currentLevel.getPreviousLevel();
            GraphLevel nextLevel = currentLevel.getNextLevel();
            boolean markPassingIsNeighbour = false;
            if (previousLevel != null && previousLevel.getManeuver().getType() == ManeuverType.MARK_PASSING
                    || nextLevel != null && nextLevel.getManeuver().getType() == ManeuverType.MARK_PASSING) {
                markPassingIsNeighbour = true;
            }
            if (Math.abs(courseChangeDeg) > 120) {
                // not ordinary maneuver
                double ratio = currentLevel.getLowestSpeedWithinMainCurve().getKnots()
                        / maneuver.getMainCurveBoundaries().getSpeedWithBearingBefore().getKnots();
                if (courseChangeDeg <= 180 && ratio > 0.93) {
                    // no jibe or tack within maneuver
                } else if (courseChangeDeg < 350) {
                    // lowest speed can either within jibing or tacking
                } else {
                    // the maneuver is a penalty circle
                    // => course at lowest speed refers upwind
                }
            } else {
                Pair<Double, SpeedWithBearingWithConfidence<Void>> tackLikelihookWithTwaTws = polarService
                        .getManeuverLikelihoodAndTwsTwa(boatClass, speedWithBearingBefore, courseChangeDeg,
                                ManeuverType.TACK);
                Pair<Double, SpeedWithBearingWithConfidence<Void>> jibeLikelihookWithTwaTws = polarService
                        .getManeuverLikelihoodAndTwsTwa(boatClass, speedWithBearingBefore, courseChangeDeg,
                                ManeuverType.JIBE);
            }
            
            for (GraphNode graphNode : currentLevel.getNodes()) {
                PointOfSail pointOfSailBeforeManeuver = graphNode.getPointOfSailBeforeManeuver();

            }

            currentLevel = currentLevel.getNextLevel();
        }
    }

}
