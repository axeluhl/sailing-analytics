package com.sap.sailing.windestimation.data.transformer;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverCategory;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

import smile.sort.QuickSelect;

public abstract class AbstractCompleteManeuverCurveWithEstimationDataTransformer<ToType>
        implements CompetitorTrackTransformer<CompleteManeuverCurveWithEstimationData, ToType> {

    protected double getSpeedScalingDivisor(List<CompleteManeuverCurveWithEstimationData> competitorTrackManeuvers) {
        List<Double> speedsInKnotsOfCleanSailingSegments = new ArrayList<>();
        CompleteManeuverCurveWithEstimationData previousManeuver = null;
        CompleteManeuverCurveWithEstimationData maneuver = null;
        for (CompleteManeuverCurveWithEstimationData nextManeuver : competitorTrackManeuvers) {
            if (maneuver != null) {
                // if (maneuver.isManeuverEnteringClean()) {
                if (previousManeuver != null
                        && isSegmentBetweenManeuversEligibleForPolarsCollection(previousManeuver, maneuver)) {
                    speedsInKnotsOfCleanSailingSegments
                            .add(maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots());
                }
                // if (maneuver.isManeuverExitingClean()) {
                if (isSegmentBetweenManeuversEligibleForPolarsCollection(maneuver, nextManeuver)) {
                    speedsInKnotsOfCleanSailingSegments
                            .add(maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots());
                }
            }
            previousManeuver = maneuver;
            maneuver = nextManeuver;
        }
        if (maneuver != null) {
            if (isSegmentBetweenManeuversEligibleForPolarsCollection(previousManeuver, maneuver)) {
                speedsInKnotsOfCleanSailingSegments
                        .add(maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots());
            }
            if (isSegmentBetweenManeuversEligibleForPolarsCollection(maneuver, null)) {
                speedsInKnotsOfCleanSailingSegments
                        .add(maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots());
            }
        }
        double[] speedsArray = new double[speedsInKnotsOfCleanSailingSegments.size()];
        int i = 0;
        for (Double speed : speedsInKnotsOfCleanSailingSegments) {
            speedsArray[i++] = speed;
        }
        if (speedsArray.length > 0) {
            double scalingDivisor = QuickSelect.select(speedsArray, (int) (speedsArray.length * 0.98));
            return scalingDivisor;
        } else {
            return 10;
        }
    }

    protected boolean isSegmentBetweenManeuversEligibleForPolarsCollection(
            CompleteManeuverCurveWithEstimationData fromManeuver, CompleteManeuverCurveWithEstimationData toManeuver) {
        if (fromManeuver == null || toManeuver == null) {
            if (fromManeuver == toManeuver) {
                // both are null
                return false;
            }
            if (fromManeuver == null) {
                return isManeuverBoundariesDataClean(toManeuver, null, null, true, false)
                        && toManeuver.getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingBefore() != null
                        && Math.abs(toManeuver.getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingBefore()
                                .getKnots()
                                - toManeuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore()
                                        .getKnots())
                                / toManeuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore()
                                        .getKnots() < 0.2;
            }
            if (toManeuver == null) {
                return isManeuverBoundariesDataClean(fromManeuver, null, null, false, true)
                        && fromManeuver.getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingAfter() != null
                        && Math.abs(fromManeuver.getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingAfter()
                                .getKnots()
                                - fromManeuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter()
                                        .getKnots())
                                / fromManeuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter()
                                        .getKnots() < 0.2;
            }
        }
        return isManeuverBoundariesDataClean(fromManeuver, null, toManeuver, false, true)
                && Math.abs(toManeuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getBearing()
                        .getDifferenceTo(fromManeuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter()
                                .getBearing())
                        .getDegrees()) <= 10
                && toManeuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots() > 2
                && Math.abs(fromManeuver.getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingAfter()
                        .getKnots()
                        - fromManeuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots())
                        / fromManeuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots() < 0.2;
    }

    protected boolean isManeuverBoundariesDataClean(CompleteManeuverCurveWithEstimationData maneuver,
            CompleteManeuverCurveWithEstimationData previousManeuver,
            CompleteManeuverCurveWithEstimationData nextManeuver, boolean validateManeuverEntering,
            boolean validateManeuverExiting) {
        return maneuver.getCurveWithUnstableCourseAndSpeed().getLongestIntervalBetweenTwoFixes().asSeconds() < 4
                && (!validateManeuverEntering || maneuver.getCurveWithUnstableCourseAndSpeed()
                        .getIntervalBetweenFirstFixOfCurveAndPreviousFix().asSeconds() <= 4)
                && (!validateManeuverExiting || maneuver.getCurveWithUnstableCourseAndSpeed()
                        .getIntervalBetweenLastFixOfCurveAndNextFix().asSeconds() <= 4)
                && (!validateManeuverEntering || maneuver.getCurveWithUnstableCourseAndSpeed()
                        .getGpsFixesCountFromPreviousManeuverEndToManeuverStart()
                        / maneuver.getCurveWithUnstableCourseAndSpeed()
                                .getDurationFromPreviousManeuverEndToManeuverStart().asSeconds() <= 8)
                && (!validateManeuverExiting || maneuver.getCurveWithUnstableCourseAndSpeed()
                        .getGpsFixesCountFromManeuverEndToNextManeuverStart()
                        / maneuver.getCurveWithUnstableCourseAndSpeed().getDurationFromManeuverEndToNextManeuverStart()
                                .asSeconds() <= 8)
                && (!validateManeuverEntering
                        || maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots() > 2)
                && (!validateManeuverExiting
                        || maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots() > 2)
                && (!validateManeuverEntering
                        || maneuver.getCurveWithUnstableCourseAndSpeed()
                                .getDurationFromPreviousManeuverEndToManeuverStart().asSeconds() >= 4
                        || previousManeuver != null
                                && Math.abs(previousManeuver.getCurveWithUnstableCourseAndSpeed()
                                        .getDirectionChangeInDegrees()) < Math.abs(maneuver
                                                .getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees())
                                                * 0.3)
                && (!validateManeuverExiting
                        || maneuver.getCurveWithUnstableCourseAndSpeed().getDurationFromManeuverEndToNextManeuverStart()
                                .asSeconds() >= 4
                        || nextManeuver != null
                                && Math.abs(nextManeuver.getCurveWithUnstableCourseAndSpeed()
                                        .getDirectionChangeInDegrees()) < Math.abs(maneuver
                                                .getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees())
                                                * 0.3);
    }

    protected boolean isManeuverClean(CompleteManeuverCurveWithEstimationData maneuver,
            CompleteManeuverCurveWithEstimationData previousManeuver,
            CompleteManeuverCurveWithEstimationData nextManeuver) {
        return isManeuverBoundariesDataClean(maneuver, previousManeuver, nextManeuver, true, true)
                && Math.abs(maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots()
                        - maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots())
                        * 3 < Math.min(
                                maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots(),
                                maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots())
                && Math.abs(maneuver.getMainCurve().getDirectionChangeInDegrees()
                        - maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees()) < Math.min(
                                Math.abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees())
                                        / 2.0,
                                40);
    }

    protected ManeuverTypeForClassification getManeuverTypeForClassification(
            CompleteManeuverCurveWithEstimationData maneuver) {
        ManeuverType maneuverType = maneuver.getManeuverTypeForCompleteManeuverCurve();
        switch (maneuverType) {
        case BEAR_AWAY:
            return ManeuverTypeForClassification.BEAR_AWAY;
        case HEAD_UP:
            return ManeuverTypeForClassification.HEAD_UP;
        case PENALTY_CIRCLE:
            return ManeuverTypeForClassification._360;
        case UNKNOWN:
            return null;
        case JIBE:
            return Math.abs(maneuver.getMainCurve().getDirectionChangeInDegrees()) > 120
                    ? ManeuverTypeForClassification._180_JIBE : ManeuverTypeForClassification.JIBE;
        case TACK:
            return Math.abs(maneuver.getMainCurve().getDirectionChangeInDegrees()) > 120
                    ? ManeuverTypeForClassification._180_TACK : ManeuverTypeForClassification.TACK;
        }
        throw new IllegalStateException();
    }

    protected ManeuverCategory getManeuverCategory(CompleteManeuverCurveWithEstimationData maneuver) {
        double absCourseChangeInDegrees = Math.abs(maneuver.getMainCurve().getDirectionChangeInDegrees());
        if (absCourseChangeInDegrees < 30) {
            return ManeuverCategory.SMALL;
        }
        if (absCourseChangeInDegrees <= 120) {
            return maneuver.isMarkPassing() ? ManeuverCategory.MARK_PASSING : ManeuverCategory.REGULAR;
        }
        if (absCourseChangeInDegrees <= 150) {
            return ManeuverCategory.WIDE;
        }
        if (absCourseChangeInDegrees <= 310) {
            return ManeuverCategory._180;
        }
        return ManeuverCategory._360;
    }

}
