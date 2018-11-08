package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class IntersectedWindRangeBasedTransitionProbabilitiesCalculator
        implements GraphNodeTransitionProbabilitiesCalculator {

    private static final int MIN_BEATING_ANGLE_PLUS_MIN_RUNNING_ANGLE = 40;

    public IntersectedWindRange getInitialWindRange(GraphNode currentNode, GraphLevel currentLevel) {
        return currentNode.getValidWindRange().toIntersected();
    }

    public Pair<IntersectedWindRange, Double> mergeWindRangeAndGetTransitionProbability(GraphNode previousNode,
            GraphLevel previousLevel, BestManeuverNodeInfo previousNodeInfo, GraphNode currentNode,
            GraphLevel currentLevel) {
        double secondsPassedSincePreviousWindRange = Math.abs(previousLevel.getManeuver().getManeuverTimePoint()
                .until(currentLevel.getManeuver().getManeuverTimePoint()).asSeconds());
        double transitionProbabilitySum = 0;
        double transitionProbabilityUntilCurrentNode = -1;
        IntersectedWindRange intersectedWindRangeUntilCurrentNode = null;
        for (GraphNode node : currentLevel.getLevelNodes()) {
            IntersectedWindRange intersectedWindRange = previousNode.getValidWindRange()
                    .intersect(node.getValidWindRange());
            double transitionProbability = intersectedWindRange
                    .getPenaltyFactorForTransition(secondsPassedSincePreviousWindRange);
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
    
//    public Pair<IntersectedWindRange, Double> mergeWindRangeAndGetTransitionProbability(GraphNode previousNode,
//            GraphLevel previousLevel, BestManeuverNodeInfo previousNodeInfo, GraphNode currentNode,
//            GraphLevel currentLevel) {
//        IntersectedWindRange intersectedWindRangeUntilCurrentNode = previousNodeInfo.getWindRange()
//                .intersect(currentNode.getValidWindRange());
//        double secondsPassedSincePreviousWindRange = Math.abs(previousLevel.getManeuver().getManeuverTimePoint()
//                .until(currentLevel.getManeuver().getManeuverTimePoint()).asSeconds());
//        double transitionProbability = intersectedWindRangeUntilCurrentNode
//                .getPenaltyFactorForTransition(secondsPassedSincePreviousWindRange);
//        return new Pair<>(intersectedWindRangeUntilCurrentNode, transitionProbability);
//    }

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

    private WindCourseRange getBearAwayWindRange(ManeuverForEstimation maneuver) {
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

    private WindCourseRange getHeadUpWindRange(ManeuverForEstimation maneuver) {
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

    private WindCourseRange getJibeWindRange(ManeuverForEstimation maneuver) {
        Bearing middleCourse = maneuver.getMiddleCourse();
        double absCourseChangeDeg = Math.abs(maneuver.getCourseChangeInDegrees());
        double middleAngleRange = maneuver.getDeviationFromOptimalJibeAngleInDegrees() == null
                ? absCourseChangeDeg * 0.2 : Math.abs(maneuver.getDeviationFromOptimalJibeAngleInDegrees());
        if(middleAngleRange < 10) {
            middleAngleRange = 10;
        }
        Bearing from = middleCourse.add(new DegreeBearingImpl(-middleAngleRange / 2.0));
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), middleAngleRange);
        return windRange;
    }

    private WindCourseRange getTackWindRange(ManeuverForEstimation maneuver) {
        Bearing middleCourse = maneuver.getMiddleCourse();
        double absCourseChangeDeg = Math.abs(maneuver.getCourseChangeInDegrees());
        double middleAngleRange = maneuver.getDeviationFromOptimalTackAngleInDegrees() == null
                ? absCourseChangeDeg * 0.1 : Math.abs(maneuver.getDeviationFromOptimalTackAngleInDegrees());
        Bearing from = middleCourse.add(new DegreeBearingImpl(-middleAngleRange / 2.0));
        from = from.reverse();
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), middleAngleRange);
        return windRange;
    }

}
