package com.sap.sailing.gwt.ui.client;

public enum DetailColumnType {
    DISTANCE_TRAVELED, AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, RANK_GAIN, CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
    ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS, VELOCITY_MADE_GOOD_IN_KNOTS, GAP_TO_LEADER_IN_SECONDS,
    WINDWARD_DISTANCE_TO_GO_IN_METERS, RACE_DISTANCE_TRAVELED, RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, RACE_GAP_TO_LEADER_IN_SECONDS;

    public String toString(StringConstants stringConstants) {
        switch (this) {
        case DISTANCE_TRAVELED:
            return stringConstants.distanceInMeters();
        case AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringConstants.averageSpeedInKnots();
        case RANK_GAIN:
            return stringConstants.rankGain();
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
        }
        return null;
    }
}