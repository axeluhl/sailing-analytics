package com.sap.sailing.windestimation.maneuvergraph.pointofsail;

import com.sap.sailing.domain.common.BearingChangeAnalyzer;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.windestimation.data.FineGrainedManeuverType;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverEstimationResult;
import com.sap.sailing.windestimation.maneuvergraph.ProbabilityUtil;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GraphLevel {

    private final ManeuverForEstimation maneuver;
    private ManeuverEstimationResult maneuverEstimationResult;

    private GraphLevel previousLevel = null;
    private GraphLevel nextLevel = null;

    protected boolean calculationOfTransitionProbabilitiesNeeded = true;

    private double[][] transitionProbabilitiesFromPreviousToThisNodesLevel = new double[FineGrainedPointOfSail
            .values().length][FineGrainedPointOfSail.values().length];

    public GraphLevel(ManeuverForEstimation maneuver, ManeuverEstimationResult maneuverEstimationResult) {
        this.maneuver = maneuver;
        this.maneuverEstimationResult = maneuverEstimationResult;
    }

    public ManeuverForEstimation getManeuver() {
        return maneuver;
    }

    public double getProbabilityFromPreviousLevelNodeToThisLevelNode(FineGrainedPointOfSail previousLevelNode,
            FineGrainedPointOfSail thisLevelNode) {
        double probability = transitionProbabilitiesFromPreviousToThisNodesLevel[previousLevelNode
                .ordinal()][thisLevelNode.ordinal()];
        return probability;
    }

    protected void setProbabilityFromPreviousLevelNodeToThisLevelNode(FineGrainedPointOfSail previousLevelNode,
            FineGrainedPointOfSail thisLevelNode, double transitionProbability) {
        if (previousLevelNode != null) {
            transitionProbabilitiesFromPreviousToThisNodesLevel[previousLevelNode.ordinal()][thisLevelNode
                    .ordinal()] = transitionProbability;
        } else {
            final FineGrainedPointOfSail pointOfSailBeforeManeuver = thisLevelNode
                    .getNextPointOfSail(maneuver.getCourseChangeInDegrees() * -1);
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

    protected void setNextLevel(GraphLevel nextLevel) {
        this.nextLevel = nextLevel;
    }

    public GraphLevel getNextLevel() {
        return nextLevel;
    }

    protected void setPreviousLevel(GraphLevel previousLevel) {
        this.previousLevel = previousLevel;
        this.calculationOfTransitionProbabilitiesNeeded = true;
    }

    public GraphLevel getPreviousLevel() {
        return previousLevel;
    }

    public void appendNextManeuverNodesLevel(GraphLevel nextManeuverNodesLevel) {
        setNextLevel(nextManeuverNodesLevel);
        nextManeuverNodesLevel.setPreviousLevel(this);
    }

    public Bearing getCourseAfter() {
        return maneuver.getSpeedWithBearingAfter().getBearing();
    }

    public Bearing getCourseBefore() {
        return maneuver.getSpeedWithBearingBefore().getBearing();
    }

    public double getWindCourseInDegrees(FineGrainedPointOfSail node) {
        return node.getWindCourse(getCourseAfter().getDegrees());
    }

    public double getWindCourseInDegrees(double twa) {
        double windCourse = (getCourseAfter().getDegrees() - twa + 180) % 360;
        if (windCourse < 0) {
            windCourse += 360;
        }
        return windCourse;
    }

    public FineGrainedPointOfSail getPointOfSailBeforeManeuver(FineGrainedPointOfSail pointOfSailAfterManeuver) {
        return pointOfSailAfterManeuver.getNextPointOfSail(maneuver.getCourseChangeInDegrees() * -1);
    }

    public FineGrainedManeuverType getTypeOfCleanManeuver(FineGrainedPointOfSail pointOfSailAfterManeuver) {
        Bearing windCourse = new DegreeBearingImpl(getWindCourseInDegrees(pointOfSailAfterManeuver));
        BearingChangeAnalyzer bearingChangeAnalyzer = BearingChangeAnalyzer.INSTANCE;
        double directionChangeInDegrees = maneuver.getCourseChangeInDegrees();
        int numberOfTacks = bearingChangeAnalyzer.didPass(getCourseBefore(), directionChangeInDegrees, getCourseAfter(),
                windCourse.reverse());
        int numberOfJibes = bearingChangeAnalyzer.didPass(getCourseBefore(), directionChangeInDegrees, getCourseAfter(),
                windCourse);
        if (numberOfTacks > 0 && numberOfJibes > 0) {
            return FineGrainedManeuverType._360;
        }
        if (numberOfTacks > 0 || numberOfJibes > 0) {
            if (Math.abs(directionChangeInDegrees) > 120) {
                return numberOfTacks > 1 ? FineGrainedManeuverType._180_TACK : FineGrainedManeuverType._180_JIBE;
            }
            return numberOfTacks > 0 ? FineGrainedManeuverType.TACK : FineGrainedManeuverType.JIBE;
        }
        boolean bearAway = pointOfSailAfterManeuver.getTack() == Tack.STARBOARD && directionChangeInDegrees > 0
                || pointOfSailAfterManeuver.getTack() == Tack.PORT && directionChangeInDegrees < 0;
        LegType legTypeBeforeManeuver = getPointOfSailBeforeManeuver(pointOfSailAfterManeuver).getLegType();
        LegType legTypeAfterManeuver = pointOfSailAfterManeuver.getLegType();
        if (bearAway) {
            switch (legTypeBeforeManeuver) {
            case UPWIND:
                switch (legTypeAfterManeuver) {
                case UPWIND:
                    return FineGrainedManeuverType.BEAR_AWAY_AT_UPWIND;
                case REACHING:
                    return FineGrainedManeuverType.BEAR_AWAY_FROM_UPWIND_UNTIL_REACHING;
                case DOWNWIND:
                    return FineGrainedManeuverType.BEAR_AWAY_FROM_UPWIND_UNTIL_DOWNWIND;
                }
            case REACHING:
                return legTypeAfterManeuver == LegType.DOWNWIND
                        ? FineGrainedManeuverType.BEAR_AWAY_FROM_REACHING_UNTIL_DOWNWIND
                        : FineGrainedManeuverType.BEAR_AWAY_AT_REACHING;
            case DOWNWIND:
                return FineGrainedManeuverType.BEAR_AWAY_AT_DOWNWIND;
            }
        } else {
            switch (legTypeBeforeManeuver) {
            case DOWNWIND:
                switch (legTypeAfterManeuver) {
                case DOWNWIND:
                    return FineGrainedManeuverType.HEAD_UP_AT_DOWNWIND;
                case REACHING:
                    return FineGrainedManeuverType.HEAD_UP_FROM_DOWNWIND_UNTIL_REACHING;
                case UPWIND:
                    return FineGrainedManeuverType.HEAD_UP_FROM_DOWNWIND_UNTIL_UPWIND;
                }
            case REACHING:
                return legTypeAfterManeuver == LegType.UPWIND
                        ? FineGrainedManeuverType.HEAD_UP_FROM_REACHING_UNTIL_UPWIND
                        : FineGrainedManeuverType.HEAD_UP_AT_REACHING;
            case UPWIND:
                return FineGrainedManeuverType.HEAD_UP_AT_UPWIND;
            }
        }
        throw new IllegalStateException();
    }

    public void computeProbabilitiesFromPreviousLevelToThisLevel() {
        for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
            double likelihoodForCurrentNode = maneuverEstimationResult
                    .getLikelihoodForPointOfSailAfterManeuver(currentNode.getCoarseGrainedPointOfSail());
            if (getPreviousLevel() == null) {
                setProbabilityFromPreviousLevelNodeToThisLevelNode(null, currentNode, likelihoodForCurrentNode);
            } else {
                for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                    double likelihoodForTransitionFromPreviousNodeToCurrentNode = likelihoodForCurrentNode
                            * getNodeTransitionPenaltyFactor(previousNode, currentNode)
                            * getBonusFactorForSymmetricTransition(previousNode, currentNode);
                    setProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode,
                            likelihoodForTransitionFromPreviousNodeToCurrentNode);
                }
            }
        }
        normalizeNodeTransitions();
        calculationOfTransitionProbabilitiesNeeded = false;
    }

    private double getBonusFactorForSymmetricTransition(FineGrainedPointOfSail previousNode,
            FineGrainedPointOfSail currentNode) {
        int previousAbsTwa = previousNode.getTwa();
        if (previousAbsTwa > 180) {
            previousAbsTwa = 360 - previousAbsTwa;
        }
        int currentAbsTwa = currentNode.getTwa();
        if (currentAbsTwa > 180) {
            currentAbsTwa = 360 - currentAbsTwa;
        }

        if (currentAbsTwa == previousAbsTwa) {
            return 1.02;
        }
        return 1;
    }

    public void setTackProbabilityBonusToManeuver(double tackProbabilityBonus) {
        this.calculationOfTransitionProbabilitiesNeeded = maneuverEstimationResult
                .setTackProbabilityBonus(tackProbabilityBonus);
    }

    public boolean isCalculationOfTransitionProbabilitiesNeeded() {
        return calculationOfTransitionProbabilitiesNeeded;
    }

    public void setUpwindBeforeProbabilityBonusToManeuver(double upwindBeforeProbabilityBonus) {
        this.calculationOfTransitionProbabilitiesNeeded = maneuverEstimationResult
                .setUpwindBeforeProbabilityBonus(upwindBeforeProbabilityBonus);
    }

    public void setUpwindAfterProbabilityBonusToManeuver(double upwindAfterProbabilityBonus) {
        this.calculationOfTransitionProbabilitiesNeeded = maneuverEstimationResult
                .setUpwindAfterProbabilityBonus(upwindAfterProbabilityBonus);
    }

}
