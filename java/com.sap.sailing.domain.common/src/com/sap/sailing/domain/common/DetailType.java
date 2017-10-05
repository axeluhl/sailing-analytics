package com.sap.sailing.domain.common;

import static com.sap.sailing.domain.common.SortingOrder.ASCENDING;
import static com.sap.sailing.domain.common.SortingOrder.DESCENDING;
import static com.sap.sailing.domain.common.SortingOrder.NONE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Identifies details that can be requested from the racing service. Optionally, the details can specify a precision
 * as the number of decimal digits in which they are usually provided and should be formatted.
 * Additionally a default sorting order can be specified which can be used for table oriented views.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum DetailType implements Serializable {
    DISTANCE_TRAVELED(0, ASCENDING), DISTANCE_TRAVELED_INCLUDING_GATE_START(0, ASCENDING),
    AVERAGE_SPEED_OVER_GROUND_IN_KNOTS(2, DESCENDING), RACE_RANK(0, ASCENDING), REGATTA_RANK(0, ASCENDING), OVERALL_RANK(0, ASCENDING),
    RANK_GAIN(0, ASCENDING),
    NUMBER_OF_MANEUVERS(0, ASCENDING),
    CURRENT_SPEED_OVER_GROUND_IN_KNOTS(2,DESCENDING),
    CURRENT_HEEL_IN_DEGREES(2,DESCENDING),
    CURRENT_PITCH_IN_DEGREES(2,DESCENDING),
    CURRENT_RIDE_HEIGHT_IN_METERS(2,DESCENDING),
    CURRENT_DB_RAKE_PORT_IN_DEGREES(2,DESCENDING),
    CURRENT_DB_RAKE_STBD_IN_DEGREES(2,DESCENDING),
    CURRENT_RUDDER_RAKE_PORT_IN_DEGREES(2,DESCENDING),
    CURRENT_RUDDER_RAKE_STBD_IN_DEGREES(2,DESCENDING),
    CURRENT_MAST_ROTATION_IN_DEGREES(2,DESCENDING),
    ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS(1, ASCENDING), VELOCITY_MADE_GOOD_IN_KNOTS(2, DESCENDING),
    GAP_TO_LEADER_IN_SECONDS(0, ASCENDING), GAP_CHANGE_SINCE_LEG_START_IN_SECONDS(0, ASCENDING),
    SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED(0, ASCENDING), WINDWARD_DISTANCE_TO_GO_IN_METERS(0, ASCENDING),
    AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING), AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING), RACE_DISTANCE_TRAVELED(0, ASCENDING),
    RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START(0, ASCENDING),
    RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS(2, DESCENDING), RACE_GAP_TO_LEADER_IN_SECONDS(0, ASCENDING),
    RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS(0, ASCENDING), RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING),
    RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING),
    WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD(0, ASCENDING), START_TACK(0, ASCENDING), DISTANCE_TO_START_AT_RACE_START(1, ASCENDING),
    TIME_BETWEEN_RACE_START_AND_COMPETITOR_START(1, ASCENDING),
    SPEED_OVER_GROUND_AT_RACE_START(1, DESCENDING), SPEED_OVER_GROUND_WHEN_PASSING_START(1, DESCENDING),
    DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_PASSING_START_IN_METERS(1, ASCENDING),
    TACK(0, ASCENDING), JIBE(0, ASCENDING), PENALTY_CIRCLE(0, ASCENDING), AVERAGE_MANEUVER_LOSS_IN_METERS(1, ASCENDING),
    AVERAGE_TACK_LOSS_IN_METERS(1, ASCENDING), AVERAGE_JIBE_LOSS_IN_METERS(1, ASCENDING), CURRENT_LEG(0, ASCENDING),
    DISPLAY_LEGS(0, NONE), TIME_TRAVELED(0, ASCENDING), CORRECTED_TIME_TRAVELED(0, ASCENDING),
    TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS(1, ASCENDING),
    TOTAL_TIME_SAILED_UPWIND_IN_SECONDS(1, ASCENDING),
    TOTAL_TIME_SAILED_REACHING_IN_SECONDS(1, ASCENDING),
    MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS(1, DESCENDING),
    TIME_ON_TIME_FACTOR(4, DESCENDING), TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE(0, ASCENDING),
    TOTAL_DISTANCE_TRAVELED(0, ASCENDING), TOTAL_AVERAGE_SPEED_OVER_GROUND(2, DESCENDING),
    TOTAL_TIME_SAILED_IN_SECONDS(1, ASCENDING), RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS(2,
            DESCENDING), RACE_CURRENT_RIDE_HEIGHT_IN_METERS(2, DESCENDING),
    RACE_NET_POINTS(2, ASCENDING), REGATTA_NET_POINTS(2, ASCENDING), REGATTA_NET_POINTS_SUM(2, ASCENDING),
    RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL(1, ASCENDING), RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_RACE_START(1, ASCENDING),
    RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START(2, DESCENDING),
    RACE_CALCULATED_TIME_TRAVELED(0, ASCENDING), RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD(0, ASCENDING),
    RACE_TIME_TRAVELED(0, ASCENDING), RACE_TIME_TRAVELED_UPWIND(0, ASCENDING), RACE_TIME_TRAVELED_DOWNWIND(0, ASCENDING), RACE_TIME_TRAVELED_REACHING(0, ASCENDING),
    DISTANCE_TO_START_LINE(0, ASCENDING), BEAT_ANGLE(0, ASCENDING), COURSE_OVER_GROUND_TRUE_DEGREES(0, ASCENDING);
    
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
    
    /**
     * Default set of DetailTypes for charts, this list contains all commonly available data, without the use of extra
     * sensors.
     */
    public static List<DetailType> getDefaultDetailTypesForChart() {
        List<DetailType> availableDetailsTypes = new ArrayList<>();
        availableDetailsTypes.add(DetailType.WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD);
        availableDetailsTypes.add(DetailType.DISTANCE_TRAVELED);
        availableDetailsTypes.add(DetailType.DISTANCE_TRAVELED_INCLUDING_GATE_START);
        availableDetailsTypes.add(DetailType.VELOCITY_MADE_GOOD_IN_KNOTS);
        availableDetailsTypes.add(DetailType.GAP_TO_LEADER_IN_SECONDS);
        availableDetailsTypes.add(DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS);
        availableDetailsTypes.add(DetailType.RACE_RANK);
        availableDetailsTypes.add(DetailType.REGATTA_RANK);
        availableDetailsTypes.add(DetailType.DISTANCE_TO_START_LINE);
        availableDetailsTypes.add(DetailType.BEAT_ANGLE);
        availableDetailsTypes.add(DetailType.COURSE_OVER_GROUND_TRUE_DEGREES);
        return availableDetailsTypes;
    }

    /**
     * Special List of DetailTypes, that allows operators to select for example the RideHeight, that is usually only selectable, if it already has data.
     */
    public static List<DetailType> getAutoplayDetailTypesForChart() {
        List<DetailType> availableDetailsTypes = getDefaultDetailTypesForChart();
        availableDetailsTypes.add(DetailType.RACE_CURRENT_RIDE_HEIGHT_IN_METERS);
        availableDetailsTypes.add(DetailType.CURRENT_HEEL_IN_DEGREES);
        availableDetailsTypes.add(DetailType.CURRENT_PITCH_IN_DEGREES);
        return availableDetailsTypes;
    }
}