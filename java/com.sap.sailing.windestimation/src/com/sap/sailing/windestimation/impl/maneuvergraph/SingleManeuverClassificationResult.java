package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.common.LegType;

public class SingleManeuverClassificationResult {

    private final double lowestSpeedWithBeginningSpeedRatio;
    private final double courseChangeUntilLowestSpeed;
    private final double highestSpeedWithBeginningSpeedRatio;
    private final double enteringExitingSpeedRatio;
    private final double courseChangeDeg;
    private final double[] presumedManeuverTypeLikelihoodsByAngleAnalysis;
    private final double[] presumedManeuverTypeLikelihoodsBySpeedAnalysis;
    private final double presumedTwsInKnotsIfTack;
    private final double presumedTwsInKnotsIfJibe;
    private double[] likelihoodsForPointOfSailBeforeManeuvers;

    public SingleManeuverClassificationResult(double lowestSpeedWithBeginningSpeedRatio, double courseChangeUntilLowestSpeed,
            double highestSpeedWithBeginningSpeedRatio, double enteringExitingSpeedRatio, double courseChangeDeg,
            double[] presumedManeuverTypeLikelihoodsByAngleAnalysis,
            double[] presumedManeuverTypeLikelihoodsBySpeedAnalysis,
            double presumedTwsInKnotsIfTack, double presumedTwsInKnotsIfJibe) {
        this.lowestSpeedWithBeginningSpeedRatio = lowestSpeedWithBeginningSpeedRatio;
        this.courseChangeUntilLowestSpeed = lowestSpeedWithBeginningSpeedRatio;
        this.highestSpeedWithBeginningSpeedRatio = highestSpeedWithBeginningSpeedRatio;
        this.enteringExitingSpeedRatio = enteringExitingSpeedRatio;
        this.courseChangeDeg = courseChangeDeg;
        this.presumedManeuverTypeLikelihoodsByAngleAnalysis = presumedManeuverTypeLikelihoodsByAngleAnalysis;
        this.presumedManeuverTypeLikelihoodsBySpeedAnalysis = presumedManeuverTypeLikelihoodsBySpeedAnalysis;
        this.presumedTwsInKnotsIfTack = presumedTwsInKnotsIfTack;
        this.presumedTwsInKnotsIfJibe = presumedTwsInKnotsIfJibe;
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

    public double getPresumedTwsInKnotsIfTack() {
        return presumedTwsInKnotsIfTack;
    }

    public double getPresumedTwsInKnotsIfJibe() {
        return presumedTwsInKnotsIfJibe;
    }

    private void computeLikelihoodsForPointOfSailBeforeManeuver() {
        likelihoodsForPointOfSailBeforeManeuvers = new double[PresumedPointOfSail.values().length];
        for(PresumedPointOfSail pointOfSailBeforeManeuver : PresumedPointOfSail.values()) {
            double likelihoodByAngleAnalysisSum = 0;
            double likelihoodBySpeedAnalysisSum = 0;
            double summandsCount = 0;
            for(PresumedManeuverType maneuverType : PresumedManeuverType.values()) {
                if(presumedManeuverTypeLikelihoodsByAngleAnalysis[maneuverType.ordinal()] != 0) {
                    boolean addLikelihood = false;
                    switch(maneuverType) {
                    case TACK:
                        if(courseChangeDeg < 0 && pointOfSailBeforeManeuver == PresumedPointOfSail.UPWIND_STARBOARD || courseChangeDeg > 0 && pointOfSailBeforeManeuver == PresumedPointOfSail.UPWIND_PORT) {
                            addLikelihood = true;
                        }
                        break;
                    case JIBE:
                        if(courseChangeDeg < 0 && pointOfSailBeforeManeuver == PresumedPointOfSail.DOWNWIND_STARBOARD || courseChangeDeg > 0 && pointOfSailBeforeManeuver == PresumedPointOfSail.DOWNWIND_PORT) {
                            addLikelihood = true;
                        }
                        break;
                    case MARK_PASSING_LUV:
                        if(courseChangeDeg < 0 && pointOfSailBeforeManeuver == PresumedPointOfSail.UPWIND_PORT || courseChangeDeg > 0 && pointOfSailBeforeManeuver == PresumedPointOfSail.UPWIND_STARBOARD) {
                            addLikelihood = true;
                        }
                        break;
                    case MARK_PASSING_LEE:
                        if(courseChangeDeg < 0 && pointOfSailBeforeManeuver == PresumedPointOfSail.DOWNWIND_STARBOARD || courseChangeDeg > 0 && pointOfSailBeforeManeuver == PresumedPointOfSail.DOWNWIND_PORT) {
                            addLikelihood = true;
                        }
                        break;
                    case HEAD_UP_BEAR_AWAY:
                    case _180:
                        addLikelihood = true;
                        break;
                    case _360:
                        double courseChangeUntilLowestSpeed = this.courseChangeUntilLowestSpeed;
                        if(courseChangeUntilLowestSpeed < 0) {
                            if(courseChangeUntilLowestSpeed >= -100) {
                                if(pointOfSailBeforeManeuver == PresumedPointOfSail.UPWIND_STARBOARD) {
                                    addLikelihood = true;
                                }
                            } else {
                                while(courseChangeUntilLowestSpeed <= -360) {
                                    courseChangeUntilLowestSpeed += 360;
                                }
                                if(courseChangeUntilLowestSpeed >= -100 || courseChangeUntilLowestSpeed <= -225) {
                                    if(pointOfSailBeforeManeuver.getLegType() == LegType.UPWIND) {
                                        addLikelihood = true;
                                    }
                                } else {
                                    if(pointOfSailBeforeManeuver.getLegType() == LegType.DOWNWIND) {
                                        addLikelihood = true;
                                    }
                                }
                            }
                        } else {
                            if(courseChangeUntilLowestSpeed <= 100) {
                                if(pointOfSailBeforeManeuver == PresumedPointOfSail.UPWIND_PORT) {
                                    addLikelihood = true;
                                }
                            } else {
                                while(courseChangeUntilLowestSpeed >= 360) {
                                    courseChangeUntilLowestSpeed -= 360;
                                }
                                if(courseChangeUntilLowestSpeed <= 100 || courseChangeUntilLowestSpeed >= 225) {
                                    if(pointOfSailBeforeManeuver.getLegType() == LegType.UPWIND) {
                                        addLikelihood = true;
                                    }
                                } else {
                                    if(pointOfSailBeforeManeuver.getLegType() == LegType.DOWNWIND) {
                                        addLikelihood = true;
                                    }
                                }
                            }
                        }
                        break;
                    }
                    if(addLikelihood) {
                        likelihoodByAngleAnalysisSum += presumedManeuverTypeLikelihoodsByAngleAnalysis[maneuverType.ordinal()];
                        likelihoodBySpeedAnalysisSum += presumedManeuverTypeLikelihoodsBySpeedAnalysis[maneuverType.ordinal()];
                        ++summandsCount;
                    }
                }
            }
            likelihoodsForPointOfSailBeforeManeuvers[pointOfSailBeforeManeuver.ordinal()] = (likelihoodByAngleAnalysisSum + likelihoodBySpeedAnalysisSum) / summandsCount / 2;
        }
        normalizeLikelihoodArray(likelihoodsForPointOfSailBeforeManeuvers);
    }

    private void normalizeLikelihoodArray(double[] likelihoodForPointOfSailBeforeManeuver) {
        double likelihoodSum = 0;
        for (double likelihood : likelihoodForPointOfSailBeforeManeuver) {
            likelihoodSum += likelihood;
        }
        for (int i = 0; i < likelihoodForPointOfSailBeforeManeuver.length; i++) {
            likelihoodForPointOfSailBeforeManeuver[i] = likelihoodForPointOfSailBeforeManeuver[i] / likelihoodSum;
        }
    }

    public double getLikelihoodForPointOfSailBeforeManeuver(PresumedPointOfSail pointOfSailBeforeManeuver) {
        return likelihoodsForPointOfSailBeforeManeuvers[pointOfSailBeforeManeuver.ordinal()];
    }
    
}
