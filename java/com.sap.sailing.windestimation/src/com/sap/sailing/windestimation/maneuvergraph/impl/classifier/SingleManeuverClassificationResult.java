package com.sap.sailing.windestimation.maneuvergraph.impl.classifier;

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

    private final double courseChangeDegUntilLowestSpeed;
    private final double courseChangeDeg;
    private double[] likelihoodsForPointOfSailAfterManeuvers;
    private final CompleteManeuverCurveWithEstimationData maneuver;
    private final double[] likelihoodPerManeuverType;

    public SingleManeuverClassificationResult(CompleteManeuverCurveWithEstimationData maneuver,
            double[] likelihoodPerManeuverType) {
        this.maneuver = maneuver;
        this.likelihoodPerManeuverType = likelihoodPerManeuverType;
        SpeedWithBearing lowestSpeedWithinManeuverMainCurve = maneuver.getMainCurve().getLowestSpeed();
        double courseChangeDegUntilLowestSpeed = maneuver.getMainCurve().getSpeedWithBearingBefore().getBearing()
                .getDifferenceTo(lowestSpeedWithinManeuverMainCurve.getBearing()).getDegrees();
        if (courseChangeDegUntilLowestSpeed * maneuver.getMainCurve().getDirectionChangeInDegrees() < 0) {
            courseChangeDegUntilLowestSpeed = courseChangeDegUntilLowestSpeed < 0
                    ? courseChangeDegUntilLowestSpeed + 360 : courseChangeDegUntilLowestSpeed - 360;
        }
        this.courseChangeDegUntilLowestSpeed = courseChangeDegUntilLowestSpeed;
        this.courseChangeDeg = maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees();
        computeLikelihoodsForPointOfSailAfterManeuver();
    }

    public double getCourseChangeDeg() {
        return courseChangeDeg;
    }

    public double getManeuverTypeLikelihood(CoarseGrainedManeuverType maneuverType) {
        return likelihoodPerManeuverType[maneuverType.ordinal()];
    }

    private void computeLikelihoodsForPointOfSailAfterManeuver() {
        likelihoodsForPointOfSailAfterManeuvers = new double[CoarseGrainedPointOfSail.values().length];
        // for (CoarseGrainedPointOfSail pointOfSailBeforeManeuver : CoarseGrainedPointOfSail.values()) {
        // double likelihoodByAngleAnalysisSum = 0;
        // double likelihoodBySpeedAnalysisSum = 0;
        // double summandsCount = 0;
        // for (CoarseGrainedManeuverType maneuverType : CoarseGrainedManeuverType.values()) {
        // if (presumedManeuverTypeLikelihoodsByAngleAnalysis[maneuverType.ordinal()] != 0) {
        // boolean addLikelihood = false;
        // switch (maneuverType) {
        // case TACK:
        // if (courseChangeDeg < 0
        // && (pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_STARBOARD
        // || (courseChangeDeg <= -110
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD))
        // || courseChangeDeg > 0
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_PORT
        // || (courseChangeDeg >= 110
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_PORT)) {
        // addLikelihood = true;
        // }
        // break;
        // case JIBE:
        // if (courseChangeDeg < 0
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.DOWNWIND_STARBOARD
        // || (courseChangeDeg <= -85
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD)
        // || courseChangeDeg > 0
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.DOWNWIND_PORT
        // || (courseChangeDeg >= 85
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_PORT)) {
        // addLikelihood = true;
        // }
        // break;
        // case MARK_PASSING_LUV:
        // if (courseChangeDeg < 0 && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_PORT
        // || (courseChangeDeg >= -85
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_PORT)
        // || courseChangeDeg > 0
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_STARBOARD
        // || (courseChangeDeg <= 85
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD)) {
        // addLikelihood = true;
        // }
        // break;
        // case MARK_PASSING_LEE:
        // if (courseChangeDeg < 0
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.DOWNWIND_STARBOARD
        // || (courseChangeDeg >= -85
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_STARBOARD)
        // || courseChangeDeg > 0
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.DOWNWIND_PORT
        // || (courseChangeDeg <= 85
        // && pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.REACHING_PORT)) {
        // addLikelihood = true;
        // }
        // break;
        // case HEAD_UP_BEAR_AWAY:
        // case _180:
        // addLikelihood = true;
        // break;
        // case _360:
        // double courseChangeUntilLowestSpeed = this.courseChangeUntilLowestSpeed;
        // if (courseChangeUntilLowestSpeed < 0) {
        // if (courseChangeUntilLowestSpeed >= -100) {
        // if (pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_STARBOARD) {
        // addLikelihood = true;
        // }
        // } else {
        // while (courseChangeUntilLowestSpeed <= -360) {
        // courseChangeUntilLowestSpeed += 360;
        // }
        // if (courseChangeUntilLowestSpeed >= -100 || courseChangeUntilLowestSpeed <= -225) {
        // if (pointOfSailBeforeManeuver.getLegType() == LegType.UPWIND) {
        // addLikelihood = true;
        // }
        // } else {
        // if (pointOfSailBeforeManeuver.getLegType() == LegType.DOWNWIND) {
        // addLikelihood = true;
        // }
        // }
        // }
        // } else {
        // if (courseChangeUntilLowestSpeed <= 100) {
        // if (pointOfSailBeforeManeuver == CoarseGrainedPointOfSail.UPWIND_PORT) {
        // addLikelihood = true;
        // }
        // } else {
        // while (courseChangeUntilLowestSpeed >= 360) {
        // courseChangeUntilLowestSpeed -= 360;
        // }
        // if (courseChangeUntilLowestSpeed <= 100 || courseChangeUntilLowestSpeed >= 225) {
        // if (pointOfSailBeforeManeuver.getLegType() == LegType.UPWIND) {
        // addLikelihood = true;
        // }
        // } else {
        // if (pointOfSailBeforeManeuver.getLegType() == LegType.DOWNWIND) {
        // addLikelihood = true;
        // }
        // }
        // }
        // }
        // break;
        // }
        // if (addLikelihood) {
        // likelihoodByAngleAnalysisSum += presumedManeuverTypeLikelihoodsByAngleAnalysis[maneuverType
        // .ordinal()];
        // likelihoodBySpeedAnalysisSum += presumedManeuverTypeLikelihoodsBySpeedAnalysis[maneuverType
        // .ordinal()];
        // ++summandsCount;
        // }
        // }
        // }
        // likelihoodsForPointOfSailBeforeManeuvers[pointOfSailBeforeManeuver
        // .ordinal()] = (likelihoodByAngleAnalysisSum + likelihoodBySpeedAnalysisSum) / summandsCount / 2;
        // }
    }

    public double getLikelihoodForPointOfSailAfterManeuver(CoarseGrainedPointOfSail pointOfSailBeforeManeuver,
            double tackProbabilityBonus) {
        return likelihoodsForPointOfSailAfterManeuvers[pointOfSailBeforeManeuver.ordinal()];
    }

    public CompleteManeuverCurveWithEstimationData getManeuver() {
        return maneuver;
    }

}
