package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleManeuverClassificationResult {

    private final double lowestSpeedWithBeginningSpeedRatio;
    private final double courseChangeUntilLowestSpeed;
    private final double highestSpeedWithBeginningSpeedRatio;
    private final double enteringExitingSpeedRatio;
    private final double courseChangeDeg;
    private final double[] presumedManeuverTypeLikelihoodsByAngleAnalysis;
    private final double[] presumedManeuverTypeLikelihoodsBySpeedAnalysis;
    private double[] likelihoodsForPointOfSailBeforeManeuvers;
    private final SpeedWithBearingWithConfidence<Void> speedWithTwaIfTack;
    private final SpeedWithBearingWithConfidence<Void> speedWithTwaIfJibe;
    private final CompleteManeuverCurveWithEstimationData maneuver;

    public SingleManeuverClassificationResult(CompleteManeuverCurveWithEstimationData maneuver,
            double lowestSpeedWithBeginningSpeedRatio, double courseChangeUntilLowestSpeed,
            double highestSpeedWithBeginningSpeedRatio, double enteringExitingSpeedRatio, double courseChangeDeg,
            double[] presumedManeuverTypeLikelihoodsByAngleAnalysis,
            double[] presumedManeuverTypeLikelihoodsBySpeedAnalysis,
            SpeedWithBearingWithConfidence<Void> speedWithTwaIfTack,
            SpeedWithBearingWithConfidence<Void> speedWithTwaIfJibe) {
        this.maneuver = maneuver;
        this.lowestSpeedWithBeginningSpeedRatio = lowestSpeedWithBeginningSpeedRatio;
        this.courseChangeUntilLowestSpeed = lowestSpeedWithBeginningSpeedRatio;
        this.highestSpeedWithBeginningSpeedRatio = highestSpeedWithBeginningSpeedRatio;
        this.enteringExitingSpeedRatio = enteringExitingSpeedRatio;
        this.courseChangeDeg = courseChangeDeg;
        this.presumedManeuverTypeLikelihoodsByAngleAnalysis = presumedManeuverTypeLikelihoodsByAngleAnalysis;
        this.presumedManeuverTypeLikelihoodsBySpeedAnalysis = presumedManeuverTypeLikelihoodsBySpeedAnalysis;
        this.speedWithTwaIfTack = speedWithTwaIfTack;
        this.speedWithTwaIfJibe = speedWithTwaIfJibe;
        computeLikelihoodsForPointOfSailBeforeManeuver();
    }

    public double getLowestSpeedWithBeginningSpeedRatio() {
        return lowestSpeedWithBeginningSpeedRatio;
    }

    public double getEnteringExitingSpeedRatio() {
        return enteringExitingSpeedRatio;
    }

    public double getCourseChangeDeg() {
        return courseChangeDeg;
    }

    public double getHighestSpeedWithBeginningSpeedRatio() {
        return highestSpeedWithBeginningSpeedRatio;
    }

    public double getManeuverTypeLikelihoodByAngleAnalysis(PresumedManeuverType maneuverType) {
        return presumedManeuverTypeLikelihoodsByAngleAnalysis[maneuverType.ordinal()];
    }

    public double getManeuverTypeLikelihoodBySpeedAnalysis(PresumedManeuverType maneuverType) {
        return presumedManeuverTypeLikelihoodsBySpeedAnalysis[maneuverType.ordinal()];
    }

    public SpeedWithBearingWithConfidence<Void> getSpeedWithTwaIfTack() {
        return speedWithTwaIfTack;
    }

    public SpeedWithBearingWithConfidence<Void> getSpeedWithTwaIfJibe() {
        return speedWithTwaIfJibe;
    }

    private void computeLikelihoodsForPointOfSailBeforeManeuver() {
        likelihoodsForPointOfSailBeforeManeuvers = new double[CoarseGrainedPointOfSail.values().length];
        for (CoarseGrainedPointOfSail pointOfSailBeforeManeuver : CoarseGrainedPointOfSail.values()) {
            double likelihoodByAngleAnalysisSum = 0;
            double likelihoodBySpeedAnalysisSum = 0;
            double summandsCount = 0;
            for (PresumedManeuverType maneuverType : PresumedManeuverType.values()) {
                if (presumedManeuverTypeLikelihoodsByAngleAnalysis[maneuverType.ordinal()] != 0) {
                    boolean addLikelihood = false;
                    switch (maneuverType) {
                    case TACK:
                        if (courseChangeDeg < 0
                                && (pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_STARBOARD
                                        || (courseChangeDeg <= -110
                                                && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD))
                                || courseChangeDeg > 0
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_PORT
                                || (courseChangeDeg >= 110
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_PORT)) {
                            addLikelihood = true;
                        }
                        break;
                    case JIBE:
                        if (courseChangeDeg < 0
                                && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.DOWNWIND_STARBOARD
                                || (courseChangeDeg <= -85
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD)
                                || courseChangeDeg > 0
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.DOWNWIND_PORT
                                || (courseChangeDeg >= 85
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_PORT)) {
                            addLikelihood = true;
                        }
                        break;
                    case MARK_PASSING_LUV:
                        if (courseChangeDeg < 0 && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_PORT
                                || (courseChangeDeg >= -85
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_PORT)
                                || courseChangeDeg > 0
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_STARBOARD
                                || (courseChangeDeg <= 85
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD)) {
                            addLikelihood = true;
                        }
                        break;
                    case MARK_PASSING_LEE:
                        if (courseChangeDeg < 0
                                && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.DOWNWIND_STARBOARD
                                || (courseChangeDeg >= -85
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD)
                                || courseChangeDeg > 0
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.DOWNWIND_PORT
                                || (courseChangeDeg <= 85
                                        && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_PORT)) {
                            addLikelihood = true;
                        }
                        break;
                    case HEAD_UP_BEAR_AWAY:
                    case _180:
                        addLikelihood = true;
                        break;
                    case _360:
                        double courseChangeUntilLowestSpeed = this.courseChangeUntilLowestSpeed;
                        if (courseChangeUntilLowestSpeed < 0) {
                            if (courseChangeUntilLowestSpeed >= -100) {
                                if (pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_STARBOARD) {
                                    addLikelihood = true;
                                }
                            } else {
                                while (courseChangeUntilLowestSpeed <= -360) {
                                    courseChangeUntilLowestSpeed += 360;
                                }
                                if (courseChangeUntilLowestSpeed >= -100 || courseChangeUntilLowestSpeed <= -225) {
                                    if (pointOfSailBeforeManeuver.getLegType() == LegType.UPWIND) {
                                        addLikelihood = true;
                                    }
                                } else {
                                    if (pointOfSailBeforeManeuver.getLegType() == LegType.DOWNWIND) {
                                        addLikelihood = true;
                                    }
                                }
                            }
                        } else {
                            if (courseChangeUntilLowestSpeed <= 100) {
                                if (pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_PORT) {
                                    addLikelihood = true;
                                }
                            } else {
                                while (courseChangeUntilLowestSpeed >= 360) {
                                    courseChangeUntilLowestSpeed -= 360;
                                }
                                if (courseChangeUntilLowestSpeed <= 100 || courseChangeUntilLowestSpeed >= 225) {
                                    if (pointOfSailBeforeManeuver.getLegType() == LegType.UPWIND) {
                                        addLikelihood = true;
                                    }
                                } else {
                                    if (pointOfSailBeforeManeuver.getLegType() == LegType.DOWNWIND) {
                                        addLikelihood = true;
                                    }
                                }
                            }
                        }
                        break;
                    }
                    if (addLikelihood) {
                        likelihoodByAngleAnalysisSum += presumedManeuverTypeLikelihoodsByAngleAnalysis[maneuverType
                                .ordinal()];
                        likelihoodBySpeedAnalysisSum += presumedManeuverTypeLikelihoodsBySpeedAnalysis[maneuverType
                                .ordinal()];
                        ++summandsCount;
                    }
                }
            }
            likelihoodsForPointOfSailBeforeManeuvers[pointOfSailBeforeManeuver
                    .ordinal()] = (likelihoodByAngleAnalysisSum + likelihoodBySpeedAnalysisSum) / summandsCount / 2;
        }
        normalizeLikelihoodArray(likelihoodsForPointOfSailBeforeManeuvers);
    }

    private void normalizeLikelihoodArray(double[] likelihoodsForPointOfSailAfterManeuver) {
        double likelihoodSum = 0;
        for (double likelihood : likelihoodsForPointOfSailAfterManeuver) {
            likelihoodSum += likelihood;
        }
        for (int i = 0; i < likelihoodsForPointOfSailAfterManeuver.length; i++) {
            likelihoodsForPointOfSailAfterManeuver[i] = likelihoodsForPointOfSailAfterManeuver[i] / likelihoodSum;
        }
    }

    public double getLikelihoodForPointOfSailAfterManeuver(CoarseGrainedPointOfSail pointOfSailBeforeManeuver) {
        return likelihoodsForPointOfSailBeforeManeuvers[pointOfSailBeforeManeuver.ordinal()];
    }

    public CompleteManeuverCurveWithEstimationData getManeuver() {
        return maneuver;
    }

}
