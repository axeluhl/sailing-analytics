package com.sap.sailing.windestimation.maneuvergraph.impl.classifier;

import java.util.Collection;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.maneuvergraph.CoarseGrainedManeuverType;
import com.sap.sailing.windestimation.maneuvergraph.CoarseGrainedPointOfSail;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleManeuverClassificationResult {

    private final double[] likelihoodsForPointOfSailAfterManeuvers = new double[CoarseGrainedPointOfSail
            .values().length];
    private final double[] likelihoodPerManeuverType;
    private final CompleteManeuverCurveWithEstimationData maneuver;

    public SingleManeuverClassificationResult(CompleteManeuverCurveWithEstimationData maneuver,
            double[] likelihoodPerManeuverType, boolean cleanManeuverBoundaries) {
        this.maneuver = maneuver;
        this.likelihoodPerManeuverType = likelihoodPerManeuverType;
        SpeedWithBearing lowestSpeedWithinManeuverMainCurve = maneuver.getMainCurve().getLowestSpeed();
        double courseChangeDegUntilLowestSpeed = maneuver.getMainCurve().getSpeedWithBearingBefore().getBearing()
                .getDifferenceTo(lowestSpeedWithinManeuverMainCurve.getBearing()).getDegrees();
        if (courseChangeDegUntilLowestSpeed * maneuver.getMainCurve().getDirectionChangeInDegrees() < 0) {
            courseChangeDegUntilLowestSpeed = courseChangeDegUntilLowestSpeed < 0
                    ? courseChangeDegUntilLowestSpeed + 360 : courseChangeDegUntilLowestSpeed - 360;
        }
        computeLikelihoodsForPointOfSailAfterManeuver(courseChangeDegUntilLowestSpeed, cleanManeuverBoundaries);
    }

    private void computeLikelihoodsForPointOfSailAfterManeuver(double courseChangeDegUntilLowestSpeed,
            boolean cleanManeuverBoundaries) {
        double courseChangeDeg = maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees();
        for (CoarseGrainedPointOfSail pointOfSailAfterManeuver : CoarseGrainedPointOfSail.values()) {
            double likelihoodSum = 0;
            int numberOfLikelihoodSummands = 0;
            for (CoarseGrainedManeuverType maneuverType : CoarseGrainedManeuverType.values()) {
                if (likelihoodPerManeuverType[maneuverType.ordinal()] != 0) {
                    boolean addLikelihood = false;
                    switch (maneuverType) {
                    case TACK:
                        if (courseChangeDeg < 0
                                && (pointOfSailAfterManeuver == CoarseGrainedPointOfSail.UPWIND_PORT
                                        || (courseChangeDeg <= -110
                                                && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.REACHING_PORT))
                                || courseChangeDeg > 0
                                        && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.UPWIND_STARBOARD
                                || (courseChangeDeg >= 110
                                        && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD)) {
                            addLikelihood = true;
                        }
                        break;
                    case JIBE:
                        if (courseChangeDeg < 0 && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.DOWNWIND_PORT
                                || (courseChangeDeg <= -85
                                        && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.REACHING_PORT)
                                || courseChangeDeg > 0
                                        && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.DOWNWIND_STARBOARD
                                || (courseChangeDeg >= 85
                                        && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD)) {
                            addLikelihood = true;
                        }
                        break;
                    case BEAR_AWAY:
                        if (Math.abs(courseChangeDeg) > 40) {
                            if (courseChangeDeg < 0
                                    && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.DOWNWIND_PORT
                                    || (courseChangeDeg >= -80
                                            && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.REACHING_PORT)
                                    || courseChangeDeg > 0
                                            && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.DOWNWIND_STARBOARD
                                    || (courseChangeDeg <= 80
                                            && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD)) {
                                addLikelihood = true;
                            }
                        } else {
                            addLikelihood = true;
                        }
                        break;
                    case HEAD_UP:
                        if (Math.abs(courseChangeDeg) > 40) {
                            if (courseChangeDeg < 0
                                    && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.UPWIND_STARBOARD
                                    || (courseChangeDeg >= -80
                                            && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD)
                                    || courseChangeDeg > 0
                                            && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.UPWIND_PORT
                                    || (courseChangeDeg <= 80
                                            && pointOfSailAfterManeuver == CoarseGrainedPointOfSail.REACHING_PORT)) {
                                addLikelihood = true;
                            }
                        } else {
                            addLikelihood = true;
                        }
                        break;
                    case _180:
                        // TODO analyze speed ratio
                        SpeedWithBearing speedWithBearingBefore = maneuver.getCurveWithUnstableCourseAndSpeed()
                                .getSpeedWithBearingBefore();
                        SpeedWithBearing speedWithBearingAfter = maneuver.getCurveWithUnstableCourseAndSpeed()
                                .getSpeedWithBearingAfter();
                        double enteringExitingSpeedRatio = speedWithBearingBefore.getKnots()
                                / speedWithBearingAfter.getKnots();
                        if (enteringExitingSpeedRatio > 1.15 && Math.abs(courseChangeDegUntilLowestSpeed) < 20) {
                            if (courseChangeDeg < 0 && pointOfSailAfterManeuver.getLegType() == LegType.DOWNWIND
                                    || pointOfSailAfterManeuver.getLegType() == LegType.REACHING) {
                                addLikelihood = true;
                            }
                        } else if (enteringExitingSpeedRatio < 0.85
                                && Math.abs(courseChangeDeg - courseChangeDegUntilLowestSpeed) < 20) {
                            if (courseChangeDeg < 0 && pointOfSailAfterManeuver.getLegType() == LegType.UPWIND
                                    || pointOfSailAfterManeuver.getLegType() == LegType.REACHING) {
                                addLikelihood = true;
                            }
                        } else if (Math.abs(180 - courseChangeDeg) < 10
                                && Math.abs(1 - enteringExitingSpeedRatio) <= 0.05) {
                            if (pointOfSailAfterManeuver.getLegType() == LegType.REACHING) {
                                addLikelihood = true;
                            }
                        } else {
                            addLikelihood = true;
                        }
                        break;
                    case _360:
                        Collection<CoarseGrainedPointOfSail> possiblePointOfSailsBeforeManeuver = pointOfSailAfterManeuver
                                .getNextPossiblePointOfSails(-courseChangeDeg);
                        if (courseChangeDegUntilLowestSpeed < 0) {
                            if (courseChangeDegUntilLowestSpeed >= -100) {
                                if (possiblePointOfSailsBeforeManeuver
                                        .contains(CoarseGrainedPointOfSail.UPWIND_STARBOARD)) {
                                    addLikelihood = true;
                                }
                            } else {
                                while (courseChangeDegUntilLowestSpeed <= -360) {
                                    courseChangeDegUntilLowestSpeed += 360;
                                }
                                if (courseChangeDegUntilLowestSpeed >= -100
                                        || courseChangeDegUntilLowestSpeed <= -225) {
                                    if (possiblePointOfSailsBeforeManeuver.stream()
                                            .anyMatch(pointOfSailBeforeManeuver -> pointOfSailBeforeManeuver
                                                    .getLegType() == LegType.UPWIND)) {
                                        addLikelihood = true;
                                    }
                                } else {
                                    if (possiblePointOfSailsBeforeManeuver.stream()
                                            .anyMatch(pointOfSailBeforeManeuver -> pointOfSailBeforeManeuver
                                                    .getLegType() == LegType.DOWNWIND)) {
                                        addLikelihood = true;
                                    }
                                }
                            }
                        } else {
                            if (courseChangeDegUntilLowestSpeed <= 100) {
                                if (possiblePointOfSailsBeforeManeuver.contains(CoarseGrainedPointOfSail.UPWIND_PORT)) {
                                    addLikelihood = true;
                                }
                            } else {
                                while (courseChangeDegUntilLowestSpeed >= 360) {
                                    courseChangeDegUntilLowestSpeed -= 360;
                                }
                                if (courseChangeDegUntilLowestSpeed <= 100 || courseChangeDegUntilLowestSpeed >= 225) {
                                    if (possiblePointOfSailsBeforeManeuver.stream()
                                            .anyMatch(pointOfSailBeforeManeuver -> pointOfSailBeforeManeuver
                                                    .getLegType() == LegType.UPWIND)) {
                                        addLikelihood = true;
                                    }
                                } else {
                                    if (possiblePointOfSailsBeforeManeuver.stream()
                                            .anyMatch(pointOfSailBeforeManeuver -> pointOfSailBeforeManeuver
                                                    .getLegType() == LegType.DOWNWIND)) {
                                        addLikelihood = true;
                                    }
                                }
                            }
                        }
                        break;
                    }
                    if (addLikelihood) {
                        likelihoodSum += likelihoodPerManeuverType[maneuverType.ordinal()];
                        ++numberOfLikelihoodSummands;
                    }
                }
            }
            likelihoodsForPointOfSailAfterManeuvers[pointOfSailAfterManeuver.ordinal()] = likelihoodSum
                    / numberOfLikelihoodSummands;
        }
    }

    public CompleteManeuverCurveWithEstimationData getManeuver() {
        return maneuver;
    }

    public double getManeuverTypeLikelihood(CoarseGrainedManeuverType maneuverType) {
        return likelihoodPerManeuverType[maneuverType.ordinal()];
    }

    public double getLikelihoodForPointOfSailAfterManeuver(CoarseGrainedPointOfSail pointOfSailBeforeManeuver,
            double tackProbabilityBonus) {
        return likelihoodsForPointOfSailAfterManeuvers[pointOfSailBeforeManeuver.ordinal()];
    }

}
