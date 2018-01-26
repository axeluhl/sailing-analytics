package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.Util.Pair;

public class SingleManeuverClassifier {

    private final BoatClass boatClass;
    private final PolarDataService polarService;
    private final IManeuverSpeedRetriever maneuverSpeedRetriever;

    public SingleManeuverClassifier(BoatClass boatClass, PolarDataService polarService,
            IManeuverSpeedRetriever maneuverSpeedRetriever) {
        this.boatClass = boatClass;
        this.polarService = polarService;
        this.maneuverSpeedRetriever = maneuverSpeedRetriever;
    }

    public SingleManeuverClassificationResult computeClassificationResult(Maneuver maneuver) {
        SpeedWithBearing lowestSpeedWithinManeuverMainCurve = maneuverSpeedRetriever
                .getLowestSpeedWithinManeuverMainCurve(maneuver);
        double lowestSpeedWithBeginningSpeedRatio = maneuverSpeedRetriever
                .getLowestSpeedWithinManeuverMainCurve(maneuver).getKnots()
                / maneuver.getMainCurveBoundaries().getSpeedWithBearingBefore().getKnots();
        double courseChangeDegUntilLowestSpeed = maneuver.getMainCurveBoundaries().getSpeedWithBearingBefore()
                .getBearing().getDifferenceTo(lowestSpeedWithinManeuverMainCurve.getBearing()).getDegrees();
        if (courseChangeDegUntilLowestSpeed * maneuver.getMainCurveBoundaries().getDirectionChangeInDegrees() < 0) {
            courseChangeDegUntilLowestSpeed = courseChangeDegUntilLowestSpeed < 0
                    ? courseChangeDegUntilLowestSpeed + 360 : courseChangeDegUntilLowestSpeed - 360;
        }
        double highestSpeedWithBeginningSpeedRatio = maneuverSpeedRetriever
                .getHighestSpeedWithinManeuverMainCurve(maneuver).getKnots()
                / maneuver.getMainCurveBoundaries().getSpeedWithBearingBefore().getKnots();
        double enteringExitingSpeedRatio = maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                .getSpeedWithBearingBefore().getKnots()
                / maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries().getSpeedWithBearingAfter().getKnots();
        double courseChangeDeg = maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                .getDirectionChangeInDegrees();
        double[] presumedManeuverTypeLikelihoodsByAngleAnalysis = new double[PresumedManeuverType.values().length];
        double[] presumedManeuverTypeLikelihoodsBySpeedAnalysis = new double[presumedManeuverTypeLikelihoodsByAngleAnalysis.length];
        double presumedTwsInKnotsIfTack = -1;
        double presumedTwsInKnotsIfJibe = -1;
        double absCourseChangeDeg = Math.abs(courseChangeDeg);
        if (Math.abs(maneuver.getMainCurveBoundaries().getDirectionChangeInDegrees()) <= 45) {
            // jibe, bear away, head up
            Pair<Double, SpeedWithBearingWithConfidence<Void>> jibeLikelihoodWithTwaTws = polarService
                    .getManeuverLikelihoodAndTwsTwa(boatClass,
                            maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries().getSpeedWithBearingBefore(),
                            courseChangeDeg, ManeuverType.JIBE);
            if (jibeLikelihoodWithTwaTws.getA() == 0) {
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.HEAD_UP_BEAR_AWAY.ordinal()] = 1.0;
            } else {
                double jibeLikelihoodBonus = jibeLikelihoodWithTwaTws.getA() - 0.5;
                if (jibeLikelihoodBonus < -0.2) {
                    jibeLikelihoodBonus = -0.2;
                } else if (jibeLikelihoodBonus > 0.4) {
                    jibeLikelihoodBonus = 0.4;
                }
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.HEAD_UP_BEAR_AWAY.ordinal()] = 0.5
                        - jibeLikelihoodBonus;
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.JIBE.ordinal()] = 0.5
                        + jibeLikelihoodBonus;
                presumedTwsInKnotsIfJibe = jibeLikelihoodWithTwaTws.getB().getObject().getKnots();
            }
        } else if (absCourseChangeDeg <= 110) {
            // tack, jibe, mark passings, head up, bear away
            SpeedWithBearing speedWithBearingBefore = maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                    .getSpeedWithBearingBefore();
            Pair<Double, SpeedWithBearingWithConfidence<Void>> tackLikelihoodWithTwaTws = polarService
                    .getManeuverLikelihoodAndTwsTwa(boatClass, speedWithBearingBefore, courseChangeDeg,
                            ManeuverType.TACK);
            Pair<Double, SpeedWithBearingWithConfidence<Void>> jibeLikelihoodWithTwaTws = polarService
                    .getManeuverLikelihoodAndTwsTwa(boatClass, speedWithBearingBefore, courseChangeDeg,
                            ManeuverType.JIBE);

            double markPassingLikelihoodBonus = (Math
                    .abs(maneuver.getMainCurveBoundaries().getDirectionChangeInDegrees()) - 70) / 100;
            if (markPassingLikelihoodBonus < -0.4) {
                markPassingLikelihoodBonus = -0.4;
            } else if (markPassingLikelihoodBonus > 0.4) {
                markPassingLikelihoodBonus = 0.4;
            }

            double markPassingLikelihood = 0.5 + markPassingLikelihoodBonus;
            double headUpBearAwayLikelihood = 1 - markPassingLikelihood;

            double tackLikelihood = tackLikelihoodWithTwaTws.getA();
            double jibeLikelihood = jibeLikelihoodWithTwaTws.getA();
            double likelihoodSum = markPassingLikelihood * 2 + headUpBearAwayLikelihood + tackLikelihood
                    + jibeLikelihood;

            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.HEAD_UP_BEAR_AWAY
                    .ordinal()] = headUpBearAwayLikelihood / likelihoodSum;
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.TACK.ordinal()] = tackLikelihood
                    / likelihoodSum;
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.JIBE.ordinal()] = jibeLikelihood
                    / likelihoodSum;
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.MARK_PASSING_LEE
                    .ordinal()] = markPassingLikelihood / likelihoodSum;
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.MARK_PASSING_LUV
                    .ordinal()] = markPassingLikelihood / likelihoodSum;

            double markPassingLuvLikelihoodBonus = (highestSpeedWithBeginningSpeedRatio - 1.01) * 5;
            double markPassingLeeLikelihoodBonus = 0;
            if (markPassingLuvLikelihoodBonus <= 0) {
                if (markPassingLuvLikelihoodBonus < -0.2) {
                    markPassingLuvLikelihoodBonus = -0.2;
                }
                markPassingLeeLikelihoodBonus = (0.95 - Math.abs(enteringExitingSpeedRatio)) * 2;
                if (markPassingLeeLikelihoodBonus < -0.1) {
                    markPassingLeeLikelihoodBonus = -0.1;
                } else if (markPassingLeeLikelihoodBonus > 0.1) {
                    markPassingLeeLikelihoodBonus = 0.1;
                }
            } else if (markPassingLuvLikelihoodBonus > 0.3) {
                markPassingLuvLikelihoodBonus = 0.3;
            }

            tackLikelihood = 0.5 + -markPassingLuvLikelihoodBonus - markPassingLeeLikelihoodBonus;
            jibeLikelihood = tackLikelihood;
            double markPassingLeeLikelihood = 0.5 + markPassingLeeLikelihoodBonus - markPassingLuvLikelihoodBonus;
            double markPassingLuvLikelihood = 0.5 + markPassingLuvLikelihoodBonus - markPassingLeeLikelihoodBonus;
            headUpBearAwayLikelihood = 0.1 + Math.max(markPassingLuvLikelihood, markPassingLeeLikelihood);
            likelihoodSum = tackLikelihood + jibeLikelihood + markPassingLeeLikelihood + markPassingLeeLikelihood + headUpBearAwayLikelihood;

            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.HEAD_UP_BEAR_AWAY.ordinal()] = headUpBearAwayLikelihood
                    / likelihoodSum;
            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.TACK.ordinal()] = tackLikelihood
                    / likelihoodSum;
            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.JIBE.ordinal()] = jibeLikelihood
                    / likelihoodSum;
            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.MARK_PASSING_LEE
                    .ordinal()] = markPassingLeeLikelihood / likelihoodSum;
            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.MARK_PASSING_LUV
                    .ordinal()] = markPassingLuvLikelihood / likelihoodSum;

        } else if (absCourseChangeDeg <= 140) {
            // mark passing, or wider jibe, wider tack
            double markPassingLuvLikelihoodBonus = (highestSpeedWithBeginningSpeedRatio - 1.04) * 5;
            double markPassingLeeLikelihoodBonus = 0;
            if (markPassingLuvLikelihoodBonus <= 0) {
                if (markPassingLuvLikelihoodBonus < -0.2) {
                    markPassingLuvLikelihoodBonus = -0.2;
                }
                markPassingLeeLikelihoodBonus = (0.95 - Math.abs(enteringExitingSpeedRatio)) * 2;
                if (markPassingLeeLikelihoodBonus < -0.1) {
                    markPassingLeeLikelihoodBonus = -0.1;
                } else if (markPassingLeeLikelihoodBonus > 0.1) {
                    markPassingLeeLikelihoodBonus = 0.1;
                }
            } else if (markPassingLuvLikelihoodBonus > 0.3) {
                markPassingLuvLikelihoodBonus = 0.3;
            }
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.TACK.ordinal()] = 0.25;
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.JIBE.ordinal()] = 0.25;
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.MARK_PASSING_LEE.ordinal()] = 0.25;
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.MARK_PASSING_LUV.ordinal()] = 0.25;

            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.TACK.ordinal()] = 0.25
                    - markPassingLeeLikelihoodBonus / 3 - markPassingLuvLikelihoodBonus / 3;
            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.JIBE.ordinal()] = 0.25
                    - markPassingLeeLikelihoodBonus / 3 - markPassingLuvLikelihoodBonus / 3;
            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.MARK_PASSING_LEE.ordinal()] = 0.25
                    + markPassingLeeLikelihoodBonus - markPassingLuvLikelihoodBonus / 3;
            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.MARK_PASSING_LUV.ordinal()] = 0.25
                    - markPassingLeeLikelihoodBonus / 3 + markPassingLuvLikelihoodBonus;
        } else if (absCourseChangeDeg <= 310) {
            // 180
            // => course at lowest speed may refer either upwind, or downwind
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType._180.ordinal()] = 1.0;
        } else {
            // the maneuver is a >= 360
            // => course at lowest speed refers upwind
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType._360.ordinal()] = 1.0;
        }
        return new SingleManeuverClassificationResult(lowestSpeedWithBeginningSpeedRatio,
                courseChangeDegUntilLowestSpeed, highestSpeedWithBeginningSpeedRatio, enteringExitingSpeedRatio,
                courseChangeDeg, presumedManeuverTypeLikelihoodsByAngleAnalysis,
                presumedManeuverTypeLikelihoodsBySpeedAnalysis, presumedTwsInKnotsIfTack, presumedTwsInKnotsIfJibe);
    }

}
