package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.BearingChangeAnalyzer;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverNodesLevel<SelfType extends ManeuverNodesLevel<SelfType>> {

    SelfType getNextLevel();

    SelfType getPreviousLevel();

    CompleteManeuverCurveWithEstimationData getManeuver();

    void computeProbabilitiesFromPreviousLevelToThisLevel();

    void appendNextManeuverNodesLevel(SelfType nextManeuverNodesLevel);

    double getProbabilityFromPreviousLevelNodeToThisLevelNode(FineGrainedPointOfSail previousLevelNode,
            FineGrainedPointOfSail thisLevelNode);

    Bearing getCourse();

    default double getWindCourseInDegrees(FineGrainedPointOfSail node) {
        return getWindCourseInDegrees(node.getTwa());
    }

    double getWindCourseInDegrees(double twa);

    BoatClass getBoatClass();

    default FineGrainedPointOfSail getPointOfSailBeforeManeuver(FineGrainedPointOfSail pointOfSailAfterManeuver) {
        return pointOfSailAfterManeuver.getNextPointOfSail(
                getManeuver().getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees() * -1);
    }

    default FineGrainedManeuverType getTypeOfCleanManeuver(FineGrainedPointOfSail pointOfSailAfterManeuver) {
        ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData maneuverBoundaries = getManeuver()
                .getCurveWithUnstableCourseAndSpeed();
        Bearing windCourse = new DegreeBearingImpl(getWindCourseInDegrees(pointOfSailAfterManeuver));
        BearingChangeAnalyzer bearingChangeAnalyzer = BearingChangeAnalyzer.INSTANCE;
        double directionChangeInDegrees = maneuverBoundaries.getDirectionChangeInDegrees();
        int numberOfTacks = bearingChangeAnalyzer.didPass(maneuverBoundaries.getSpeedWithBearingBefore().getBearing(),
                directionChangeInDegrees, maneuverBoundaries.getSpeedWithBearingAfter().getBearing(),
                windCourse.reverse());
        int numberOfJibes = bearingChangeAnalyzer.didPass(maneuverBoundaries.getSpeedWithBearingBefore().getBearing(),
                directionChangeInDegrees, maneuverBoundaries.getSpeedWithBearingAfter().getBearing(), windCourse);
        if (numberOfTacks > 0 && numberOfJibes > 0) {
            return FineGrainedManeuverType._360;
        }
        if (numberOfTacks > 0 || numberOfJibes > 0) {
            if (Math.abs(directionChangeInDegrees) > 120) {
                return numberOfTacks > 1 ? FineGrainedManeuverType._180_TACK : FineGrainedManeuverType._180_JIBE;
            }
            return numberOfTacks > 0 ? FineGrainedManeuverType.TACK : FineGrainedManeuverType.JIBE;
        }
        boolean bearAway = pointOfSailAfterManeuver.getTack() == Tack.STARBOARD && directionChangeInDegrees > 0
                || pointOfSailAfterManeuver.getTack() == Tack.PORT && directionChangeInDegrees < 0;
        LegType legTypeBeforeManeuver = getPointOfSailBeforeManeuver(pointOfSailAfterManeuver).getLegType();
        LegType legTypeAfterManeuver = pointOfSailAfterManeuver.getLegType();
        if (bearAway) {
            switch (legTypeBeforeManeuver) {
            case UPWIND:
                switch (legTypeAfterManeuver) {
                case UPWIND:
                    return FineGrainedManeuverType.BEAR_AWAY_AT_UPWIND;
                case REACHING:
                    return FineGrainedManeuverType.BEAR_AWAY_FROM_UPWIND_UNTIL_REACHING;
                case DOWNWIND:
                    return FineGrainedManeuverType.BEAR_AWAY_FROM_UPWIND_UNTIL_DOWNWIND;
                }
            case REACHING:
                return legTypeAfterManeuver == LegType.DOWNWIND
                        ? FineGrainedManeuverType.BEAR_AWAY_FROM_REACHING_UNTIL_DOWNWIND
                        : FineGrainedManeuverType.BEAR_AWAY_AT_REACHING;
            case DOWNWIND:
                return FineGrainedManeuverType.BEAR_AWAY_AT_DOWNWIND;
            }
        } else {
            switch (legTypeBeforeManeuver) {
            case DOWNWIND:
                switch (legTypeAfterManeuver) {
                case DOWNWIND:
                    return FineGrainedManeuverType.HEAD_UP_AT_DOWNWIND;
                case REACHING:
                    return FineGrainedManeuverType.HEAD_UP_FROM_DOWNWIND_UNTIL_REACHING;
                case UPWIND:
                    return FineGrainedManeuverType.HEAD_UP_FROM_DOWNWIND_UNTIL_UPWIND;
                }
            case REACHING:
                return legTypeAfterManeuver == LegType.UPWIND
                        ? FineGrainedManeuverType.HEAD_UP_FROM_REACHING_UNTIL_UPWIND
                        : FineGrainedManeuverType.HEAD_UP_AT_REACHING;
            case UPWIND:
                return FineGrainedManeuverType.HEAD_UP_AT_UPWIND;
            }
        }
        throw new IllegalStateException();
    }

    default boolean isCleanManeuver(FineGrainedPointOfSail pointOfSailAfterPreviousManeuver,
            FineGrainedPointOfSail pointOfSailAfterCurrentManeuver) {
        FineGrainedPointOfSail targetPointOfSailAfterPreviousManeuver = getPointOfSailBeforeManeuver(
                pointOfSailAfterCurrentManeuver);
        if (targetPointOfSailAfterPreviousManeuver != pointOfSailAfterPreviousManeuver) {
            return false;
        }
        boolean maneuverBeginningClean = isManeuverBeginningClean();
        boolean maneuverEndClean = isManeuverEndClean();
        if (getManeuver().isMarkPassing() || !maneuverBeginningClean || !maneuverEndClean) {
            return false;
        }
        return true;
    }

    default boolean isManeuverEndClean() {
        CompleteManeuverCurveWithEstimationData maneuver = getManeuver();
        CompleteManeuverCurveWithEstimationData nextManeuver = getNextLevel() == null ? null
                : getNextLevel().getManeuver();
        ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData curveWithUnstableCourseAndSpeed = maneuver
                .getCurveWithUnstableCourseAndSpeed();
        double secondsToNextManeuver = curveWithUnstableCourseAndSpeed.getDurationFromManeuverEndToNextManeuverStart()
                .asSeconds();
        if (curveWithUnstableCourseAndSpeed.getSpeedWithBearingBefore().getKnots() > 1
                && curveWithUnstableCourseAndSpeed.getSpeedWithBearingAfter().getKnots() > 1
                && Math.abs(curveWithUnstableCourseAndSpeed.getDirectionChangeInDegrees()
                        - maneuver.getMainCurve().getDirectionChangeInDegrees()) < 30
                && (secondsToNextManeuver >= 4
                        && maneuver.getCurveWithUnstableCourseAndSpeed().getIntervalBetweenLastFixOfCurveAndNextFix()
                                .asSeconds() < 8
                        || nextManeuver != null
                                && Math.abs(nextManeuver.getMainCurve().getDirectionChangeInDegrees()) < Math
                                        .abs(maneuver.getMainCurve().getDirectionChangeInDegrees()) * 0.3)) {
            return true;
        }
        return false;
    }

    default boolean isManeuverBeginningClean() {
        CompleteManeuverCurveWithEstimationData maneuver = getManeuver();
        CompleteManeuverCurveWithEstimationData previousManeuver = getPreviousLevel() == null ? null
                : getPreviousLevel().getManeuver();
        double secondsToPreviousManeuver = maneuver.getCurveWithUnstableCourseAndSpeed()
                .getDurationFromPreviousManeuverEndToManeuverStart().asSeconds();
        if (maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots() > 1
                && (secondsToPreviousManeuver >= 4
                        && maneuver.getCurveWithUnstableCourseAndSpeed()
                                .getIntervalBetweenFirstFixOfCurveAndPreviousFix().asSeconds() < 8
                        || previousManeuver != null
                                && Math.abs(previousManeuver.getMainCurve().getDirectionChangeInDegrees()) < Math
                                        .abs(maneuver.getMainCurve().getDirectionChangeInDegrees()) * 0.3)) {
            return true;
        }
        return false;
    }

}
