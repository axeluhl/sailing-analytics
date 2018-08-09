package com.sap.sailing.windestimation.maneuvergraph.impl;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevel;
import com.sap.sse.common.Bearing;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractManeuverNodesLevel<SelfType extends AbstractManeuverNodesLevel<SelfType>>
        implements ManeuverNodesLevel<SelfType> {

    private final CompleteManeuverCurveWithEstimationData maneuver;

    private SelfType previousLevel = null;
    private SelfType nextLevel = null;

    private double[][] transitionProbabilitiesFromPreviousToThisNodesLevel = new double[FineGrainedPointOfSail
            .values().length][FineGrainedPointOfSail.values().length];

    public AbstractManeuverNodesLevel(CompleteManeuverCurveWithEstimationData maneuver) {
        this.maneuver = maneuver;
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getManeuver() {
        return maneuver;
    }

    @Override
    public double getProbabilityFromPreviousLevelNodeToThisLevelNode(FineGrainedPointOfSail previousLevelNode,
            FineGrainedPointOfSail thisLevelNode) {
        return transitionProbabilitiesFromPreviousToThisNodesLevel[previousLevelNode.ordinal()][thisLevelNode
                .ordinal()];
    }

    protected void setProbabilityFromPreviousLevelNodeToThisLevelNode(FineGrainedPointOfSail previousLevelNode,
            FineGrainedPointOfSail thisLevelNode, double transitionProbability) {
        if (previousLevelNode != null) {
            transitionProbabilitiesFromPreviousToThisNodesLevel[previousLevelNode.ordinal()][thisLevelNode
                    .ordinal()] = transitionProbability;
        } else {
            final FineGrainedPointOfSail pointOfSailBeforeManeuver = thisLevelNode.getNextPointOfSail(
                    getManeuver().getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees() * -1);
            for (FineGrainedPointOfSail previousNodeToSet : FineGrainedPointOfSail.values()) {
                if (previousNodeToSet != pointOfSailBeforeManeuver) {
                    transitionProbabilitiesFromPreviousToThisNodesLevel[previousNodeToSet.ordinal()][thisLevelNode
                            .ordinal()] = 0.05;
                } else {
                    transitionProbabilitiesFromPreviousToThisNodesLevel[previousNodeToSet.ordinal()][thisLevelNode
                            .ordinal()] = transitionProbability;
                }
            }
        }
    }

    protected double getNodeTransitionPenaltyFactor(FineGrainedPointOfSail previousNode,
            FineGrainedPointOfSail currentNode) {
        double courseDifference = getPreviousLevel().getCourseAfter().getDifferenceTo(getCourseAfter()).getDegrees();
        FineGrainedPointOfSail targetPointOfSail = previousNode.getNextPointOfSail(courseDifference);
        if (targetPointOfSail == currentNode) {
            return 1;
        }
        double windCourseDeviation = targetPointOfSail.getDifferenceInDegrees(currentNode);
        if (targetPointOfSail.getCoarseGrainedPointOfSail() == currentNode.getCoarseGrainedPointOfSail()) {
            return 1 / (1 + Math.abs(windCourseDeviation / 45));
        }
        if (targetPointOfSail.getTack() == currentNode.getTack() && (targetPointOfSail.getLegType() == LegType.REACHING
                || currentNode.getLegType() == LegType.REACHING)) {
            return 1 / (1 + Math.abs(windCourseDeviation / 30));
        }
        if (targetPointOfSail.getNextPointOfSail(NauticalSide.STARBOARD) == currentNode
                || targetPointOfSail.getNextPointOfSail(NauticalSide.PORT) == currentNode) {
            return 1 / (1 + Math.abs((windCourseDeviation) / 15));
        }
        return 1 / (1 + Math.pow((windCourseDeviation) / 15, 2));
    }

    protected void normalizeNodeTransitions() {
        for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
            ProbabilityUtil.normalizeLikelihoodArray(
                    transitionProbabilitiesFromPreviousToThisNodesLevel[previousNode.ordinal()]);
        }
    }

    protected void setNextLevel(SelfType nextLevel) {
        this.nextLevel = nextLevel;
    }

    @Override
    public SelfType getNextLevel() {
        return nextLevel;
    }

    protected void setPreviousLevel(SelfType previousLevel) {
        this.previousLevel = previousLevel;
    }

    @Override
    public SelfType getPreviousLevel() {
        return previousLevel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void appendNextManeuverNodesLevel(SelfType nextManeuverNodesLevel) {
        setNextLevel(nextManeuverNodesLevel);
        nextManeuverNodesLevel.setPreviousLevel((SelfType) this);
    }

    @Override
    public Bearing getCourseAfter() {
        return getManeuver().getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getBearing();
    }

    @Override
    public Bearing getCourseBefore() {
        return getManeuver().getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getBearing();
    }

    @Override
    public double getWindCourseInDegrees(FineGrainedPointOfSail node) {
        double windCourse = (getCourseAfter().getDegrees() - node.getTwa() + 180) % 360;
        if (windCourse < 0) {
            windCourse += 360;
        }
        return windCourse;
    }

    @Override
    public double getWindCourseInDegrees(double twa) {
        double windCourse = (getCourseAfter().getDegrees() - twa + 180) % 360;
        if (windCourse < 0) {
            windCourse += 360;
        }
        return windCourse;
    }

}
