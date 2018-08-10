package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.BearingChangeAnalyzer;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.impl.DegreeBearingImpl;

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

    Bearing getCourseAfter();

    Bearing getCourseBefore();

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

    boolean isManeuverClean();

    CompleteManeuverCurveWithEstimationData getPreviousManeuverOfSameTrack();

    CompleteManeuverCurveWithEstimationData getNextManeuverOfSameTrack();

    void setTackProbabilityBonusToManeuver(double tackProbabilityBonus);

    boolean isCalculationOfTransitionProbabilitiesNeeded();

}
