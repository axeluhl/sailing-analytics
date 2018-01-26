package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.tracking.Maneuver;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverNodesLevel {

    private ManeuverNodesLevel previousLevel = null;
    private ManeuverNodesLevel nextLevel = null;

    private final Maneuver maneuver;
    private final SingleManeuverClassificationResult maneuverClassificationResult;

    private final double[] bestDistancesFromStart = new double[PresumedPointOfSail.values().length];
    private final double[] bestDistancesFromEnd = new double[bestDistancesFromStart.length];
    private final PresumedPointOfSail[] bestPrecedingNodesForThisNodes = new PresumedPointOfSail[bestDistancesFromStart.length];
    private final PresumedPointOfSail[] bestFollowingNodesForThisNodes = new PresumedPointOfSail[bestDistancesFromStart.length];

    public ManeuverNodesLevel(Maneuver maneuver, SingleManeuverClassifier singleManeuverClassifier,
            ManeuverNodesLevel previousLevel) {
        this.maneuver = maneuver;
        if (previousLevel != null) {
            this.previousLevel = previousLevel;
            previousLevel.setNextLevel(this);
        }
        maneuverClassificationResult = singleManeuverClassifier.computeClassificationResult(maneuver);
    }

    void setNextLevel(ManeuverNodesLevel nextLevel) {
        this.nextLevel = nextLevel;
    }

    public ManeuverNodesLevel getNextLevel() {
        return nextLevel;
    }

    void setPreviousLevel(ManeuverNodesLevel previousLevel) {
        this.previousLevel = previousLevel;
    }

    public ManeuverNodesLevel getPreviousLevel() {
        return previousLevel;
    }

    public Maneuver getManeuver() {
        return maneuver;
    }

    public SingleManeuverClassificationResult getManeuverClassificationResult() {
        return maneuverClassificationResult;
    }

    public double getDistanceToNodeFromStart(PresumedPointOfSail node) {
        return bestDistancesFromStart[node.ordinal()];
    }

    public double getDistanceToNodeFromEnd(PresumedPointOfSail node) {
        return bestDistancesFromEnd[node.ordinal()];
    }

    public void computeDistancesFromStart() {
        computeDistances(false);
    }

    public void computeDistancesFromEnd() {
        computeDistances(true);
    }

    private void computeDistances(boolean fromEnd) {
        final double[] bestDistances;
        final double[] previousLevelBestDistances;
        final ManeuverNodesLevel previousLevel;
        final PresumedPointOfSail[] bestPrecidingNodes;
        if (fromEnd) {
            bestDistances = this.bestDistancesFromEnd;
            previousLevelBestDistances = this.nextLevel.bestDistancesFromEnd;
            previousLevel = this.nextLevel;
            bestPrecidingNodes = this.bestFollowingNodesForThisNodes;
        } else {
            bestDistances = this.bestDistancesFromStart;
            previousLevelBestDistances = this.previousLevel.bestDistancesFromStart;
            previousLevel = this.previousLevel;
            bestPrecidingNodes = this.bestPrecedingNodesForThisNodes;
        }
        boolean markPassingIsNeighbour = isMarkPassingNeighbour();
        for (PresumedPointOfSail pointOfSailBeforeManeuver : PresumedPointOfSail.values()) {
            double likelihoodForPointOfSailBeforeManeuver = markPassingIsNeighbour
                    ? 1 / PresumedPointOfSail.values().length
                    : maneuverClassificationResult.getLikelihoodForPointOfSailBeforeManeuver(pointOfSailBeforeManeuver);
            if (previousLevel == null) {
                bestDistances[pointOfSailBeforeManeuver.ordinal()] = convertLikelihoodToDistance(
                        likelihoodForPointOfSailBeforeManeuver);
            } else {
                double courseChangeDegFromPreviousPointOfSailBefore = previousLevel.getManeuver()
                        .getManeuverCurveWithStableSpeedAndCourseBoundaries().getSpeedWithBearingBefore().getBearing()
                        .getDifferenceTo(maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                                .getSpeedWithBearingBefore().getBearing())
                        .getDegrees();
                double bestDistanceThroughPreviousLevel = Double.MAX_VALUE;
                PresumedPointOfSail bestPreviousLevelPointOfSailBeforeManeuver = null;
                for (PresumedPointOfSail previousLevelPointOfSailBeforeManeuver : PresumedPointOfSail.values()) {
                    double likelihoodForPointOfSailTransition = getLikelihoodForPointOfSailTransition(
                            previousLevelPointOfSailBeforeManeuver, pointOfSailBeforeManeuver,
                            courseChangeDegFromPreviousPointOfSailBefore, likelihoodForPointOfSailBeforeManeuver);
                    double distanceThroughPreviousLevelPointOfSailBeforeManeuver = previousLevelBestDistances[previousLevelPointOfSailBeforeManeuver
                            .ordinal()] + convertLikelihoodToDistance(likelihoodForPointOfSailTransition);
                    if (bestDistanceThroughPreviousLevel > distanceThroughPreviousLevelPointOfSailBeforeManeuver) {
                        bestDistanceThroughPreviousLevel = distanceThroughPreviousLevelPointOfSailBeforeManeuver;
                        bestPreviousLevelPointOfSailBeforeManeuver = previousLevelPointOfSailBeforeManeuver;
                    }
                }
                bestDistances[pointOfSailBeforeManeuver.ordinal()] = bestDistanceThroughPreviousLevel;
                bestPrecidingNodes[pointOfSailBeforeManeuver.ordinal()] = bestPreviousLevelPointOfSailBeforeManeuver;
            }
        }
    }

    private double getLikelihoodForPointOfSailTransition(PresumedPointOfSail previousLevelPointOfSailBeforeManeuver,
            PresumedPointOfSail pointOfSailBeforeManeuver, double courseChangeDegFromPreviousPointOfSailBefore,
            double likelihoodForPointOfSailBeforeManeuver) {
        PresumedPointOfSail lastIteratedPointOfSail = previousLevelPointOfSailBeforeManeuver;
        PresumedPointOfSail nextIteratedPointOfSail;
        double courseChangeDegLeft = courseChangeDegFromPreviousPointOfSailBefore;
        NauticalSide maneuverDirection;
        int courseChangeSignum;
        if (courseChangeDegFromPreviousPointOfSailBefore < 0) {
            maneuverDirection = NauticalSide.PORT;
            courseChangeSignum = -1;
        } else {
            maneuverDirection = NauticalSide.STARBOARD;
            courseChangeSignum = 1;
        }
        nextIteratedPointOfSail = lastIteratedPointOfSail.getNextPointOfSail(maneuverDirection);
        do {
            courseChangeDegLeft += lastIteratedPointOfSail.getDifferenceInDegrees(nextIteratedPointOfSail)
                    * courseChangeSignum;
            if (courseChangeDegLeft * courseChangeSignum > 0) {
                lastIteratedPointOfSail = nextIteratedPointOfSail;
                nextIteratedPointOfSail = nextIteratedPointOfSail.getNextPointOfSail(maneuverDirection);
            } else {
                break;
            }
        } while (true);
        if (pointOfSailBeforeManeuver == nextIteratedPointOfSail
                || pointOfSailBeforeManeuver == lastIteratedPointOfSail) {
            return likelihoodForPointOfSailBeforeManeuver + 0.001;
        }
        if (Math.abs(courseChangeDegFromPreviousPointOfSailBefore) <= 20
                && previousLevelPointOfSailBeforeManeuver.getLegType() == LegType.DOWNWIND
                && pointOfSailBeforeManeuver.getLegType() == LegType.DOWNWIND) {
            return 0.5 * likelihoodForPointOfSailBeforeManeuver + 0.001;
        }
        return 0.05 * likelihoodForPointOfSailBeforeManeuver + 0.001;
    }

    private boolean isMarkPassingNeighbour() {
        boolean markPassingIsNeighbour = false;
        if (previousLevel != null && previousLevel.getManeuver().getType() == ManeuverType.MARK_PASSING
                && previousLevel.getManeuver().getManeuverCurveWithStableSpeedAndCourseBoundaries().getTimePointAfter()
                        .until(maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries().getTimePointBefore())
                        .asSeconds() <= 24
                || nextLevel != null && nextLevel.getManeuver().getType() == ManeuverType.MARK_PASSING
                        && maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                                .getTimePointAfter().until(nextLevel.getManeuver()
                                        .getManeuverCurveWithStableSpeedAndCourseBoundaries().getTimePointBefore())
                                .asSeconds() <= 24) {
            markPassingIsNeighbour = true;
        }
        return markPassingIsNeighbour;
    }

    private double convertLikelihoodToDistance(double likelihoodForPointOfSailBeforeManeuver) {
        return 1 - likelihoodForPointOfSailBeforeManeuver;
    }

}
