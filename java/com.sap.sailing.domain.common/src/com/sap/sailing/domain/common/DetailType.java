package com.sap.sailing.domain.common;

import java.io.Serializable;

/**
 * Identifies details that can be requested from the racing service. Optionally, the details can specify a precision
 * as the number of decimal digits in which they are usually provided and should be formatted.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum DetailType implements Serializable {
    DISTANCE_TRAVELED(0), AVERAGE_SPEED_OVER_GROUND_IN_KNOTS(2), RANK_GAIN(0), NUMBER_OF_MANEUVERS(0), CURRENT_SPEED_OVER_GROUND_IN_KNOTS(2),
    ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS(1), VELOCITY_MADE_GOOD_IN_KNOTS(2), GAP_TO_LEADER_IN_SECONDS(0),
    WINDWARD_DISTANCE_TO_GO_IN_METERS(0), RACE_DISTANCE_TRAVELED(0), RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS(2), RACE_GAP_TO_LEADER_IN_SECONDS(0),
    WINDWARD_DISTANCE_TO_OVERALL_LEADER(0), HEAD_UP(0), BEAR_AWAY(0), TACK(0), JIBE(0), PENALTY_CIRCLE(0), MARK_PASSING(0);
    
    private int precision;
    
    DetailType(int precision) {
        this.precision = precision;
    }
    
    public int getPrecision() {
        return precision;
    }
}