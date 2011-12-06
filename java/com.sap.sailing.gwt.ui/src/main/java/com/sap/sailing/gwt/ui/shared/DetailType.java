package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.StringConstants;

public enum DetailType implements IsSerializable {
    DISTANCE_TRAVELED(0), AVERAGE_SPEED_OVER_GROUND_IN_KNOTS(2), RANK_GAIN(0), NUMBER_OF_MANEUVERS(0), CURRENT_SPEED_OVER_GROUND_IN_KNOTS(2),
    ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS(1), VELOCITY_MADE_GOOD_IN_KNOTS(2), GAP_TO_LEADER_IN_SECONDS(0),
    WINDWARD_DISTANCE_TO_GO_IN_METERS(0), RACE_DISTANCE_TRAVELED(0), RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS(2), RACE_GAP_TO_LEADER_IN_SECONDS(0),
    RACE_MANEUVERS(0), WINDWARD_DISTANCE_TO_OVERALL_LEADER(0);
    
    private int precision;
    
    DetailType(int precision){
        this.precision = precision;
    }
    
    public int getPrecision(){
        return precision;
    }

    public String toString(StringConstants stringConstants) {
        switch (this) {
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
        case RACE_MANEUVERS:
            return stringConstants.numberOfManeuvers();
        case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
            return stringConstants.windwardDistanceToOverallLeader();
        }
        return null;
    }
}