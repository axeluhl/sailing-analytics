package com.sap.sailing.windestimation.impl.maneuvergraph;

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

    private final double[] bestDistancesFromStart = new double[FineGrainedPointOfSail.values().length];
    private final FineGrainedPointOfSail[] bestPrecedingNodesForThisNodes = new FineGrainedPointOfSail[bestDistancesFromStart.length];

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

    public double getDistanceToNodeFromStart(FineGrainedPointOfSail node) {
        return bestDistancesFromStart[node.ordinal()];
    }
    
    public FineGrainedPointOfSail getBestPrecedingNode(FineGrainedPointOfSail node) {
        return bestPrecedingNodesForThisNodes[node.ordinal()];
    }

    public void computeDistances() {
        boolean markPassingIsNeighbour = isMarkPassingNeighbour();
        for (FineGrainedPointOfSail pointOfSailBeforeManeuver : FineGrainedPointOfSail.values()) {
            double likelihoodForPointOfSailBeforeManeuver = markPassingIsNeighbour
                    ? 1 / CoarseGrainedPointOfSail.values().length
                    : maneuverClassificationResult.getLikelihoodForPointOfSailBeforeManeuver(
                            pointOfSailBeforeManeuver.getCoarseGrainedPointOfSail());
            if (this.previousLevel == null) {
                this.bestDistancesFromStart[pointOfSailBeforeManeuver.ordinal()] = convertLikelihoodToDistance(
                        likelihoodForPointOfSailBeforeManeuver);
            } else {
                double courseChangeDegFromPreviousPointOfSailBefore = this.previousLevel.getManeuver()
                        .getManeuverCurveWithStableSpeedAndCourseBoundaries().getSpeedWithBearingBefore().getBearing()
                        .getDifferenceTo(maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                                .getSpeedWithBearingBefore().getBearing())
                        .getDegrees();
                double bestDistanceThroughPreviousLevel = Double.MAX_VALUE;
                FineGrainedPointOfSail bestPreviousLevelPointOfSailBeforeManeuver = null;
                for (FineGrainedPointOfSail previousLevelPointOfSailBeforeManeuver : FineGrainedPointOfSail.values()) {
                    double likelihoodForPointOfSailTransition = getLikelihoodForPointOfSailTransition(
                            previousLevelPointOfSailBeforeManeuver, pointOfSailBeforeManeuver,
                            courseChangeDegFromPreviousPointOfSailBefore, likelihoodForPointOfSailBeforeManeuver);
                    double distanceThroughPreviousLevelPointOfSailBeforeManeuver = this.previousLevel.bestDistancesFromStart[previousLevelPointOfSailBeforeManeuver
                            .ordinal()] + convertLikelihoodToDistance(likelihoodForPointOfSailTransition);
                    if (bestDistanceThroughPreviousLevel > distanceThroughPreviousLevelPointOfSailBeforeManeuver) {
                        bestDistanceThroughPreviousLevel = distanceThroughPreviousLevelPointOfSailBeforeManeuver;
                        bestPreviousLevelPointOfSailBeforeManeuver = previousLevelPointOfSailBeforeManeuver;
                    }
                }
                this.bestDistancesFromStart[pointOfSailBeforeManeuver.ordinal()] = bestDistanceThroughPreviousLevel;
                this.bestPrecedingNodesForThisNodes[pointOfSailBeforeManeuver
                        .ordinal()] = bestPreviousLevelPointOfSailBeforeManeuver;
            }
        }
    }

    private double getLikelihoodForPointOfSailTransition(FineGrainedPointOfSail previousLevelPointOfSailBeforeManeuver,
            FineGrainedPointOfSail pointOfSailBeforeManeuver, double courseChangeDegFromPreviousPointOfSailBefore,
            double likelihoodForPointOfSailBeforeManeuver) {
        FineGrainedPointOfSail previousIteratedPointOfSail;
        FineGrainedPointOfSail currentIteratedPointOfSail = previousLevelPointOfSailBeforeManeuver;
        FineGrainedPointOfSail nextIteratedPointOfSail;
        double courseChangeDegLeft = courseChangeDegFromPreviousPointOfSailBefore;
        NauticalSide maneuverDirection;
        int courseChangeSignum;
        if (courseChangeDegFromPreviousPointOfSailBefore < 0) {
            previousIteratedPointOfSail = currentIteratedPointOfSail.getNextPointOfSail(NauticalSide.STARBOARD);
            maneuverDirection = NauticalSide.PORT;
            courseChangeSignum = -1;
        } else {
            previousIteratedPointOfSail = currentIteratedPointOfSail.getNextPointOfSail(NauticalSide.PORT);
            maneuverDirection = NauticalSide.STARBOARD;
            courseChangeSignum = 1;
        }
        nextIteratedPointOfSail = currentIteratedPointOfSail.getNextPointOfSail(maneuverDirection);
        do {
            courseChangeDegLeft += currentIteratedPointOfSail.getDifferenceInDegrees(nextIteratedPointOfSail)
                    * courseChangeSignum;
            if (courseChangeDegLeft * courseChangeSignum > 0) {
                previousIteratedPointOfSail = currentIteratedPointOfSail;
                currentIteratedPointOfSail = nextIteratedPointOfSail;
                nextIteratedPointOfSail = nextIteratedPointOfSail.getNextPointOfSail(maneuverDirection);
            } else {
                break;
            }
        } while (true);
        if (pointOfSailBeforeManeuver == nextIteratedPointOfSail
                || pointOfSailBeforeManeuver == currentIteratedPointOfSail) {
            return likelihoodForPointOfSailBeforeManeuver + 0.001;
        }
        if (pointOfSailBeforeManeuver == previousIteratedPointOfSail && Math.abs(courseChangeDegLeft) <= 10) {
            return 0.6 * likelihoodForPointOfSailBeforeManeuver + 0.001;
        }
        double targetCourseChangeDeviation = currentIteratedPointOfSail.getTwa() - pointOfSailBeforeManeuver.getTwa();
        if (targetCourseChangeDeviation <= -180) {
            targetCourseChangeDeviation += 180;
        } else if (targetCourseChangeDeviation > 180) {
            targetCourseChangeDeviation -= 180;
        }
        return 1 / (1 + (targetCourseChangeDeviation / 5)) * likelihoodForPointOfSailBeforeManeuver + 0.001;
    }

    private boolean isMarkPassingNeighbour() {
        boolean markPassingIsNeighbour = false;
        if (previousLevel != null && previousLevel.getManeuver().isMarkPassing()
                && previousLevel.getManeuver().getManeuverCurveWithStableSpeedAndCourseBoundaries().getTimePointAfter()
                        .until(maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries().getTimePointBefore())
                        .asSeconds() <= 24
                || nextLevel != null && nextLevel.getManeuver().isMarkPassing()
                        && maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                                .getTimePointAfter().until(nextLevel.getManeuver()
                                        .getManeuverCurveWithStableSpeedAndCourseBoundaries().getTimePointBefore())
                                .asSeconds() <= 24) {
            markPassingIsNeighbour = true;
        }
        return markPassingIsNeighbour;
    }

    private double convertLikelihoodToDistance(double likelihoodForPointOfSailBeforeManeuver) {
        return 1 / (likelihoodForPointOfSailBeforeManeuver * likelihoodForPointOfSailBeforeManeuver);
    }

    public FineGrainedPointOfSail[] getBestPrecedingNodesForThisNodes() {
        return bestPrecedingNodesForThisNodes;
    }

    public double[] getBestDistancesFromStart() {
        return bestDistancesFromStart;
    }

}
