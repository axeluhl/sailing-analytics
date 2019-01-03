package com.sap.sailing.windestimation.aggregator.hmm;

import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange.CombinationModeOnViolation;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class IntersectedWindRangeBasedTransitionProbabilitiesCalculator
        implements GraphNodeTransitionProbabilitiesCalculator {

    protected static final int MIN_BEATING_ANGLE_PLUS_MIN_RUNNING_ANGLE = 40;
    private static final double MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES = 40;

    @Override
    public Pair<IntersectedWindRange, Double> mergeWindRangeAndGetTransitionProbability(GraphNode previousNode,
            GraphLevelBase previousLevel, IntersectedWindRange previousNodeIntersectedWindRange, GraphNode currentNode,
            GraphLevelBase currentLevel) {
        double transitionProbabilitySum = 0;
        double transitionProbabilityUntilCurrentNode = -1;
        IntersectedWindRange intersectedWindRangeUntilCurrentNode = null;
        for (GraphNode node : currentLevel.getLevelNodes()) {
            WindCourseRange previousWindCourseRange = previousNode
                    .getManeuverType() == ManeuverTypeForClassification.BEAR_AWAY
                    || previousNode.getManeuverType() == ManeuverTypeForClassification.HEAD_UP
                            ? previousNodeIntersectedWindRange
                            : previousNode.getValidWindRange();
            IntersectedWindRange intersectedWindRange = previousWindCourseRange.intersect(node.getValidWindRange(),
                    CombinationModeOnViolation.INTERSECTION);
            double transitionProbability = getPenaltyFactorForTransition(intersectedWindRange);
            transitionProbabilitySum += transitionProbability;
            if (node == currentNode) {
                transitionProbabilityUntilCurrentNode = transitionProbability;
                intersectedWindRangeUntilCurrentNode = intersectedWindRange;
            }
        }
        if (transitionProbabilityUntilCurrentNode < 0) {
            throw new IllegalArgumentException("currentNode not contained in currentLevel");
        }
        double normalizedTransitionProbabilityUntilCurrentNode = transitionProbabilityUntilCurrentNode
                / transitionProbabilitySum;
        return new Pair<>(intersectedWindRangeUntilCurrentNode, normalizedTransitionProbabilityUntilCurrentNode);
    }

    protected double getPenaltyFactorForTransition(IntersectedWindRange intersectedWindRange) {
        double violationRange = intersectedWindRange.getViolationRange();
        double penaltyFactor;
        if (violationRange == 0) {
            penaltyFactor = 1.0;
        } else {
            if (violationRange <= MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES) {
                penaltyFactor = 1 / (1 + Math.pow(violationRange
                        / MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES * 2, 2));
            } else {
                penaltyFactor = 1 / (1 + (Math.pow(violationRange, 2)));
            }
        }
        assert (penaltyFactor > 0.0001);
        return penaltyFactor;
    }

    public WindCourseRange getWindCourseRangeForManeuverType(ManeuverForEstimation maneuver,
            ManeuverTypeForClassification maneuverType) {
        switch (maneuverType) {
        case TACK:
            return getTackWindRange(maneuver);
        case JIBE:
            return getJibeWindRange(maneuver);
        case HEAD_UP:
            return getHeadUpWindRange(maneuver);
        case BEAR_AWAY:
            return getBearAwayWindRange(maneuver);
        default:
            throw new IllegalArgumentException();
        }
    }

    protected WindCourseRange getBearAwayWindRange(ManeuverForEstimation maneuver) {
        Bearing invertedCourseBefore = maneuver.getSpeedWithBearingBefore().getBearing().reverse();
        double angleTowardStarboard = invertedCourseBefore
                .getDifferenceTo(maneuver.getSpeedWithBearingAfter().getBearing()).abs().getDegrees();
        angleTowardStarboard -= MIN_BEATING_ANGLE_PLUS_MIN_RUNNING_ANGLE;
        assert (angleTowardStarboard > 0);
        Bearing from;
        if (maneuver.getCourseChangeInDegrees() < 0) {
            from = invertedCourseBefore.add(new DegreeBearingImpl(MIN_BEATING_ANGLE_PLUS_MIN_RUNNING_ANGLE));
        } else {
            from = maneuver.getSpeedWithBearingAfter().getBearing();
        }
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), angleTowardStarboard);
        return windRange;
    }

    protected WindCourseRange getHeadUpWindRange(ManeuverForEstimation maneuver) {
        Bearing invertedCourseAfter = maneuver.getSpeedWithBearingAfter().getBearing().reverse();
        double angleTowardStarboard = invertedCourseAfter
                .getDifferenceTo(maneuver.getSpeedWithBearingBefore().getBearing()).abs().getDegrees();
        angleTowardStarboard -= MIN_BEATING_ANGLE_PLUS_MIN_RUNNING_ANGLE;
        assert (angleTowardStarboard > 0);
        Bearing from;
        if (maneuver.getCourseChangeInDegrees() < 0) {
            from = maneuver.getSpeedWithBearingBefore().getBearing();
        } else {
            from = invertedCourseAfter.add(new DegreeBearingImpl(MIN_BEATING_ANGLE_PLUS_MIN_RUNNING_ANGLE));
        }
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), angleTowardStarboard);
        return windRange;
    }

    protected WindCourseRange getJibeWindRange(ManeuverForEstimation maneuver) {
        Bearing middleCourse = maneuver.getMiddleCourse();
        double absCourseChangeDeg = Math.abs(maneuver.getCourseChangeInDegrees());
        double middleAngleRange = maneuver.getDeviationFromOptimalJibeAngleInDegrees() == null
                ? absCourseChangeDeg * 0.2
                : Math.abs(maneuver.getDeviationFromOptimalJibeAngleInDegrees());
        if (middleAngleRange < 10) {
            middleAngleRange = 10;
        }
        Bearing from = middleCourse.add(new DegreeBearingImpl(-middleAngleRange / 2.0));
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), middleAngleRange);
        return windRange;
    }

    protected WindCourseRange getTackWindRange(ManeuverForEstimation maneuver) {
        Bearing middleCourse = maneuver.getMiddleCourse();
        double absCourseChangeDeg = Math.abs(maneuver.getCourseChangeInDegrees());
        double middleAngleRange = maneuver.getDeviationFromOptimalTackAngleInDegrees() == null
                ? absCourseChangeDeg * 0.1
                : Math.abs(maneuver.getDeviationFromOptimalTackAngleInDegrees());
        Bearing from = middleCourse.add(new DegreeBearingImpl(-middleAngleRange / 2.0));
        from = from.reverse();
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), middleAngleRange);
        return windRange;
    }

}
