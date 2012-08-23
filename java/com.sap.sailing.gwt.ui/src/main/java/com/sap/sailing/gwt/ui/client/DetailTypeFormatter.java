package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.DetailType;

public class DetailTypeFormatter {
    public static String format(DetailType detailType, StringMessages stringMessages) {
        switch (detailType) {
        case DISTANCE_TRAVELED:
            return stringMessages.distanceInMeters();
        case AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.averageSpeedInKnots();
        case RANK_GAIN:
            return stringMessages.rankGain();
        case RACE_RANK:
            return stringMessages.rank();
        case NUMBER_OF_MANEUVERS:
            return stringMessages.numberOfManeuvers();
        case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
        case RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.currentSpeedOverGroundInKnots();
        case ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS:
            return stringMessages.estimatedTimeToNextWaypointInSeconds();
        case VELOCITY_MADE_GOOD_IN_KNOTS:
            return stringMessages.velocityMadeGoodInKnots();
        case GAP_TO_LEADER_IN_SECONDS:
            return stringMessages.gapToLeaderInSeconds();
        case WINDWARD_DISTANCE_TO_GO_IN_METERS:
            return stringMessages.windwardDistanceToGoInMeters();
        case RACE_DISTANCE_TRAVELED:
            return stringMessages.distanceInMeters();
        case RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.averageSpeedInKnots();
        case RACE_GAP_TO_LEADER_IN_SECONDS:
            return stringMessages.gapToLeaderInSeconds();
        case RACE_DISTANCE_TO_LEADER_IN_METERS:
            return stringMessages.windwardDistanceToLeaderInMeters();
        case RACE_AVERAGE_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.averageCrossTrackErrorInMeters();
        case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
            return stringMessages.windwardDistanceToLeader();
        case HEAD_UP:
            return stringMessages.headUp();
        case BEAR_AWAY:
            return stringMessages.bearAway();
        case TACK:
            return stringMessages.tack();
        case JIBE:
            return stringMessages.jibe();
        case PENALTY_CIRCLE:
            return stringMessages.penaltyCircle();
        case MARK_PASSING:
            return stringMessages.markPassing();
        case DISPLAY_LEGS:
            return stringMessages.legs();
        case CURRENT_LEG:
            return stringMessages.currentLeg();
        case TIME_TRAVELED:
            return stringMessages.time();
        case AVERAGE_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.averageCrossTrackErrorInMeters();
        case MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.maximumSpeedOverGroundInKnots();
        case TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS:
            return stringMessages.totalTimeSailedDownwindInSeconds();
        case TOTAL_TIME_SAILED_IN_SECONDS:
            return stringMessages.totalTimeSailedInSeconds();
        }
        return null;

    }
}
