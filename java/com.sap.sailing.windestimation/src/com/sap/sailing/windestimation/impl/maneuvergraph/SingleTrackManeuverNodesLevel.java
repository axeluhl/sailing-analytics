package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.common.NauticalSide;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleTrackManeuverNodesLevel extends AbstractManeuverNodesLevel<SingleTrackManeuverNodesLevel> {

    private final SingleManeuverClassificationResult maneuverClassificationResult;

    public SingleTrackManeuverNodesLevel(SingleManeuverClassificationResult singleManeuverClassificationResult) {
        super(singleManeuverClassificationResult.getManeuver());
        maneuverClassificationResult = singleManeuverClassificationResult;
    }

    @Override
    public void computeDistancesFromPreviousLevelToThisLevel() {
        for (FineGrainedPointOfSail pointOfSailAfterManeuver : FineGrainedPointOfSail.values()) {
            double likelihoodForPointOfSailBeforeManeuver = maneuverClassificationResult
                    .getLikelihoodForPointOfSailBeforeManeuver(pointOfSailAfterManeuver.getCoarseGrainedPointOfSail());
            if (getPreviousLevel() == null) {
                this.nodeTransitions[pointOfSailAfterManeuver.ordinal()].setBestPreviousNode(null,
                        convertLikelihoodToDistance(likelihoodForPointOfSailBeforeManeuver));
            } else {
                double courseChangeDegFromPreviousPointOfSailBefore = getPreviousLevel().getManeuver()
                        .getCurveWithUnstableCourseAndSpeed()
                        .getSpeedWithBearingBefore().getBearing().getDifferenceTo(getManeuver()
                                .getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getBearing())
                        .getDegrees();
                double bestDistanceThroughPreviousLevel = Double.MAX_VALUE;
                FineGrainedPointOfSail bestPreviousLevelPointOfSailBeforeManeuver = null;
                for (FineGrainedPointOfSail previousLevelPointOfSailBeforeManeuver : FineGrainedPointOfSail.values()) {
                    double likelihoodForPointOfSailTransition = getLikelihoodForPointOfSailTransition(
                            previousLevelPointOfSailBeforeManeuver, pointOfSailAfterManeuver,
                            courseChangeDegFromPreviousPointOfSailBefore, likelihoodForPointOfSailBeforeManeuver);
                    double distanceThroughPreviousLevelPointOfSailBeforeManeuver = getPreviousLevel()
                            .getBestDistanceToNodeFromStart(previousLevelPointOfSailBeforeManeuver)
                            + convertLikelihoodToDistance(likelihoodForPointOfSailTransition);
                    if (bestDistanceThroughPreviousLevel > distanceThroughPreviousLevelPointOfSailBeforeManeuver) {
                        bestDistanceThroughPreviousLevel = distanceThroughPreviousLevelPointOfSailBeforeManeuver;
                        bestPreviousLevelPointOfSailBeforeManeuver = previousLevelPointOfSailBeforeManeuver;
                    }
                }
                this.nodeTransitions[pointOfSailAfterManeuver.ordinal()].setBestPreviousNode(
                        bestPreviousLevelPointOfSailBeforeManeuver, bestDistanceThroughPreviousLevel);
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

    private double convertLikelihoodToDistance(double likelihoodForPointOfSailBeforeManeuver) {
        return 1 / (likelihoodForPointOfSailBeforeManeuver * likelihoodForPointOfSailBeforeManeuver);
    }

    public static ManeuverNodesLevelFactory<SingleTrackManeuverNodesLevel, SingleManeuverClassificationResult> getFactory() {
        return new ManeuverNodesLevelFactory<SingleTrackManeuverNodesLevel, SingleManeuverClassificationResult>() {

            @Override
            public SingleTrackManeuverNodesLevel createNewManeuverNodesLevel(
                    SingleManeuverClassificationResult singleManeuverClassificationResult) {
                return new SingleTrackManeuverNodesLevel(singleManeuverClassificationResult);
            }
        };
    }

}
