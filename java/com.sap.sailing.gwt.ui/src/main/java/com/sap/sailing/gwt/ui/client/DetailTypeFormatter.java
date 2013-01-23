package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.domain.common.DetailType;

public class DetailTypeFormatter {
    
    private DetailTypeFormatter() { }
	
    private static final StringMessages stringMessages = GWT.create(StringMessages.class);
	
    public static String format(DetailType detailType) {
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
        case GAP_CHANGE_SINCE_LEG_START_IN_SECONDS:
            return stringMessages.gapChangeSinceLegStartInSeconds();
        case SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED:
            return stringMessages.sideToWhichMarkAtLegStartWasRounded();
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
            return stringMessages.windwardDistanceToLeaderInMeters();
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
        case TOTAL_TIME_SAILED_UPWIND_IN_SECONDS:
            return stringMessages.totalTimeSailedUpwindInSeconds();
        case TOTAL_TIME_SAILED_REACHING_IN_SECONDS:
            return stringMessages.totalTimeSailedReachingInSeconds();
        case TOTAL_TIME_SAILED_IN_SECONDS:
            return stringMessages.totalTimeSailedInSeconds();
        case AVERAGE_MANEUVER_LOSS_IN_METERS:
            return stringMessages.averageManeuverLossInMeters();
        case AVERAGE_TACK_LOSS_IN_METERS:
            return stringMessages.averageTackLossInMeters();
        case AVERAGE_JIBE_LOSS_IN_METERS:
            return stringMessages.averageJibeLossInMeters();
        }
        return null;
    }
    
    /**
     * Returns the unit of the given {@link DetailType}, like 'm', 'kts' or an empty string, if the detail type has no
     * unit.<br>
     * Throws an UnsupportedOperationException if the given detail type isn't supported.<br>
     * Currently <b>not</b> supported are:<br>
     * <ul>
     * <li>BEAR_AWAY</li>
     * <li>HEAD_UP</li>
     * <li>MARK_PASSING</li>
     * </ul>
     * 
     * @param detailType
     * @return The unit of the detail type as string.
     */
    public static String getUnit(DetailType detailType) {
        switch (detailType) {
        case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
        case RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
        case MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS:
        case RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
        case VELOCITY_MADE_GOOD_IN_KNOTS:
        case AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.knotsUnit();

        case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
        case WINDWARD_DISTANCE_TO_GO_IN_METERS:
        case DISTANCE_TRAVELED:
        case RACE_DISTANCE_TO_LEADER_IN_METERS:
        case RACE_DISTANCE_TRAVELED:
        case AVERAGE_TACK_LOSS_IN_METERS:
        case AVERAGE_JIBE_LOSS_IN_METERS:
        case AVERAGE_MANEUVER_LOSS_IN_METERS:
        case AVERAGE_CROSS_TRACK_ERROR_IN_METERS:
        case RACE_AVERAGE_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.metersUnit();

        case GAP_TO_LEADER_IN_SECONDS:
        case GAP_CHANGE_SINCE_LEG_START_IN_SECONDS:
        case RACE_GAP_TO_LEADER_IN_SECONDS:
        case ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS:
        case TIME_TRAVELED:
            return stringMessages.secondsUnit();

        case TOTAL_TIME_SAILED_IN_SECONDS:
        case TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS:
        case TOTAL_TIME_SAILED_UPWIND_IN_SECONDS:
        case TOTAL_TIME_SAILED_REACHING_IN_SECONDS:
            return stringMessages.hhmmssUnit();

        // Cases for detail types without unit, so that an empty string is returned.
        case RACE_RANK:
        case CURRENT_LEG:
        case TACK:
        case JIBE:
        case PENALTY_CIRCLE:
        case RANK_GAIN:
        case NUMBER_OF_MANEUVERS:
        case DISPLAY_LEGS:
        case SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED:
            return "";

        default:
            // Throwing an exception to get notificated if an implementation of
            // an detail type is missing.
            throw new UnsupportedOperationException("Theres currently no support for the enum value '" + detailType
                    + "' in this method.");
        }
    }
    
    /**
     * Returns a tooltip text for the given detail type or
     * throws an UnsupportedOperationException if it isn't supported.
     * @param detailType
     * @return A tooltip string for the given detail type.
     */
    public static String getTooltip(DetailType detailType) {
        switch (detailType) {
        case AVERAGE_CROSS_TRACK_ERROR_IN_METERS:
            break;
        case BEAR_AWAY:
            break;
        case GAP_CHANGE_SINCE_LEG_START_IN_SECONDS:
            break;
        case HEAD_UP:
            break;
        case MARK_PASSING:
            break;
        case RACE_AVERAGE_CROSS_TRACK_ERROR_IN_METERS:
            break;
        case SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED:
            break;
        case AVERAGE_JIBE_LOSS_IN_METERS:
            return stringMessages.averageJibeLossInMetersTooltip();
        case AVERAGE_MANEUVER_LOSS_IN_METERS:
            return stringMessages.averageManeuverLossInMetersTooltip();
        case AVERAGE_TACK_LOSS_IN_METERS:
            return stringMessages.averageTackLossInMetersTooltip();
        case CURRENT_LEG:
            return stringMessages.currentLegTooltip();
        case DISPLAY_LEGS:
            return "";
        case DISTANCE_TRAVELED:
            return stringMessages.distanceTraveledTooltip();
        case AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.averageSpeedInKnotsTooltip();
        case ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS:
            return stringMessages.estimatedTimeToNextWaypointInSecondsTooltip();
        case GAP_TO_LEADER_IN_SECONDS:
            return stringMessages.gapToLeaderInSecondsTooltip();
        case JIBE:
            return stringMessages.jibeTooltip();
        case MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.maximumSpeedOverGroundInKnotsTooltip();
        case NUMBER_OF_MANEUVERS:
            return stringMessages.numberOfManeuversTooltip();
        case PENALTY_CIRCLE:
            return stringMessages.penaltyCircleTooltip();
        case RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.raceAverageSpeedInKnotsTooltip();
        case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.currentSpeedOverGroundInKnotsTooltip();
        case RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.currentSpeedOverGroundInKnotsTooltip();
        case RACE_DISTANCE_TO_LEADER_IN_METERS:
            return stringMessages.windwardDistanceToLeaderInMetersTooltip();
        case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
            return stringMessages.windwardDistanceToLeaderInMetersTooltip();
        case RACE_DISTANCE_TRAVELED:
            return stringMessages.raceDistanceTraveledTooltip();
        case RACE_GAP_TO_LEADER_IN_SECONDS:
            return stringMessages.gapToLeaderInSecondsTooltip();
        case RACE_RANK:
            return stringMessages.rankTooltip();
        case RANK_GAIN:
            return stringMessages.rankGainTooltip();
        case TACK:
            return stringMessages.tackTooltip();
        case TIME_TRAVELED:
            return stringMessages.timeTooltip();
        case TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS:
            return stringMessages.totalTimeSailedDownwindInSecondsTooltip();
        case TOTAL_TIME_SAILED_IN_SECONDS:
            return stringMessages.totalTimeSailedInSecondsTooltip();
        case TOTAL_TIME_SAILED_REACHING_IN_SECONDS:
            return stringMessages.totalTimeSailedReachingInSecondsTooltip();
        case TOTAL_TIME_SAILED_UPWIND_IN_SECONDS:
            return stringMessages.totalTimeSailedUpwindInSecondsTooltip();
        case WINDWARD_DISTANCE_TO_GO_IN_METERS:
            return stringMessages.windwardDistanceToGoInMetersTooltip();
            
        default:
            // Throwing an exception to get notificated if an implementation of
            // an detail type is missing.
//            throw new UnsupportedOperationException("Theres currently no support for the enum value '" + detailType
//                    + "' in this method.");
            return "Unsupported";
        }
        
        return "Empty Tooltip";
    }

    public static NumberFormat getNumberFormat(DetailType detailType) {
        String decimalPlaces = "";
        for (int i = 0; i < detailType.getPrecision(); i++) {
            if (i == 0) {
                decimalPlaces += ".";
            }
            decimalPlaces += "0";
        }
        return NumberFormat.getFormat("0" + decimalPlaces);
    }
}
