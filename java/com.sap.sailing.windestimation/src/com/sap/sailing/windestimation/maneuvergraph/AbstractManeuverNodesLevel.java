package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

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
                            .ordinal()] = 0;
                } else {
                    transitionProbabilitiesFromPreviousToThisNodesLevel[previousNodeToSet.ordinal()][thisLevelNode
                            .ordinal()] = transitionProbability;
                }
            }
        }
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
    public Bearing getCourse() {
        return getManeuver().getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getBearing();
    }

    @Override
    public double getWindCourseInDegrees(FineGrainedPointOfSail node) {
        double windCourse = (getCourse().getDegrees() - node.getTwa() + 180) % 360;
        if (windCourse < 0) {
            windCourse += 360;
        }
        return windCourse;
    }

    @Override
    public double getWindCourseInDegrees(double twa) {
        double windCourse = (getCourse().getDegrees() - twa + 180) % 360;
        if (windCourse < 0) {
            windCourse += 360;
        }
        return windCourse;
    }

}
