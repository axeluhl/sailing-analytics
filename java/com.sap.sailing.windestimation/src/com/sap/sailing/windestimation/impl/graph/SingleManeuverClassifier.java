package com.sap.sailing.windestimation.impl.graph;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.Util.Pair;

public class SingleManeuverClassifier {

    private static final double LOWEST_SPEED_MEAN_RATIO_BETWEEN_JIBE_AND_TACK = 0.8;

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
        double lowestSpeedWithBeginningSpeedRatio = maneuverSpeedRetriever
                .getLowestSpeedWithinManeuverMainCurve(maneuver).getKnots()
                / maneuver.getMainCurveBoundaries().getSpeedWithBearingBefore().getKnots();
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
        double[] presumedTrueWindCourseInDeg = new double[presumedManeuverTypeLikelihoodsByAngleAnalysis.length];
        double presumedTwsInKnotsIfTack = -1;
        double presumedTwsInKnotsIfJibe = -1;
        double absCourseChangeDeg = Math.abs(courseChangeDeg);
        if (absCourseChangeDeg > 120) {
            // not ordinary maneuver
            if (courseChangeDeg <= 180 && lowestSpeedWithBeginningSpeedRatio > 0.95) {
                // no jibe or tack within maneuver
                double otherLikelihookBonus = (130 - absCourseChangeDeg) / 50;
                if (otherLikelihookBonus < -0.2) {
                    otherLikelihookBonus = -0.2;
                }
                if (otherLikelihookBonus > 0.2) {
                    otherLikelihookBonus = 0.2;
                }
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.OTHER.ordinal()] = 0.3
                        + otherLikelihookBonus;
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType._180_WITH_JIBE.ordinal()] = 0.3
                        - otherLikelihookBonus / 2;
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType._180_WITH_TACK.ordinal()] = 0.3
                        - otherLikelihookBonus / 2;

                presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.OTHER.ordinal()] = 0.5;
                presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType._180_WITH_JIBE.ordinal()] = 0.4;
                presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType._180_WITH_TACK.ordinal()] = 0.1;

            } else if (courseChangeDeg < 350) {
                double penaltyCircleLikelihookBonus = (absCourseChangeDeg - 360) / 100;
                if (penaltyCircleLikelihookBonus < -0.2) {
                    penaltyCircleLikelihookBonus = -0.2;
                }
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.OTHER.ordinal()] = 0.25
                        - penaltyCircleLikelihookBonus / 3;
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType._180_WITH_JIBE.ordinal()] = 0.25
                        - penaltyCircleLikelihookBonus / 3;
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType._180_WITH_TACK.ordinal()] = 0.25
                        - penaltyCircleLikelihookBonus / 3;
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType._360.ordinal()] = 0.25
                        + penaltyCircleLikelihookBonus;

                // lowest speed can either within jibing or tacking
                double lowestSpeedRefersUpwindLikelihood = 0.5
                        + (LOWEST_SPEED_MEAN_RATIO_BETWEEN_JIBE_AND_TACK - lowestSpeedWithBeginningSpeedRatio) * 2;
                if (lowestSpeedRefersUpwindLikelihood < 0.3) {
                    lowestSpeedRefersUpwindLikelihood = 0.3;
                } else if (lowestSpeedRefersUpwindLikelihood > 0.7) {
                    lowestSpeedRefersUpwindLikelihood = 0.7;
                }
                presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType._180_WITH_TACK
                        .ordinal()] = lowestSpeedRefersUpwindLikelihood;
                presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType._180_WITH_JIBE.ordinal()] = 1.0
                        - lowestSpeedRefersUpwindLikelihood;
            } else {
                // the maneuver is a penalty circle
                // => course at lowest speed refers upwind
                presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType._360.ordinal()] = 1.0;
            }
        } else {
            SpeedWithBearing speedWithBearingBefore = maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries()
                    .getSpeedWithBearingBefore();
            Pair<Double, SpeedWithBearingWithConfidence<Void>> tackLikelihookWithTwaTws = polarService
                    .getManeuverLikelihoodAndTwsTwa(boatClass, speedWithBearingBefore, courseChangeDeg,
                            ManeuverType.TACK);
            Pair<Double, SpeedWithBearingWithConfidence<Void>> jibeLikelihookWithTwaTws = polarService
                    .getManeuverLikelihoodAndTwsTwa(boatClass, speedWithBearingBefore, courseChangeDeg,
                            ManeuverType.JIBE);
            double normalizedTackLikelihook = tackLikelihookWithTwaTws.getA()
                    / (tackLikelihookWithTwaTws.getA() + jibeLikelihookWithTwaTws.getA()) - 0.1;
            double normalizedJibeLikelihook = 1 - normalizedTackLikelihook - 0.1;
            double normalizedOtherLikelihook = 0.2;
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.TACK
                    .ordinal()] = normalizedTackLikelihook;
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.JIBE
                    .ordinal()] = normalizedJibeLikelihook;
            presumedManeuverTypeLikelihoodsByAngleAnalysis[PresumedManeuverType.OTHER
                    .ordinal()] = normalizedOtherLikelihook;
            presumedTwsInKnotsIfTack = tackLikelihookWithTwaTws.getB().getObject().getKnots();
            presumedTwsInKnotsIfJibe = jibeLikelihookWithTwaTws.getB().getObject().getKnots();

            double otherLikelihookBonus = (highestSpeedWithBeginningSpeedRatio - 1.04) * 5;
            if (otherLikelihookBonus <= 0) {
                otherLikelihookBonus = (0.95 - Math.abs(enteringExitingSpeedRatio)) * 2;
                if (otherLikelihookBonus < -0.2) {
                    otherLikelihookBonus = -0.2;
                } else if (otherLikelihookBonus > 0.1) {
                    otherLikelihookBonus = 0.1;
                }
            } else if (otherLikelihookBonus > 0.4) {
                otherLikelihookBonus = 0.4;
            }

            double lowestSpeedRefersUpwindLikelihood = 0.5
                    + (LOWEST_SPEED_MEAN_RATIO_BETWEEN_JIBE_AND_TACK - lowestSpeedWithBeginningSpeedRatio) * 2;
            if (lowestSpeedRefersUpwindLikelihood < 0.4) {
                lowestSpeedRefersUpwindLikelihood = 0.4;
            } else if (lowestSpeedRefersUpwindLikelihood > 0.6) {
                lowestSpeedRefersUpwindLikelihood = 0.6;
            }

            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.TACK
                    .ordinal()] = lowestSpeedRefersUpwindLikelihood - 0.15 - otherLikelihookBonus / 2;
            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.JIBE.ordinal()] = 1
                    - lowestSpeedRefersUpwindLikelihood - 0.15 - otherLikelihookBonus / 2;
            presumedManeuverTypeLikelihoodsBySpeedAnalysis[PresumedManeuverType.OTHER.ordinal()] = 0.3
                    + otherLikelihookBonus;

            // TODO calculate with course before maneuver
            presumedTrueWindCourseInDeg[PresumedManeuverType.TACK.ordinal()] = tackLikelihookWithTwaTws.getB()
                    .getObject().getBearing().getDegrees();
            presumedTrueWindCourseInDeg[PresumedManeuverType.JIBE.ordinal()] = jibeLikelihookWithTwaTws.getB()
                    .getObject().getBearing().getDegrees();
        }
        return new SingleManeuverClassificationResult(lowestSpeedWithBeginningSpeedRatio,
                highestSpeedWithBeginningSpeedRatio, enteringExitingSpeedRatio, courseChangeDeg,
                presumedManeuverTypeLikelihoodsByAngleAnalysis, presumedManeuverTypeLikelihoodsBySpeedAnalysis,
                presumedTrueWindCourseInDeg, presumedTwsInKnotsIfTack, presumedTwsInKnotsIfJibe);
    }

}
