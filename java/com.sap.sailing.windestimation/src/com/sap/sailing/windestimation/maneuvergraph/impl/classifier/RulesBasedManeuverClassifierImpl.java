package com.sap.sailing.windestimation.maneuvergraph.impl.classifier;

import java.util.Arrays;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.maneuvergraph.CoarseGrainedManeuverType;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class RulesBasedManeuverClassifierImpl implements ManeuverClassifier {

    private final BoatClass boatClass;
    private final PolarDataService polarService;
    private final boolean hasPolarsForBoatClass;

    public RulesBasedManeuverClassifierImpl(BoatClass boatClass, PolarDataService polarService) {
        this.boatClass = boatClass;
        this.polarService = polarService;
        this.hasPolarsForBoatClass = polarService.getAllBoatClassesWithPolarSheetsAvailable().contains(boatClass);
    }

    public ManeuverClassificationResult classifyManeuver(CompleteManeuverCurveWithEstimationData maneuver,
            CompleteManeuverCurveWithEstimationData previousManeuver,
            CompleteManeuverCurveWithEstimationData nextManeuver) {
        double[] likelihoodPerManeuverType = new double[CoarseGrainedManeuverType.values().length];

        double absCourseChangeDegMainCurve = Math.abs(maneuver.getMainCurve().getDirectionChangeInDegrees());
        boolean cleanManeuverBeginning = maneuver.isManeuverBeginningClean(previousManeuver);
        boolean cleanManeuverEnd = maneuver.isManeuverEndClean(nextManeuver);
        if (cleanManeuverBeginning && cleanManeuverEnd) {
            if (absCourseChangeDegMainCurve <= 15) {
                // jibe, bear away, head up - hard to distinguish
                double bearAwayLikelihoodBonus = limitProbabilityBonus(computeBearAwayLikelihoodBonus(maneuver) / 4,
                        0.05);
                double headUpLikelihoodBonus = -bearAwayLikelihoodBonus;
                double jibeLikelihoodBonus = limitProbabilityBonus(computeJibeLikelihoodBonus(maneuver), -1,
                        Math.abs(bearAwayLikelihoodBonus) + 0.01);
                likelihoodPerManeuverType[CoarseGrainedManeuverType.JIBE.ordinal()] = 1 + jibeLikelihoodBonus;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.BEAR_AWAY.ordinal()] = 1 + bearAwayLikelihoodBonus;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.HEAD_UP.ordinal()] = 1 + headUpLikelihoodBonus;
            } else if (absCourseChangeDegMainCurve <= 45) {
                // jibe, bear away, head up
                double bearAwayLikelihoodBonus = computeBearAwayLikelihoodBonus(maneuver);
                double headUpLikelihoodBonus = -bearAwayLikelihoodBonus;
                double jibeLikelihoodBonus = computeJibeLikelihoodBonus(maneuver) / 2;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.JIBE.ordinal()] = 1 + jibeLikelihoodBonus;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.BEAR_AWAY.ordinal()] = 1 + bearAwayLikelihoodBonus;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.HEAD_UP.ordinal()] = 1 + headUpLikelihoodBonus;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.TACK.ordinal()] = 0.1;
            } else if (absCourseChangeDegMainCurve <= 120) {
                // tack, jibe, bear away, head up
                double bearAwayLikelihoodBonus = computeBearAwayLikelihoodBonus(maneuver);
                double headUpLikelihoodBonus = -bearAwayLikelihoodBonus;
                double jibeLikelihoodBonus = computeJibeLikelihoodBonus(maneuver);
                double tackLikelihoodBonus = computeTackLikelihoodBonus(maneuver);
                likelihoodPerManeuverType[CoarseGrainedManeuverType.JIBE.ordinal()] = 1 + jibeLikelihoodBonus;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.BEAR_AWAY.ordinal()] = 1 + bearAwayLikelihoodBonus;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.HEAD_UP.ordinal()] = 1 + headUpLikelihoodBonus;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.TACK.ordinal()] = 1 + tackLikelihoodBonus;
            } else if (absCourseChangeDegMainCurve <= 150) {
                // wide tack, jibe, bear away, head up
                double bearAwayLikelihoodBonus = computeBearAwayLikelihoodBonus(maneuver);
                double headUpLikelihoodBonus = -bearAwayLikelihoodBonus;
                double _180LikelihoodBonus = computeTackJibeProbabilityBonus(maneuver)
                        + Math.max(bearAwayLikelihoodBonus, headUpLikelihoodBonus) - 0.01;
                likelihoodPerManeuverType[CoarseGrainedManeuverType._180.ordinal()] = 1 + _180LikelihoodBonus;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.BEAR_AWAY.ordinal()] = 1 + bearAwayLikelihoodBonus;
                likelihoodPerManeuverType[CoarseGrainedManeuverType.HEAD_UP.ordinal()] = 1 + headUpLikelihoodBonus;
            } else if (absCourseChangeDegMainCurve < 315) {
                // It is likely that the maneuver contains one tack or one jibe
                // => course at lowest speed may refer either upwind, or downwind
                likelihoodPerManeuverType[CoarseGrainedManeuverType._180.ordinal()] = 1.0;
            } else if (absCourseChangeDegMainCurve < 400) {
                // It is likely that the maneuver contains one tack and one jibe.
                // => course at lowest speed refers upwind
                likelihoodPerManeuverType[CoarseGrainedManeuverType._360.ordinal()] = 1.0;
                likelihoodPerManeuverType[CoarseGrainedManeuverType._180.ordinal()] = 1
                        / (5 + (absCourseChangeDegMainCurve - 315) / 5);
            } else {
                // It is highly likely that the maneuver contains at least one tack and one jibe.
                // => course at lowest speed refers upwind
                likelihoodPerManeuverType[CoarseGrainedManeuverType._360.ordinal()] = 1.0;
            }
        } else {
            Arrays.fill(likelihoodPerManeuverType, 1);
        }
        return new ManeuverClassificationResult(maneuver, boatClass, likelihoodPerManeuverType, cleanManeuverBeginning,
                cleanManeuverEnd);
    }

    private double computeJibeLikelihoodBonus(CompleteManeuverCurveWithEstimationData maneuver) {
        SpeedWithBearing speedWithBearingBefore = maneuver.getCurveWithUnstableCourseAndSpeed()
                .getSpeedWithBearingBefore();
        SpeedWithBearing speedWithBearingAfter = maneuver.getCurveWithUnstableCourseAndSpeed()
                .getSpeedWithBearingAfter();
        double lowestSpeedEnteringSpeedRatio = maneuver.getMainCurve().getLowestSpeed().getKnots()
                / maneuver.getMainCurve().getSpeedWithBearingBefore().getKnots();
        if (lowestSpeedEnteringSpeedRatio >= 0.98) {
            return -0.5;
        }
        double highestSpeedEnteringSpeedRatio = maneuver.getMainCurve().getHighestSpeed().getKnots()
                / maneuver.getMainCurve().getSpeedWithBearingBefore().getKnots();
        if (highestSpeedEnteringSpeedRatio >= 1.07) {
            return -0.5;
        }
        double enteringExitingSpeedRatio = speedWithBearingBefore.getKnots() / speedWithBearingAfter.getKnots();
        double lowestSpeedExitingSpeedRatio = maneuver.getMainCurve().getLowestSpeed().getKnots()
                / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots();

        double maneuverAngleBonus = 0;
        if (hasPolarsForBoatClass) {
            Pair<Double, SpeedWithBearingWithConfidence<Void>> jibeLikelihoodWithTwaTws = polarService
                    .getManeuverLikelihoodAndTwsTwa(boatClass, speedWithBearingBefore,
                            maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees(),
                            ManeuverType.JIBE);
            if (jibeLikelihoodWithTwaTws.getA() == 0) {
                maneuverAngleBonus = -0.1;
            } else {
                maneuverAngleBonus = limitProbabilityBonus(jibeLikelihoodWithTwaTws.getA() - 0.5, -0.1, 0.5);
            }
        }
        double enteringExitingSpeedRatioBonus = limitProbabilityBonus(
                5 * (0.07 - Math.abs(1 - enteringExitingSpeedRatio)), 0.2);
        double lowestSpeedEnteringSpeedRatioBonus = limitProbabilityBonus(
                Math.abs(0.95 - lowestSpeedEnteringSpeedRatio) * 5, 0.2);
        double lowestSpeedExitingSpeedRatioBonus = limitProbabilityBonus(
                0.4 - Math.abs(1 - lowestSpeedExitingSpeedRatio / lowestSpeedEnteringSpeedRatio) * 5, 0.2);
        double highestSpeedEnteringSpeedRatioPenalty = limitProbabilityBonus(1 - highestSpeedEnteringSpeedRatio * 10,
                -1, 0);
        double jibeLikelihoodBonus = (maneuverAngleBonus + enteringExitingSpeedRatioBonus
                + lowestSpeedEnteringSpeedRatioBonus + lowestSpeedExitingSpeedRatioBonus
                + highestSpeedEnteringSpeedRatioPenalty) / 4;
        return jibeLikelihoodBonus + computeTackJibeProbabilityBonus(maneuver);
    }

    private double computeTackLikelihoodBonus(CompleteManeuverCurveWithEstimationData maneuver) {
        SpeedWithBearing speedWithBearingBefore = maneuver.getCurveWithUnstableCourseAndSpeed()
                .getSpeedWithBearingBefore();
        SpeedWithBearing speedWithBearingAfter = maneuver.getCurveWithUnstableCourseAndSpeed()
                .getSpeedWithBearingAfter();
        double lowestSpeedEnteringSpeedRatio = maneuver.getMainCurve().getLowestSpeed().getKnots()
                / maneuver.getMainCurve().getSpeedWithBearingBefore().getKnots();
        if (lowestSpeedEnteringSpeedRatio >= 0.9) {
            return -0.5;
        }
        double highestSpeedEnteringSpeedRatio = maneuver.getMainCurve().getHighestSpeed().getKnots()
                / maneuver.getMainCurve().getSpeedWithBearingBefore().getKnots();
        if (highestSpeedEnteringSpeedRatio >= 1.02) {
            return -0.5;
        }
        double enteringExitingSpeedRatio = speedWithBearingBefore.getKnots() / speedWithBearingAfter.getKnots();
        double lowestSpeedExitingSpeedRatio = maneuver.getMainCurve().getLowestSpeed().getKnots()
                / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots();

        double maneuverAngleBonus = 0;
        if (hasPolarsForBoatClass) {
            Pair<Double, SpeedWithBearingWithConfidence<Void>> tackLikelihoodWithTwaTws = polarService
                    .getManeuverLikelihoodAndTwsTwa(boatClass, speedWithBearingBefore,
                            maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees(),
                            ManeuverType.TACK);
            if (tackLikelihoodWithTwaTws.getA() == 0) {
                maneuverAngleBonus = -0.1;
            } else {
                maneuverAngleBonus = limitProbabilityBonus(tackLikelihoodWithTwaTws.getA() - 0.5, -0.1, 0.5);
            }
        }
        double enteringExitingSpeedRatioBonus = limitProbabilityBonus(
                5 * (0.07 - Math.abs(1 - enteringExitingSpeedRatio)), 0.2);
        double lowestSpeedEnteringSpeedRatioBonus = limitProbabilityBonus(
                Math.abs(0.85 - lowestSpeedEnteringSpeedRatio) * 5, 0.2);
        double lowestSpeedExitingSpeedRatioBonus = limitProbabilityBonus(
                0.4 - Math.abs(1 - lowestSpeedExitingSpeedRatio / lowestSpeedEnteringSpeedRatio) * 5, 0.2);
        double highestSpeedEnteringSpeedRatioPenalty = limitProbabilityBonus(1 - highestSpeedEnteringSpeedRatio * 10,
                -1, 0);
        double tackLikelihoodBonus = (maneuverAngleBonus + enteringExitingSpeedRatioBonus
                + lowestSpeedEnteringSpeedRatioBonus + lowestSpeedExitingSpeedRatioBonus
                + highestSpeedEnteringSpeedRatioPenalty) / 4;
        return tackLikelihoodBonus + computeTackJibeProbabilityBonus(maneuver);
    }

    private double computeBearAwayLikelihoodBonus(CompleteManeuverCurveWithEstimationData maneuver) {
        double lowestSpeedEnteringSpeedRatio = maneuver.getMainCurve().getLowestSpeed().getKnots()
                / maneuver.getMainCurve().getSpeedWithBearingBefore().getKnots();
        double highestSpeedEnteringSpeedRatio = maneuver.getMainCurve().getHighestSpeed().getKnots()
                / maneuver.getMainCurve().getSpeedWithBearingBefore().getKnots();
        double enteringExitingSpeedRatio = maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore()
                .getKnots() / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots();
        double enteringExitingSpeedRatioBonus = limitProbabilityBonus(0.5 - enteringExitingSpeedRatio / 2, 0.2);
        double highestSpeedEnteringSpeedRatioBonus = limitProbabilityBonus(highestSpeedEnteringSpeedRatio - 1, 0.2);
        double lowestSpeedEnteringSpeedRatioBonus = limitProbabilityBonus(lowestSpeedEnteringSpeedRatio - 1, 0.2);
        double bearAwayLikelihoodBonus = (enteringExitingSpeedRatioBonus + highestSpeedEnteringSpeedRatioBonus
                + lowestSpeedEnteringSpeedRatioBonus) / 3;
        return bearAwayLikelihoodBonus;
    }

    private double computeTackJibeProbabilityBonus(CompleteManeuverCurveWithEstimationData maneuver) {
        Bearing before = maneuver.getRelativeBearingToNextMarkBeforeManeuver();
        Bearing after = maneuver.getRelativeBearingToNextMarkBeforeManeuver();
        double absCourseChangeDeg = Math
                .abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees());
        if (before != null && absCourseChangeDeg >= 30 && absCourseChangeDeg <= 120) {
            if (maneuver.isMarkPassing()) {
                double enteringExitingSpeedRatio = maneuver.getCurveWithUnstableCourseAndSpeed()
                        .getSpeedWithBearingBefore().getKnots()
                        / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots();
                if (Math.abs(1 - enteringExitingSpeedRatio) > 0.15) {
                    return -0.2;
                }
            } else if (after != null) {
                if (Math.signum(before.getDegrees()) != Math.signum(after.getDegrees())
                        || isBearingNearlyZero(before) && !isBearingNearlyZero(after)
                        || !isBearingNearlyZero(before) && isBearingNearlyZero(after)) {
                    return 1;
                }
            }

        }
        return 0;
    }

    private boolean isBearingNearlyZero(Bearing bearing) {
        return Math.abs(bearing.getDegrees()) <= 10;
    }

    private double limitProbabilityBonus(double probabilityBonus, double limit) {
        return limitProbabilityBonus(probabilityBonus, -limit, limit);
    }

    private double limitProbabilityBonus(double probabilityBonus, double lowestLimit, double upperLimit) {
        if (probabilityBonus < lowestLimit) {
            return lowestLimit;
        } else if (probabilityBonus > upperLimit) {
            return upperLimit;
        }
        return probabilityBonus;
    }

}
