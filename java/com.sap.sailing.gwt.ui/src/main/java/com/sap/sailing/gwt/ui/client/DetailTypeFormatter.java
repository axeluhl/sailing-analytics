package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.server.api.DetailType;

public class DetailTypeFormatter {
    public static String format(DetailType detailType, StringMessages stringConstants) {
        switch (detailType) {
        case DISTANCE_TRAVELED:
            return stringConstants.distanceInMeters();
        case AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringConstants.averageSpeedInKnots();
        case RANK_GAIN:
            return stringConstants.rankGain();
        case NUMBER_OF_MANEUVERS:
            return stringConstants.numberOfManeuvers();
        case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
            return stringConstants.currentSpeedOverGroundInKnots();
        case ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS:
            return stringConstants.estimatedTimeToNextWaypointInSeconds();
        case VELOCITY_MADE_GOOD_IN_KNOTS:
            return stringConstants.velocityMadeGoodInKnots();
        case GAP_TO_LEADER_IN_SECONDS:
            return stringConstants.gapToLeaderInSeconds();
        case WINDWARD_DISTANCE_TO_GO_IN_METERS:
            return stringConstants.windwardDistanceToGoInMeters();
        case RACE_DISTANCE_TRAVELED:
            return stringConstants.distanceInMeters();
        case RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringConstants.averageSpeedInKnots();
        case RACE_GAP_TO_LEADER_IN_SECONDS:
            return stringConstants.gapToLeaderInSeconds();
        case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
            return stringConstants.windwardDistanceToOverallLeader();
        case HEAD_UP:
            return stringConstants.headUp();
        case BEAR_AWAY:
            return stringConstants.bearAway();
        case TACK:
            return stringConstants.tack();
        case JIBE:
            return stringConstants.jibe();
        case PENALTY_CIRCLE:
            return stringConstants.penaltyCircle();
        case MARK_PASSING:
            return stringConstants.markPassing();
        }
        return null;

    }
}
