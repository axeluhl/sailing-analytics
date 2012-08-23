package com.sap.sailing.domain.common;

import java.io.Serializable;
import static com.sap.sailing.domain.common.SortingOrder.*;

/**
 * Identifies details that can be requested from the racing service. Optionally, the details can specify a precision
 * as the number of decimal digits in which they are usually provided and should be formatted.
 * Additionally a default sorting order can be specified which can be used for table oriented views.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum DetailType implements Serializable {
    DISTANCE_TRAVELED(0, ASCENDING), AVERAGE_SPEED_OVER_GROUND_IN_KNOTS(2, DESCENDING), RACE_RANK(0, ASCENDING), 
    RANK_GAIN(0, DESCENDING), NUMBER_OF_MANEUVERS(0, ASCENDING), CURRENT_SPEED_OVER_GROUND_IN_KNOTS(2, DESCENDING),
    ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS(1, ASCENDING), VELOCITY_MADE_GOOD_IN_KNOTS(2, DESCENDING),
    GAP_TO_LEADER_IN_SECONDS(0, ASCENDING), WINDWARD_DISTANCE_TO_GO_IN_METERS(0, ASCENDING),
    AVERAGE_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING), RACE_DISTANCE_TRAVELED(0, ASCENDING),
    RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS(2, DESCENDING), RACE_GAP_TO_LEADER_IN_SECONDS(0, ASCENDING),
    RACE_DISTANCE_TO_LEADER_IN_METERS(0, ASCENDING), RACE_AVERAGE_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING),
    WINDWARD_DISTANCE_TO_OVERALL_LEADER(0, ASCENDING), 
    HEAD_UP(0, ASCENDING), BEAR_AWAY(0, ASCENDING),
    TACK(0, ASCENDING), JIBE(0, ASCENDING), PENALTY_CIRCLE(0, ASCENDING), MARK_PASSING(0, ASCENDING), CURRENT_LEG(0, ASCENDING),
    DISPLAY_LEGS(0, NONE), TIME_TRAVELED(0, ASCENDING),
    TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS(1, ASCENDING),
    MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS(1, DESCENDING),
    TOTAL_TIME_SAILED_IN_SECONDS(1, ASCENDING), RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS(2, DESCENDING);
    
    private int precision;
    
    private SortingOrder defaultSortingOrder;
    
    DetailType(int precision, SortingOrder defaultSortingOrder) {
        this.precision = precision;
        this.defaultSortingOrder = defaultSortingOrder;
    }
    
    public int getPrecision() {
        return precision;
    }

    public SortingOrder getDefaultSortingOrder() {
        return defaultSortingOrder;
    }
}