package com.sap.sailing.domain.common;

import static com.sap.sailing.domain.common.security.SecuredDomainType.LeaderboardActions.PREMIUM_LEADERBOARD_INFORMATION;
import static com.sap.sse.common.SortingOrder.ASCENDING;
import static com.sap.sse.common.SortingOrder.DESCENDING;
import static com.sap.sse.common.SortingOrder.NONE;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sse.common.SortingOrder;
import com.sap.sse.security.shared.HasPermissions.Action;

/**
 * Identifies details that can be requested from the racing service. Optionally, the details can specify a precision as
 * the number of decimal digits in which they are usually provided and should be formatted. Additionally a default
 * sorting order can be specified which can be used for table oriented views.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum DetailType implements Serializable {
    LEG_TACKTYPE_LONGTACK_SHORTTACK(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION, "TACKTYPE"),
    LEG_DISTANCE_TRAVELED(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION, "DISTANCE_TRAVELED"),
    LEG_DISTANCE_TRAVELED_INCLUDING_GATE_START(0, ASCENDING, null, "DISTANCE_TRAVELED_INCLUDING_GATE_START"),
    LEG_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS(2, DESCENDING, PREMIUM_LEADERBOARD_INFORMATION, "AVERAGE_SPEED_OVER_GROUND_IN_KNOTS"),
    RACE_RANK(0, ASCENDING, null),
    REGATTA_RANK(0, ASCENDING, null),
    OVERALL_RANK(0, ASCENDING, null),
    LEG_RANK_GAIN(0, ASCENDING, null, "RANK_GAIN"),
    NUMBER_OF_MANEUVERS(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION),
    LEG_CURRENT_SPEED_OVER_GROUND_IN_KNOTS(2, DESCENDING, PREMIUM_LEADERBOARD_INFORMATION, "CURRENT_SPEED_OVER_GROUND_IN_KNOTS"),
    BRAVO_LEG_CURRENT_HEEL_IN_DEGREES(2, DESCENDING, null, "CURRENT_HEEL_IN_DEGREES"),
    BRAVO_LEG_CURRENT_PITCH_IN_DEGREES(2, DESCENDING, null, "CURRENT_PITCH_IN_DEGREES"),
    BRAVO_LEG_CURRENT_RIDE_HEIGHT_IN_METERS(2, DESCENDING, null, "CURRENT_RIDE_HEIGHT_IN_METERS"),
    BRAVOEXTENDED_LEG_CURRENT_DISTANCE_FOILED_IN_METERS(0, DESCENDING, null, "CURRENT_DISTANCE_FOILED_IN_METERS"),
    BRAVOEXTENDED_LEG_CURRENT_DURATION_FOILED_IN_SECONDS(0, DESCENDING, null, "CURRENT_DURATION_FOILED_IN_SECONDS"),
    BRAVOEXTENDED_RACE_CURRENT_PORT_DAGGERBOARD_RAKE(2, DESCENDING, null, "CURRENT_PORT_DAGGERBOARD_RAKE"),
    BRAVOEXTENDED_RACE_CURRENT_STBD_DAGGERBOARD_RAKE(2, DESCENDING, null, "CURRENT_STBD_DAGGERBOARD_RAKE"),
    BRAVOEXTENDED_RACE_CURRENT_PORT_RUDDER_RAKE(2, DESCENDING, null, "CURRENT_PORT_RUDDER_RAKE"),
    BRAVOEXTENDED_RACE_CURRENT_STBD_RUDDER_RAKE(2, DESCENDING, null, "CURRENT_STBD_RUDDER_RAKE"),
    BRAVOEXTENDED_RACE_CURRENT_MAST_ROTATION_IN_DEGREES(2, DESCENDING, null, "CURRENT_MAST_ROTATION_IN_DEGREES"),
    BRAVOEXTENDED_RACE_CURRENT_LEEWAY_IN_DEGREES(1, ASCENDING, null, "CURRENT_LEEWAY_IN_DEGREES"),
    BRAVOEXTENDED_RACE_CURRENT_SET(1, ASCENDING, null, "CURRENT_SET"),
    BRAVOEXTENDED_RACE_CURRENT_DRIFT_IN_DEGREES(1, ASCENDING, null, "CURRENT_DRIFT_IN_DEGREES"),
    BRAVOEXTENDED_RACE_CURRENT_DEPTH_IN_METERS(1, ASCENDING, null, "CURRENT_DEPTH_IN_METERS"),
    BRAVOEXTENDED_RACE_CURRENT_RUDDER_IN_DEGREES(2, ASCENDING, null, "CURRENT_RUDDER_IN_DEGREES"),
    BRAVOEXTENDED_RACE_CURRENT_TACK_ANGLE_IN_DEGREES(2, ASCENDING, null, "CURRENT_TACK_ANGLE_IN_DEGREES"),
    BRAVOEXTENDED_RACE_CURRENT_DEFLECTOR_PERCENTAGE(2, ASCENDING, null, "CURRENT_DEFLECTOR_PERCENTAGE"),
    BRAVOEXTENDED_RACE_CURRENT_DEFLECTOR_IN_MILLIMETERS(2, ASCENDING, null, "CURRENT_DEFLECTOR_IN_MILLIMETERS"),
    BRAVOEXTENDED_RACE_CURRENT_RAKE_IN_DEGREES(2, ASCENDING, null, "CURRENT_RAKE_IN_DEGREES"),
    BRAVOEXTENDED_RACE_CURRENT_TARGET_HEEL_ANGLE_IN_DEGREES(2, ASCENDING, null, "CURRENT_TARGET_HEEL_ANGLE_IN_DEGREES"),
    BRAVOEXTENDED_RACE_CURRENT_FORESTAY_LOAD(2, ASCENDING, null, "CURRENT_FORESTAY_LOAD"),
    BRAVOEXTENDED_RACE_CURRENT_FORESTAY_PRESSURE(2, ASCENDING, null, "CURRENT_FORESTAY_PRESSURE"),
    BRAVOEXTENDED_RACE_CURRENT_TARGET_BOATSPEED_PERCENTAGE(2, ASCENDING, null, "CURRENT_TARGET_BOATSPEED_PERCENTAGE"),
    LEG_ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS(1, ASCENDING, null, "ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS"),
    LEG_VELOCITY_MADE_GOOD_IN_KNOTS(2, DESCENDING, PREMIUM_LEADERBOARD_INFORMATION, "VELOCITY_MADE_GOOD_IN_KNOTS"),
    LEG_GAP_TO_LEADER_IN_SECONDS(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION, "GAP_TO_LEADER_IN_SECONDS"),
    LEG_GAP_CHANGE_SINCE_LEG_START_IN_SECONDS(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION, "GAP_CHANGE_SINCE_LEG_START_IN_SECONDS"),
    LEG_SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED(0, ASCENDING, null, "SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED"),
    LEG_WINDWARD_DISTANCE_TO_GO_IN_METERS(0, ASCENDING, null, "WINDWARD_DISTANCE_TO_GO_IN_METERS"),
    LEG_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION, "AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS"),
    LEG_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION, "AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS"),
    LEG_CURRENT_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING, null),
    LEG_CURRENT_SIGNED_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING, null),
    RACE_DISTANCE_TRAVELED(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION),
    RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START(0, ASCENDING, null),
    RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS(2, DESCENDING, PREMIUM_LEADERBOARD_INFORMATION),
    RACE_GAP_TO_LEADER_IN_SECONDS(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION),
    RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION),
    RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION),
    RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION),
    CHART_WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD(0, ASCENDING, null),
    START_TACK(0, ASCENDING, null),
    DISTANCE_TO_START_AT_RACE_START(1, ASCENDING, null),
    TIME_BETWEEN_RACE_START_AND_COMPETITOR_START(1, ASCENDING, null),
    SPEED_OVER_GROUND_AT_RACE_START(1, DESCENDING, null),
    SPEED_OVER_GROUND_WHEN_PASSING_START(1, DESCENDING, null),
    DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_PASSING_START_IN_METERS(1, ASCENDING, null),
    TACK(0, ASCENDING, null),
    JIBE(0, ASCENDING, null),
    PENALTY_CIRCLE(0, ASCENDING, null),
    AVERAGE_MANEUVER_LOSS_IN_METERS(1, ASCENDING, null),
    AVERAGE_TACK_LOSS_IN_METERS(1, ASCENDING, null),
    AVERAGE_JIBE_LOSS_IN_METERS(1, ASCENDING, null),
    RACE_CURRENT_LEG(0, ASCENDING, null, "CURRENT_LEG"),
    RACE_DISPLAY_LEGS(0, NONE, null, "DISPLAY_LEGS"),
    RACE_DISPLAY_BOATS(0, NONE, null),
    LEG_TIME_TRAVELED(0, ASCENDING, null, "TIME_TRAVELED"),
    LEG_CORRECTED_TIME_TRAVELED(0, ASCENDING, null, "CORRECTED_TIME_TRAVELED"),
    TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS(1, ASCENDING, null),
    TOTAL_TIME_SAILED_UPWIND_IN_SECONDS(1, ASCENDING, null),
    TOTAL_TIME_SAILED_REACHING_IN_SECONDS(1, ASCENDING, null),
    OVERALL_MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS(1, DESCENDING, null, "MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS"),
    OVERALL_TIME_ON_TIME_FACTOR(4, DESCENDING, null, "TIME_ON_TIME_FACTOR"),
    OVERALL_TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE(
            0,
            ASCENDING,
            null,
            "TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE"),
    OVERALL_TOTAL_DISTANCE_TRAVELED(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION, "TOTAL_DISTANCE_TRAVELED"),
    OVERALL_TOTAL_AVERAGE_SPEED_OVER_GROUND(2, DESCENDING, PREMIUM_LEADERBOARD_INFORMATION, "TOTAL_AVERAGE_SPEED_OVER_GROUND"),
    OVERALL_TOTAL_TIME_SAILED_IN_SECONDS(1, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION, "TOTAL_TIME_SAILED_IN_SECONDS"),
    OVERALL_TOTAL_DURATION_FOILED_IN_SECONDS(0, DESCENDING, null, "TOTAL_DURATION_FOILED_IN_SECONDS"),
    OVERALL_TOTAL_DISTANCE_FOILED_IN_METERS(0, DESCENDING, null, "TOTAL_DISTANCE_FOILED_IN_METERS"),
    RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS(2, DESCENDING, PREMIUM_LEADERBOARD_INFORMATION),
    RACE_CURRENT_COURSE_OVER_GROUND_IN_TRUE_DEGREES(1, ASCENDING, null),
    RACE_CURRENT_POSITION_LAT_DEG(10, ASCENDING, null),
    RACE_CURRENT_POSITION_LNG_DEG(10, ASCENDING, null),
    BRAVO_RACE_CURRENT_RIDE_HEIGHT_IN_METERS(2, DESCENDING, null),
    RACE_CURRENT_DISTANCE_FOILED_IN_METERS(0, DESCENDING, null),
    RACE_CURRENT_DURATION_FOILED_IN_SECONDS(0, DESCENDING, null),
    RACE_NET_POINTS(2, ASCENDING, null),
    REGATTA_NET_POINTS(2, ASCENDING, null),
    REGATTA_NET_POINTS_SUM(2, ASCENDING, null),
    RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL(1, DESCENDING, null),
    RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_RACE_START(1, ASCENDING, null),
    RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START(2, DESCENDING, null),
    RACE_CALCULATED_TIME_TRAVELED(0, ASCENDING, null),
    RACE_IMPLIED_WIND(5, DESCENDING, null),
    RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD(0, ASCENDING, PREMIUM_LEADERBOARD_INFORMATION),
    RACE_TIME_TRAVELED(0, ASCENDING, null),
    RACE_TIME_TRAVELED_UPWIND(0, ASCENDING, null),
    RACE_TIME_TRAVELED_DOWNWIND(0, ASCENDING, null),
    RACE_TIME_TRAVELED_REACHING(0, ASCENDING, null),
    CHART_DISTANCE_TO_START_LINE(0, ASCENDING, null, "DISTANCE_TO_START_LINE"),
    CHART_BEAT_ANGLE(0, ASCENDING, null, "BEAT_ANGLE"),
    CHART_ABS_TWA(0, ASCENDING, null),
    CHART_COURSE_OVER_GROUND_TRUE_DEGREES(0, ASCENDING, null, "COURSE_OVER_GROUND_TRUE_DEGREES"),
    OVERALL_TOTAL_SCORED_RACE_COUNT(0, ASCENDING, null, "TOTAL_SCORED_RACE_COUNT"),
    EXPEDITION_RACE_AWA(2, ASCENDING, null),
    EXPEDITION_RACE_AWS(2, ASCENDING, null),
    EXPEDITION_RACE_TWA(2, ASCENDING, null),
    EXPEDITION_RACE_TWS(2, ASCENDING, null),
    EXPEDITION_RACE_TWD(2, ASCENDING, null),
    EXPEDITION_RACE_TARG_TWA(2, ASCENDING, null),
    EXPEDITION_RACE_BOAT_SPEED(2, ASCENDING, null),
    EXPEDITION_RACE_TARG_BOAT_SPEED(2, ASCENDING, null),
    EXPEDITION_RACE_SOG(2, ASCENDING, null),
    EXPEDITION_RACE_COG(2, ASCENDING, null),
    EXPEDITION_RACE_FORESTAY_LOAD(2, ASCENDING, null),
    EXPEDITION_RACE_RAKE(2, ASCENDING, null),
    EXPEDITION_RACE_COURSE(2, ASCENDING, null),
    EXPEDITION_RACE_HEADING(2, ASCENDING, null),
    EXPEDITION_RACE_VMG(2, ASCENDING, null),
    EXPEDITION_RACE_VMG_TARG_VMG_DELTA(2, ASCENDING, null),
    EXPEDITION_RACE_RATE_OF_TURN(2, ASCENDING, null),
    EXPEDITION_RACE_RUDDER_ANGLE(2, ASCENDING, null),
    EXPEDITION_RACE_TARGET_HEEL(2, ASCENDING, null),
    EXPEDITION_RACE_TIME_TO_PORT_LAYLINE(2, ASCENDING, null),
    EXPEDITION_RACE_TIME_TO_STB_LAYLINE(2, ASCENDING, null),
    EXPEDITION_RACE_DIST_TO_PORT_LAYLINE(2, ASCENDING, null),
    EXPEDITION_RACE_DIST_TO_STB_LAYLINE(2, ASCENDING, null),
    EXPEDITION_RACE_TIME_TO_GUN(2, ASCENDING, null),
    EXPEDITION_RACE_TIME_TO_COMMITTEE_BOAT(2, ASCENDING, null),
    EXPEDITION_RACE_TIME_TO_PIN(2, ASCENDING, null),
    EXPEDITION_RACE_TIME_TO_BURN_TO_LINE(2, ASCENDING, null),
    EXPEDITION_RACE_TIME_TO_BURN_TO_COMMITTEE_BOAT(2, ASCENDING, null),
    EXPEDITION_RACE_TIME_TO_BURN_TO_PIN(2, ASCENDING, null),
    EXPEDITION_RACE_DISTANCE_TO_COMMITTEE_BOAT(2, ASCENDING, null),
    EXPEDITION_RACE_DISTANCE_TO_PIN(2, ASCENDING, null),
    EXPEDITION_RACE_DISTANCE_BELOW_LINE(2, ASCENDING, null),
    EXPEDITION_RACE_LINE_SQUARE_FOR_WIND_DIRECTION(2, ASCENDING, null),
    EXPEDITION_LEG_AWA(2, ASCENDING, null),
    EXPEDITION_LEG_AWS(2, ASCENDING, null),
    EXPEDITION_LEG_TWA(2, ASCENDING, null),
    EXPEDITION_LEG_TWS(2, ASCENDING, null),
    EXPEDITION_LEG_TWD(2, ASCENDING, null),
    EXPEDITION_LEG_TARG_TWA(2, ASCENDING, null),
    EXPEDITION_LEG_BOAT_SPEED(2, ASCENDING, null),
    EXPEDITION_LEG_TARG_BOAT_SPEED(2, ASCENDING, null),
    EXPEDITION_LEG_SOG(2, ASCENDING, null),
    EXPEDITION_LEG_COG(2, ASCENDING, null),
    EXPEDITION_LEG_FORESTAY_LOAD(2, ASCENDING, null),
    EXPEDITION_LEG_RAKE(2, ASCENDING, null),
    EXPEDITION_LEG_COURSE(2, ASCENDING, null),
    EXPEDITION_LEG_HEADING(2, ASCENDING, null),
    EXPEDITION_LEG_VMG(2, ASCENDING, null),
    EXPEDITION_LEG_VMG_TARG_VMG_DELTA(2, ASCENDING, null),
    EXPEDITION_LEG_RATE_OF_TURN(2, ASCENDING, null),
    EXPEDITION_LEG_RUDDER_ANGLE(2, ASCENDING, null),
    EXPEDITION_LEG_TARGET_HEEL(2, ASCENDING, null),
    EXPEDITION_LEG_TIME_TO_PORT_LAYLINE(2, ASCENDING, null),
    EXPEDITION_LEG_TIME_TO_STB_LAYLINE(2, ASCENDING, null),
    EXPEDITION_LEG_DIST_TO_PORT_LAYLINE(2, ASCENDING, null),
    EXPEDITION_LEG_DIST_TO_STB_LAYLINE(2, ASCENDING, null),
    EXPEDITION_LEG_TIME_TO_GUN(2, ASCENDING, null),
    EXPEDITION_LEG_TIME_TO_COMMITTEE_BOAT(2, ASCENDING, null),
    EXPEDITION_LEG_TIME_TO_PIN(2, ASCENDING, null),
    EXPEDITION_LEG_TIME_TO_BURN_TO_LINE(2, ASCENDING, null),
    EXPEDITION_LEG_TIME_TO_BURN_TO_COMMITTEE_BOAT(2, ASCENDING, null),
    EXPEDITION_LEG_TIME_TO_BURN_TO_PIN(2, ASCENDING, null),
    EXPEDITION_LEG_DISTANCE_TO_COMMITTEE_BOAT(2, ASCENDING, null),
    EXPEDITION_LEG_DISTANCE_TO_PIN(2, ASCENDING, null),
    EXPEDITION_LEG_DISTANCE_BELOW_LINE(2, ASCENDING, null),
    EXPEDITION_LEG_LINE_SQUARE_FOR_WIND_DIRECTION(2, ASCENDING, null),
    EXPEDITION_RACE_BARO(2, ASCENDING, null),
    EXPEDITION_RACE_LOAD_S(2, ASCENDING, null),
    EXPEDITION_RACE_LOAD_P(2, ASCENDING, null),
    EXPEDITION_RACE_JIB_CAR_PORT(2, ASCENDING, null),
    EXPEDITION_RACE_JIB_CAR_STBD(2, ASCENDING, null),
    EXPEDITION_RACE_MAST_BUTT(2, ASCENDING, null),
    EXPEDITION_LEG_BARO(2, ASCENDING, null),
    EXPEDITION_LEG_LOAD_S(2, ASCENDING, null),
    EXPEDITION_LEG_LOAD_P(2, ASCENDING, null),
    EXPEDITION_LEG_JIB_CAR_PORT(2, ASCENDING, null),
    EXPEDITION_LEG_JIB_CAR_STBD(2, ASCENDING, null),
    EXPEDITION_LEG_MAST_BUTT(2, ASCENDING, null),
    BRAVO_RACE_HEEL_IN_DEGREES(2, DESCENDING, null),
    BRAVO_RACE_PITCH_IN_DEGREES(2, DESCENDING, null);

    private int precision;

    private SortingOrder defaultSortingOrder;
    
    private Action premiumAction;

    private final String[] oldNames;

    DetailType(int precision, SortingOrder defaultSortingOrder, Action premiumAction, String... oldNames) {
        this.oldNames = oldNames;
        this.precision = precision;
        this.premiumAction = premiumAction;
        this.defaultSortingOrder = defaultSortingOrder;
    }

    public int getPrecision() {
        return precision;
    }

    public SortingOrder getDefaultSortingOrder() {
        return defaultSortingOrder;
    }
    
    /**
     * If not {@code null} then the user must have permission to execute the action returned on the
     * leaderboard to which it is applied.
     */
    public Action getPremiumAction() {
        return this.premiumAction;
    }

    /**
     * Determines whether or not this {@link DetailType} represents an expedition data type.
     * 
     * @return <code>true</code> if this {@link DetailType} is an expedition data type, <code>false</code> otherwise
     * 
     * @see #getLegExpeditionDetailColumnTypes()
     * @see #getRaceExpeditionDetailTypes()
     */
    public boolean isExpeditionType() {
        return getLegExpeditionDetailColumnTypes().contains(this) || getRaceExpeditionDetailTypes().contains(this);
    }

    /**
     * Determines whether or not this {@link DetailType} represents a degree detail which needs to be recalculated, e.g.
     * to ensure continuous linear rendering in chart avoiding leaps from 360 to 0 degrees and vice verse.
     * 
     * @return <code>true</code> if this {@link DetailType} is a degree detail type which needs to be recalculated,
     *         <code>false</code> otherwise
     * 
     * @see #CHART_COURSE_OVER_GROUND_TRUE_DEGREES
     */
    public boolean isDegreeTypeWithRecalculation() {
        return this == CHART_COURSE_OVER_GROUND_TRUE_DEGREES;
    }

    /**
     * Special collection of {@link DetailType}s, that allows operators to select for example the RideHeight, that is
     * usually only selectable, if it already has data.
     */
    public static Collection<DetailType> getAutoplayDetailTypesForChart() {
        final Collection<DetailType> availableDetailsTypes = new LinkedHashSet<>();
        availableDetailsTypes.add(DetailType.CHART_WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD);
        availableDetailsTypes.add(DetailType.LEG_CURRENT_SPEED_OVER_GROUND_IN_KNOTS);
        availableDetailsTypes.add(DetailType.LEG_DISTANCE_TRAVELED);
        availableDetailsTypes.add(DetailType.LEG_VELOCITY_MADE_GOOD_IN_KNOTS);
        availableDetailsTypes.add(DetailType.LEG_GAP_TO_LEADER_IN_SECONDS);
        availableDetailsTypes.add(DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS);
        availableDetailsTypes.add(DetailType.RACE_RANK);
        availableDetailsTypes.add(DetailType.REGATTA_RANK);
        availableDetailsTypes.add(DetailType.CHART_DISTANCE_TO_START_LINE);
        availableDetailsTypes.add(DetailType.CHART_BEAT_ANGLE);
        availableDetailsTypes.add(DetailType.CHART_ABS_TWA);
        availableDetailsTypes.add(DetailType.CHART_COURSE_OVER_GROUND_TRUE_DEGREES);
        availableDetailsTypes.add(DetailType.BRAVO_RACE_CURRENT_RIDE_HEIGHT_IN_METERS);
        availableDetailsTypes.add(DetailType.BRAVO_RACE_HEEL_IN_DEGREES);
        availableDetailsTypes.add(DetailType.BRAVO_RACE_PITCH_IN_DEGREES);
        return availableDetailsTypes;
    }

    public static Collection<DetailType> getRaceExpeditionDetailTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(EXPEDITION_RACE_AWA);
        allowed.add(EXPEDITION_RACE_AWS);
        allowed.add(EXPEDITION_RACE_TWA);
        allowed.add(EXPEDITION_RACE_TWS);
        allowed.add(EXPEDITION_RACE_TWD);
        allowed.add(EXPEDITION_RACE_TARG_TWA);
        allowed.add(EXPEDITION_RACE_BOAT_SPEED);
        allowed.add(EXPEDITION_RACE_TARG_BOAT_SPEED);
        allowed.add(EXPEDITION_RACE_SOG);
        allowed.add(EXPEDITION_RACE_COG);
        allowed.add(EXPEDITION_RACE_FORESTAY_LOAD);
        allowed.add(EXPEDITION_RACE_RAKE);
        allowed.add(EXPEDITION_RACE_COURSE);
        allowed.add(EXPEDITION_RACE_HEADING);
        allowed.add(EXPEDITION_RACE_VMG);
        allowed.add(EXPEDITION_RACE_VMG_TARG_VMG_DELTA);
        allowed.add(EXPEDITION_RACE_RATE_OF_TURN);
        allowed.add(EXPEDITION_RACE_RUDDER_ANGLE);
        allowed.add(EXPEDITION_RACE_TARGET_HEEL);
        allowed.add(EXPEDITION_RACE_TIME_TO_PORT_LAYLINE);
        allowed.add(EXPEDITION_RACE_TIME_TO_STB_LAYLINE);
        allowed.add(EXPEDITION_RACE_DIST_TO_PORT_LAYLINE);
        allowed.add(EXPEDITION_RACE_DIST_TO_STB_LAYLINE);
        allowed.add(EXPEDITION_RACE_TIME_TO_GUN);
        allowed.add(EXPEDITION_RACE_TIME_TO_COMMITTEE_BOAT);
        allowed.add(EXPEDITION_RACE_TIME_TO_PIN);
        allowed.add(EXPEDITION_RACE_TIME_TO_BURN_TO_LINE);
        allowed.add(EXPEDITION_RACE_TIME_TO_BURN_TO_COMMITTEE_BOAT);
        allowed.add(EXPEDITION_RACE_TIME_TO_BURN_TO_PIN);
        allowed.add(EXPEDITION_RACE_DISTANCE_TO_COMMITTEE_BOAT);
        allowed.add(EXPEDITION_RACE_DISTANCE_TO_PIN);
        allowed.add(EXPEDITION_RACE_DISTANCE_BELOW_LINE);
        allowed.add(EXPEDITION_RACE_LINE_SQUARE_FOR_WIND_DIRECTION);
        allowed.add(EXPEDITION_RACE_BARO);
        allowed.add(EXPEDITION_RACE_LOAD_S);
        allowed.add(EXPEDITION_RACE_LOAD_P);
        allowed.add(EXPEDITION_RACE_JIB_CAR_PORT);
        allowed.add(EXPEDITION_RACE_JIB_CAR_STBD);
        allowed.add(EXPEDITION_RACE_MAST_BUTT);
        return allowed;
    }

     public static Collection<DetailType> getRaceExtendedBravoDetailTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_PORT_DAGGERBOARD_RAKE);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_STBD_DAGGERBOARD_RAKE);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_PORT_RUDDER_RAKE);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_STBD_RUDDER_RAKE);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_MAST_ROTATION_IN_DEGREES);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_LEEWAY_IN_DEGREES);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_SET);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_DRIFT_IN_DEGREES);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_DEPTH_IN_METERS);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_RUDDER_IN_DEGREES);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_TACK_ANGLE_IN_DEGREES);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_DEFLECTOR_PERCENTAGE);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_DEFLECTOR_IN_MILLIMETERS);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_RAKE_IN_DEGREES);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_TARGET_HEEL_ANGLE_IN_DEGREES);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_FORESTAY_LOAD);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_FORESTAY_PRESSURE);
        allowed.add(DetailType.BRAVOEXTENDED_RACE_CURRENT_TARGET_BOATSPEED_PERCENTAGE);
        return allowed;
    }

    public static Collection<DetailType> getRaceBravoDetailTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(DetailType.BRAVO_RACE_HEEL_IN_DEGREES);
        allowed.add(DetailType.BRAVO_RACE_PITCH_IN_DEGREES);
        allowed.add(DetailType.BRAVO_RACE_CURRENT_RIDE_HEIGHT_IN_METERS);
        allowed.add(DetailType.RACE_CURRENT_DISTANCE_FOILED_IN_METERS);
        allowed.add(DetailType.RACE_CURRENT_DURATION_FOILED_IN_SECONDS);
        return allowed;
    }

    public static Collection<DetailType> getLegBravoDetailTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(DetailType.BRAVO_LEG_CURRENT_HEEL_IN_DEGREES);
        allowed.add(DetailType.BRAVO_LEG_CURRENT_PITCH_IN_DEGREES);
        allowed.add(DetailType.BRAVO_LEG_CURRENT_RIDE_HEIGHT_IN_METERS);
        allowed.add(DetailType.BRAVOEXTENDED_LEG_CURRENT_DISTANCE_FOILED_IN_METERS);
        allowed.add(DetailType.BRAVOEXTENDED_LEG_CURRENT_DURATION_FOILED_IN_SECONDS);
        return allowed;
    }

    public static Collection<DetailType> getAllRaceDetailTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(DetailType.LEG_TACKTYPE_LONGTACK_SHORTTACK);
        allowed.add(RACE_GAP_TO_LEADER_IN_SECONDS);
        allowed.add(RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        allowed.add(RACE_DISTANCE_TRAVELED);
        allowed.add(RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START);
        allowed.add(RACE_TIME_TRAVELED);
        allowed.add(RACE_CALCULATED_TIME_TRAVELED);
        allowed.add(RACE_IMPLIED_WIND);
        allowed.add(RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD);
        allowed.add(RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS);
        allowed.add(RACE_CURRENT_COURSE_OVER_GROUND_IN_TRUE_DEGREES);
        allowed.add(RACE_CURRENT_DISTANCE_FOILED_IN_METERS);
        allowed.add(RACE_CURRENT_DURATION_FOILED_IN_SECONDS);
        allowed.add(RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS);
        allowed.add(NUMBER_OF_MANEUVERS);
        allowed.add(RACE_DISPLAY_LEGS);
        allowed.add(RACE_DISPLAY_BOATS);
        allowed.add(RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS);
        allowed.add(RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS);
        allowed.add(RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL);
        allowed.add(RACE_CURRENT_LEG);
        allowed.addAll(getRaceBravoDetailTypes());
        allowed.addAll(getRaceExtendedBravoDetailTypes());
        allowed.addAll(getRaceExpeditionDetailTypes());
        return allowed;
    }

    /**
     * Returns all types in the enum, minus those for expedition, bravo, bravo extended
     * and the ToT/ToD/ORC PCS ones
     */
    public static Collection<DetailType> getAllNonRestrictedDetailTypes() {
        final Collection<DetailType> all = new LinkedHashSet<>(Arrays.asList(values()));
        all.removeAll(getRaceBravoDetailTypes());
        all.removeAll(getLegBravoDetailTypes());
        all.removeAll(getRaceExtendedBravoDetailTypes());
        all.removeAll(getRaceExpeditionDetailTypes());
        all.removeAll(getLegExpeditionDetailColumnTypes());
        all.removeAll(getOverallBravoDetailTypes());
        all.removeAll(getAllToTToDHandicapDetailTypes());
        all.removeAll(getAllOrcPerformanceCurveDetailTypes());
        return all;
    }

    public static Collection<DetailType> getAvailableOverallDetailColumnTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(REGATTA_RANK);
        allowed.add(OVERALL_TOTAL_DISTANCE_TRAVELED);
        allowed.add(OVERALL_TOTAL_AVERAGE_SPEED_OVER_GROUND);
        allowed.add(OVERALL_TOTAL_TIME_SAILED_IN_SECONDS);
        allowed.add(OVERALL_MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS);
        // TODO bug5209 make ToT / ToD details depend on ranking metric
        allowed.add(OVERALL_TIME_ON_TIME_FACTOR);
        allowed.add(OVERALL_TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE);
        allowed.add(OVERALL_TOTAL_SCORED_RACE_COUNT);
        return allowed;
    }

    public static Collection<DetailType> getOverallBravoDetailTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(OVERALL_TOTAL_DISTANCE_FOILED_IN_METERS);
        allowed.add(OVERALL_TOTAL_DURATION_FOILED_IN_SECONDS);
        return allowed;
    }

    public static Collection<? extends DetailType> getAllToTToDHandicapDetailTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD);
        allowed.add(RACE_CALCULATED_TIME_TRAVELED);
        return allowed;
    }

    public static Collection<? extends DetailType> getAllOrcPerformanceCurveDetailTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD);
        allowed.add(RACE_CALCULATED_TIME_TRAVELED);
        allowed.add(RACE_IMPLIED_WIND);
        return allowed;
    }

    public static Collection<DetailType> getLegDetailColumnTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(LEG_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        allowed.add(LEG_DISTANCE_TRAVELED);
        allowed.add(LEG_DISTANCE_TRAVELED_INCLUDING_GATE_START);
        allowed.add(LEG_GAP_TO_LEADER_IN_SECONDS);
        allowed.add(LEG_GAP_CHANGE_SINCE_LEG_START_IN_SECONDS);
        allowed.add(LEG_SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED);
        allowed.add(LEG_CURRENT_SPEED_OVER_GROUND_IN_KNOTS);
        allowed.add(LEG_WINDWARD_DISTANCE_TO_GO_IN_METERS);
        allowed.add(NUMBER_OF_MANEUVERS);
        allowed.add(LEG_ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS);
        allowed.add(LEG_VELOCITY_MADE_GOOD_IN_KNOTS);
        allowed.add(LEG_TIME_TRAVELED);
        allowed.add(LEG_CORRECTED_TIME_TRAVELED);
        allowed.add(LEG_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS);
        allowed.add(LEG_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS);
        allowed.add(LEG_RANK_GAIN);
        return allowed;
    }

    public static Collection<DetailType> getRaceStartAnalysisColumnTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_RACE_START);
        allowed.add(RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START);
        allowed.add(DISTANCE_TO_START_AT_RACE_START);
        allowed.add(TIME_BETWEEN_RACE_START_AND_COMPETITOR_START);
        allowed.add(SPEED_OVER_GROUND_AT_RACE_START);
        allowed.add(SPEED_OVER_GROUND_WHEN_PASSING_START);
        allowed.add(DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_PASSING_START_IN_METERS);
        allowed.add(START_TACK);
        return allowed;
    }

    public static Collection<DetailType> getLegExpeditionDetailColumnTypes() {
        final Collection<DetailType> allowed = new LinkedHashSet<>();
        allowed.add(EXPEDITION_LEG_AWA);
        allowed.add(EXPEDITION_LEG_AWS);
        allowed.add(EXPEDITION_LEG_TWA);
        allowed.add(EXPEDITION_LEG_TWS);
        allowed.add(EXPEDITION_LEG_TWD);
        allowed.add(EXPEDITION_LEG_TARG_TWA);
        allowed.add(EXPEDITION_LEG_BOAT_SPEED);
        allowed.add(EXPEDITION_LEG_TARG_BOAT_SPEED);
        allowed.add(EXPEDITION_LEG_SOG);
        allowed.add(EXPEDITION_LEG_COG);
        allowed.add(EXPEDITION_LEG_FORESTAY_LOAD);
        allowed.add(EXPEDITION_LEG_RAKE);
        allowed.add(EXPEDITION_LEG_COURSE);
        allowed.add(EXPEDITION_LEG_HEADING);
        allowed.add(EXPEDITION_LEG_VMG);
        allowed.add(EXPEDITION_LEG_VMG_TARG_VMG_DELTA);
        allowed.add(EXPEDITION_LEG_RATE_OF_TURN);
        allowed.add(EXPEDITION_LEG_RUDDER_ANGLE);
        allowed.add(EXPEDITION_LEG_TARGET_HEEL);
        allowed.add(EXPEDITION_LEG_TIME_TO_PORT_LAYLINE);
        allowed.add(EXPEDITION_LEG_TIME_TO_STB_LAYLINE);
        allowed.add(EXPEDITION_LEG_DIST_TO_PORT_LAYLINE);
        allowed.add(EXPEDITION_LEG_DIST_TO_STB_LAYLINE);
        allowed.add(EXPEDITION_LEG_TIME_TO_GUN);
        allowed.add(EXPEDITION_LEG_TIME_TO_COMMITTEE_BOAT);
        allowed.add(EXPEDITION_LEG_TIME_TO_PIN);
        allowed.add(EXPEDITION_LEG_TIME_TO_BURN_TO_LINE);
        allowed.add(EXPEDITION_LEG_TIME_TO_BURN_TO_COMMITTEE_BOAT);
        allowed.add(EXPEDITION_LEG_TIME_TO_BURN_TO_PIN);
        allowed.add(EXPEDITION_LEG_DISTANCE_TO_COMMITTEE_BOAT);
        allowed.add(EXPEDITION_LEG_DISTANCE_TO_PIN);
        allowed.add(EXPEDITION_LEG_DISTANCE_BELOW_LINE);
        allowed.add(EXPEDITION_LEG_LINE_SQUARE_FOR_WIND_DIRECTION);
        allowed.add(EXPEDITION_LEG_BARO);
        allowed.add(EXPEDITION_LEG_LOAD_S);
        allowed.add(EXPEDITION_LEG_LOAD_P);
        allowed.add(EXPEDITION_LEG_JIB_CAR_PORT);
        allowed.add(EXPEDITION_LEG_JIB_CAR_STBD);
        allowed.add(EXPEDITION_LEG_MAST_BUTT);
        return allowed;
    }

    /**
     * Not yet implemented types can be added here, they will be hidden from any user under all circumstances
     */
    public static Collection<DetailType> getDisabledDetailColumTypes() {
        final Collection<DetailType> disabled = new LinkedHashSet<>();
        disabled.add(EXPEDITION_LEG_TARG_TWA);
        disabled.add(EXPEDITION_LEG_VMG);
        disabled.add(EXPEDITION_LEG_VMG_TARG_VMG_DELTA);
        disabled.add(EXPEDITION_LEG_RATE_OF_TURN);
        disabled.add(EXPEDITION_LEG_RUDDER_ANGLE);
        disabled.add(EXPEDITION_LEG_TIME_TO_PORT_LAYLINE);
        disabled.add(EXPEDITION_LEG_TIME_TO_STB_LAYLINE);
        disabled.add(EXPEDITION_LEG_DIST_TO_PORT_LAYLINE);
        disabled.add(EXPEDITION_LEG_DIST_TO_STB_LAYLINE);
        disabled.add(EXPEDITION_LEG_TIME_TO_COMMITTEE_BOAT);
        disabled.add(EXPEDITION_LEG_TIME_TO_PIN);
        disabled.add(EXPEDITION_LEG_TIME_TO_BURN_TO_COMMITTEE_BOAT);
        disabled.add(EXPEDITION_LEG_TIME_TO_BURN_TO_PIN);
        disabled.add(EXPEDITION_LEG_DISTANCE_TO_COMMITTEE_BOAT);
        disabled.add(EXPEDITION_LEG_DISTANCE_TO_PIN);
        disabled.add(EXPEDITION_LEG_LINE_SQUARE_FOR_WIND_DIRECTION);
        disabled.add(EXPEDITION_RACE_TARG_TWA);
        disabled.add(EXPEDITION_RACE_VMG);
        disabled.add(EXPEDITION_RACE_VMG_TARG_VMG_DELTA);
        disabled.add(EXPEDITION_RACE_RUDDER_ANGLE);
        disabled.add(EXPEDITION_RACE_TIME_TO_PORT_LAYLINE);
        disabled.add(EXPEDITION_RACE_TIME_TO_STB_LAYLINE);
        disabled.add(EXPEDITION_RACE_DIST_TO_PORT_LAYLINE);
        disabled.add(EXPEDITION_RACE_DIST_TO_STB_LAYLINE);
        disabled.add(EXPEDITION_RACE_TIME_TO_COMMITTEE_BOAT);
        disabled.add(EXPEDITION_RACE_TIME_TO_PIN);
        disabled.add(EXPEDITION_RACE_TIME_TO_BURN_TO_PIN);
        disabled.add(EXPEDITION_RACE_TIME_TO_BURN_TO_COMMITTEE_BOAT);
        disabled.add(EXPEDITION_RACE_DISTANCE_TO_COMMITTEE_BOAT);
        disabled.add(EXPEDITION_RACE_DISTANCE_TO_PIN);
        disabled.add(EXPEDITION_RACE_LINE_SQUARE_FOR_WIND_DIRECTION);
        return disabled;
    }

    public static Collection<DetailType> getAllLegDetailColumnTypes() {
        final Collection<DetailType> all = new LinkedHashSet<>();
        all.addAll(getLegDetailColumnTypes());
        all.addAll(getLegBravoDetailTypes());
        all.addAll(getLegExpeditionDetailColumnTypes());
        return all;
    }

    /**
     * This method allows callers to also restore legacy names of older settings that do not match the renamed
     * detailtypes anymore. It matches {@code value} primarily to the literals; if no match is found, all
     * {@link #oldNames old names} are checked, and the first match, if any, is returned. If no match is found, an
     * {@link IllegalArgumentException} is thrown.
     */
    public static DetailType valueOfString(String value) {
        // fastpath for directly mappable values
        try {
            return DetailType.valueOf(value);
        } catch (IllegalArgumentException e) {
            // fallback for renamed legacy settings
            for (DetailType t : values()) {
                if (t.oldNames != null) {
                    for (String oldname : t.oldNames) {
                        if (oldname.equals(value)) {
                            return t;
                        }
                    }
                }
            }
        }
        throw new IllegalArgumentException("Could not restore " + value + " to an DetailType enum");
    }

    /**
     * Get a list of all defined {@link Action}s from all possible {@link DetailType}s).
     * 
     * @return a {@link Set} of all Actions which are defined on the {@link DetailType}s.
     */
    public static Set<Action> getAllPremiumActionsFromAll() {
        final Set<Action> premiumLeaderboardActions = new HashSet<>();
        for (DetailType detailType : DetailType.values()) {
            premiumLeaderboardActions.add(detailType.getPremiumAction());
        }
        return premiumLeaderboardActions;
    }

    /**
     * Get a list of all defined {@link Action}s from a list (mostly a subset of all possible {@link DetailType}s).
     * 
     * @param detailTypes
     *            a list of DetailType
     * @return a {@link Set} of all Actions which are defined on the given {@link DetailType}s.
     */
    public static Set<Action> getAllPremiumActionsFromSubset(Iterable<DetailType> detailTypes) {
        final Set<Action> premiumLeaderboardActions = new HashSet<>();
        for (DetailType detailType : detailTypes) {
            if (detailType.getPremiumAction() != null) {
                premiumLeaderboardActions.add(detailType.getPremiumAction());
            }
        }
        return premiumLeaderboardActions;
    }
}
