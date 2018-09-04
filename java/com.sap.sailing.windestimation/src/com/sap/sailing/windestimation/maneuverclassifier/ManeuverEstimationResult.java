package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.Arrays;
import java.util.Collection;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuvergraph.CoarseGrainedManeuverType;
import com.sap.sailing.windestimation.maneuvergraph.CoarseGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.ProbabilityUtil;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverEstimationResult {

    private final double[] likelihoodsForPointOfSailAfterManeuvers = new double[CoarseGrainedPointOfSail
            .values().length];
    private final double[] likelihoodPerManeuverType;
    private final ManeuverForEstimation maneuver;
    private double tackProbabilityBonus = 0.0;
    private double upwindBeforeProbabilityBonus = 0.0;
    private double upwindAfterProbabilityBonus = 0.0;

    public ManeuverEstimationResult(ManeuverForEstimation maneuver, double[] likelihoodPerManeuverType) {
        this.maneuver = maneuver;
        this.likelihoodPerManeuverType = likelihoodPerManeuverType;
        computeLikelihoodsForPointOfSailAfterManeuver();
    }

    private void computeLikelihoodsForPointOfSailAfterManeuver() {
        double maxlikelihood = Arrays.stream(likelihoodPerManeuverType).max().getAsDouble();
        double courseChangeDeg = maneuver.getCourseChangeInDegrees();
        double courseChangeDegUntilLowestSpeed = maneuver.getSpeedWithBearingBefore().getBearing()
                .getDifferenceTo(maneuver.getCourseAtLowestSpeed()).getDegrees();
        if (courseChangeDegUntilLowestSpeed * maneuver.getCourseChangeInDegrees() < 0) {
            courseChangeDegUntilLowestSpeed = courseChangeDegUntilLowestSpeed < 0
                    ? courseChangeDegUntilLowestSpeed + 360 : courseChangeDegUntilLowestSpeed - 360;
        }
        for (CoarseGrainedPointOfSail pointOfSailAfterManeuver : CoarseGrainedPointOfSail.values()) {
            double likelihood = 0;
            for (CoarseGrainedManeuverType maneuverType : CoarseGrainedManeuverType.values()) {
                if (likelihoodPerManeuverType[maneuverType.ordinal()] != 0) {
                    boolean addLikelihood = false;
                    boolean addTackProbabilityBonus = false;
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
                            addTackProbabilityBonus = true;
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
                        SpeedWithBearing speedWithBearingBefore = maneuver.getSpeedWithBearingBefore();
                        SpeedWithBearing speedWithBearingAfter = maneuver.getSpeedWithBearingAfter();
                        double enteringExitingSpeedRatio = speedWithBearingBefore.getKnots()
                                / speedWithBearingAfter.getKnots();
                        if (enteringExitingSpeedRatio < 0.85 && Math.abs(courseChangeDegUntilLowestSpeed) < 20) {
                            if (pointOfSailAfterManeuver.getLegType() == LegType.DOWNWIND
                                    || pointOfSailAfterManeuver.getLegType() == LegType.REACHING) {
                                addLikelihood = true;
                            }
                        } else if (enteringExitingSpeedRatio > 1.15
                                && Math.abs(courseChangeDeg - courseChangeDegUntilLowestSpeed) < 20) {
                            if (pointOfSailAfterManeuver.getLegType() == LegType.UPWIND
                                    || pointOfSailAfterManeuver.getLegType() == LegType.REACHING) {
                                addLikelihood = true;
                            }
                        }
                        if (addLikelihood) {
                            if (courseChangeDeg < 0 && pointOfSailAfterManeuver.getTack() == Tack.PORT
                                    || courseChangeDeg > 0 && pointOfSailAfterManeuver.getTack() == Tack.STARBOARD) {
                                addTackProbabilityBonus = true;
                            }
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
                                    if (possiblePointOfSailsBeforeManeuver.stream().anyMatch(
                                            pointOfSailBefore -> pointOfSailBefore.getLegType() == LegType.UPWIND)) {
                                        addLikelihood = true;
                                    }
                                } else {
                                    if (possiblePointOfSailsBeforeManeuver.stream().anyMatch(
                                            pointOfSailBefore -> pointOfSailBefore.getLegType() == LegType.DOWNWIND)) {
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
                                    if (possiblePointOfSailsBeforeManeuver.stream().anyMatch(
                                            pointOfSailBefore -> pointOfSailBefore.getLegType() == LegType.UPWIND)) {
                                        addLikelihood = true;
                                    }
                                } else {
                                    if (possiblePointOfSailsBeforeManeuver.stream().anyMatch(
                                            pointOfSailBefore -> pointOfSailBefore.getLegType() == LegType.DOWNWIND)) {
                                        addLikelihood = true;
                                    }
                                }
                            }
                        }
                        break;
                    }
                    if (addLikelihood) {
                        double newLikelihood = likelihoodPerManeuverType[maneuverType.ordinal()];
                        if (addTackProbabilityBonus) {
                            newLikelihood += tackProbabilityBonus * maxlikelihood;
                        }
                        if (newLikelihood > likelihood) {
                            likelihood = newLikelihood;
                        }
                    }
                    if (upwindBeforeProbabilityBonus != 0) {
                        Collection<CoarseGrainedPointOfSail> possiblePointOfSailsBeforeManeuver = pointOfSailAfterManeuver
                                .getNextPossiblePointOfSails(maneuver.getCourseChangeInDegrees() * -1);
                        for (CoarseGrainedPointOfSail pointOfSailBeforeManeuver : possiblePointOfSailsBeforeManeuver) {
                            if (pointOfSailBeforeManeuver.getLegType() == LegType.UPWIND) {
                                likelihood += upwindBeforeProbabilityBonus;
                                break;
                            }
                        }
                    }
                    if (upwindAfterProbabilityBonus != 0 && pointOfSailAfterManeuver.getLegType() == LegType.UPWIND) {
                        likelihood += upwindAfterProbabilityBonus;
                    }
                }
            }
            if (likelihood >= 0) {
                likelihoodsForPointOfSailAfterManeuvers[pointOfSailAfterManeuver.ordinal()] = likelihood;
            }
        }
        ProbabilityUtil.normalizeLikelihoodArray(likelihoodsForPointOfSailAfterManeuvers, 0.01);
    }

    public ManeuverForEstimation getManeuver() {
        return maneuver;
    }

    public double getManeuverTypeLikelihood(CoarseGrainedManeuverType maneuverType) {
        return likelihoodPerManeuverType[maneuverType.ordinal()];
    }

    public double getLikelihoodForPointOfSailAfterManeuver(CoarseGrainedPointOfSail pointOfSailBeforeManeuver) {
        return likelihoodsForPointOfSailAfterManeuvers[pointOfSailBeforeManeuver.ordinal()];
    }

    public boolean setTackProbabilityBonus(double tackProbabilityBonus) {
        if (Math.abs(this.tackProbabilityBonus - tackProbabilityBonus) > 0.0001) {
            this.tackProbabilityBonus = tackProbabilityBonus;
            computeLikelihoodsForPointOfSailAfterManeuver();
            return true;
        }
        return false;
    }

    public double getTackProbabilityBonus() {
        return tackProbabilityBonus;
    }

    public boolean setUpwindBeforeProbabilityBonus(double upwindBeforeProbabilityBonus) {
        if (Math.abs(this.upwindBeforeProbabilityBonus - upwindBeforeProbabilityBonus) > 0.0001) {
            this.upwindBeforeProbabilityBonus = upwindBeforeProbabilityBonus;
            computeLikelihoodsForPointOfSailAfterManeuver();
            return true;
        }
        return false;
    }

    public boolean setUpwindAfterProbabilityBonus(double upwindAfterProbabilityBonus) {
        if (Math.abs(this.upwindAfterProbabilityBonus - upwindAfterProbabilityBonus) > 0.0001) {
            this.upwindAfterProbabilityBonus = upwindAfterProbabilityBonus;
            computeLikelihoodsForPointOfSailAfterManeuver();
            return true;
        }
        return false;
    }

}
