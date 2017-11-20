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
        case DISTANCE_TRAVELED_INCLUDING_GATE_START:
            return stringMessages.distanceIncludingGateStartInMeters();
        case AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.averageSpeedInKnots();
        case RANK_GAIN:
            return stringMessages.rankGain();
        case RACE_RANK:
            return stringMessages.rank();
        case REGATTA_RANK:
            return stringMessages.regattaRank();
        case OVERALL_RANK:
            return stringMessages.overallRank();
        case RACE_NET_POINTS:
            return stringMessages.netPoints();
        case REGATTA_NET_POINTS:
            return stringMessages.netPoints();            
        case REGATTA_NET_POINTS_SUM:
            return stringMessages.netPoints();            
        case NUMBER_OF_MANEUVERS:
            return stringMessages.numberOfManeuvers();
        case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.currentOrAverageSpeedOverGroundInKnots();
        case RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.currentSpeedOverGroundInKnots();
        case CURRENT_RIDE_HEIGHT_IN_METERS:
            return stringMessages.currentOrAverageRideHeightInMeters();
        case CURRENT_DISTANCE_FOILED_IN_METERS:
            return stringMessages.currentDistanceFoiledInMeters();
        case CURRENT_DURATION_FOILED_IN_SECONDS:
            return stringMessages.currentDurationFoiled();
        case CURRENT_HEEL_IN_DEGREES:
            return stringMessages.currentHeelInDegree();
        case CURRENT_PITCH_IN_DEGREES:
            return stringMessages.currentPitchInDegree();
        case RACE_CURRENT_RIDE_HEIGHT_IN_METERS:
            return stringMessages.currentRideHeightInMeters();
        case RACE_CURRENT_DURATION_FOILED_IN_SECONDS:
            return stringMessages.currentDurationFoiled();
        case RACE_CURRENT_DISTANCE_FOILED_IN_METERS:
            return stringMessages.currentDistanceFoiledInMeters();
        case CURRENT_PORT_DAGGERBOARD_RAKE:
            return stringMessages.currentPortDaggerboardRake();
        case CURRENT_STBD_DAGGERBOARD_RAKE:
            return stringMessages.currentStbdDaggerboardRake();
        case CURRENT_PORT_RUDDER_RAKE:
            return stringMessages.currentPortRudderRake();
        case CURRENT_STBD_RUDDER_RAKE:
            return stringMessages.currentStbdRudderRake();
        case CURRENT_MAST_ROTATION_IN_DEGREES:
            return stringMessages.currentMastRotationInDegree();
        case CURRENT_DEPTH_IN_METERS:
            return stringMessages.currentDepthInMeters();
        case CURRENT_DRIFT_IN_DEGREES:
            return stringMessages.currentDriftInDegrees();
        case CURRENT_LEEWAY_IN_DEGREES:
            return stringMessages.currentLeewayInDegrees();
        case CURRENT_RUDDER_IN_DEGREES:
            return stringMessages.currentRudderInDegrees();
        case CURRENT_SET:
            return stringMessages.currentSet();
        case CURRENT_TACK_ANGLE_IN_DEGREES:
            return stringMessages.currentTackAngleInDegrees();
        case CURRENT_DEFLECTOR_IN_MILLIMETERS:
            return stringMessages.currentDeflectorInMillimeters();
        case CURRENT_DEFLECTOR_PERCENTAGE:
            return stringMessages.currentDeflectorPercentage();
        case CURRENT_FORESTAY_LOAD:
            return stringMessages.currentForestayLoad();
        case CURRENT_FORESTAY_PRESSURE:
            return stringMessages.currentForestayPressure();
        case CURRENT_RAKE_IN_DEGREES:
            return stringMessages.currentRakeInDegrees();
        case CURRENT_TARGET_BOATSPEED_PERCENTAGE:
            return stringMessages.currentTargetBoatspeedPercentage();
        case CURRENT_TARGET_HEEL_ANGLE_IN_DEGREES:
            return stringMessages.currentTargetHeelAngleInDegrees();
        case ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS:
            return stringMessages.estimatedTimeToNextWaypointInSeconds();
        case VELOCITY_MADE_GOOD_IN_KNOTS:
            return stringMessages.velocityMadeGoodInKnots();
        case GAP_TO_LEADER_IN_SECONDS:
            return stringMessages.gapToLeaderInSeconds();
        case CORRECTED_TIME_TRAVELED:
            return stringMessages.calculatedTimeTraveled();
        case GAP_CHANGE_SINCE_LEG_START_IN_SECONDS:
            return stringMessages.gapChangeSinceLegStartInSeconds();
        case SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED:
            return stringMessages.sideToWhichMarkAtLegStartWasRounded();
        case WINDWARD_DISTANCE_TO_GO_IN_METERS:
            return stringMessages.windwardDistanceToGoInMeters();
        case RACE_DISTANCE_TRAVELED:
            return stringMessages.distanceInMeters();
        case RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START:
            return stringMessages.distanceIncludingGateStartInMeters();
        case RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.averageSpeedInKnots();
        case RACE_GAP_TO_LEADER_IN_SECONDS:
            return stringMessages.gapToLeaderInSeconds();
        case RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS:
            return stringMessages.windwardDistanceToCompetitorFarthestAheadInMeters();
        case RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.averageAbsoluteCrossTrackErrorInMeters();
        case RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.averageSignedCrossTrackErrorInMeters();
        case START_TACK:
            return stringMessages.startTack();
        case WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD:
            return stringMessages.windwardDistanceToCompetitorFarthestAheadInMeters();
        case DISTANCE_TO_START_AT_RACE_START:
            return stringMessages.distanceToLineAtRaceStart();
        case TIME_BETWEEN_RACE_START_AND_COMPETITOR_START:
            return stringMessages.timeBetweenRaceStartAndCompetitorStartInSeconds();
        case SPEED_OVER_GROUND_AT_RACE_START:
            return stringMessages.speedOverGroundAtRaceStart();
        case SPEED_OVER_GROUND_WHEN_PASSING_START:
            return stringMessages.speedOverGroundWhenPassingStart();
        case DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_PASSING_START_IN_METERS:
            return stringMessages.distanceToStarboardEndOfStartlineWhenPassingStart();
        case TACK:
            return stringMessages.tack();
        case JIBE:
            return stringMessages.jibe();
        case PENALTY_CIRCLE:
            return stringMessages.penaltyCircle();
        case DISPLAY_LEGS:
            return stringMessages.legs();
        case CURRENT_LEG:
            return stringMessages.currentLeg();
        case TIME_TRAVELED:
            return stringMessages.time();
        case AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.averageAbsoluteCrossTrackErrorInMeters();
        case AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.averageSignedCrossTrackErrorInMeters();
        case MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.maximumSpeedOverGroundInKnots();
        case TOTAL_DISTANCE_TRAVELED:
            return stringMessages.totalDistanceTraveled();
        case TIME_ON_TIME_FACTOR:
            return stringMessages.timeOnTimeFactor();
        case TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE:
            return stringMessages.timeOnDistanceAllowanceInSecondsPerNauticalMile();
        case TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS:
            return stringMessages.totalTimeSailedDownwindInSeconds();
        case TOTAL_TIME_SAILED_UPWIND_IN_SECONDS:
            return stringMessages.totalTimeSailedUpwindInSeconds();
        case TOTAL_TIME_SAILED_REACHING_IN_SECONDS:
            return stringMessages.totalTimeSailedReachingInSeconds();
        case TOTAL_TIME_SAILED_IN_SECONDS:
            return stringMessages.totalTimeSailedInSeconds();
        case TOTAL_AVERAGE_SPEED_OVER_GROUND:
            return stringMessages.totalAverageSpeedOverGround();
        case TOTAL_DURATION_FOILED_IN_SECONDS:
            return stringMessages.totalDurationFoiledInSeconds();
        case TOTAL_DISTANCE_FOILED_IN_METERS:
            return stringMessages.totalDistanceFoiledInMeters();
        case AVERAGE_MANEUVER_LOSS_IN_METERS:
            return stringMessages.averageManeuverLossInMeters();
        case AVERAGE_TACK_LOSS_IN_METERS:
            return stringMessages.averageTackLossInMeters();
        case AVERAGE_JIBE_LOSS_IN_METERS:
            return stringMessages.averageJibeLossInMeters();
        case RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL:
            return stringMessages.ratioBetweenTimeSinceLastPositionFixAndAverageSamplingInterval();
        case RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_RACE_START:
            return stringMessages.distanceToLineFiveSecondsBeforeStart();
        case RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START:
            return stringMessages.speedOverGroundFiveSecondsBeforeStart();
        case RACE_TIME_TRAVELED:
            return stringMessages.time();
        case RACE_CALCULATED_TIME_TRAVELED:
            return stringMessages.calculatedTimeTraveled();
        case RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD:
            return stringMessages.raceCalculatedTimeAtEstimatedArrivalAtCompetitorFarthestAheadInSeconds();
        case RACE_TIME_TRAVELED_DOWNWIND:
            return stringMessages.timeDownwind();
        case RACE_TIME_TRAVELED_REACHING:
            return stringMessages.timeReaching();
        case RACE_TIME_TRAVELED_UPWIND:
            return stringMessages.timeUpwind();
        case DISTANCE_TO_START_LINE:
            return stringMessages.distanceToLine();
        case BEAT_ANGLE:
            return stringMessages.beatAngle();
        case COURSE_OVER_GROUND_TRUE_DEGREES:
            return stringMessages.courseOverGroundTrueDegrees();
        }
        return null;
    }
    
    /**
     * Returns the unit of the given {@link DetailType}, like 'm', 'kts' or an empty string, if the detail type has no
     * unit.<br>
     * Throws an UnsupportedOperationException if the given detail type isn't supported.
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
        case TOTAL_AVERAGE_SPEED_OVER_GROUND:
        case SPEED_OVER_GROUND_AT_RACE_START:
        case SPEED_OVER_GROUND_WHEN_PASSING_START:
        case RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START:
            return stringMessages.knotsUnit();

        case CURRENT_DEFLECTOR_PERCENTAGE:
        case CURRENT_TARGET_BOATSPEED_PERCENTAGE:
            return stringMessages.percent();
        
        case WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD:
        case WINDWARD_DISTANCE_TO_GO_IN_METERS:
        case DISTANCE_TRAVELED:
        case DISTANCE_TRAVELED_INCLUDING_GATE_START:
        case RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS:
        case RACE_DISTANCE_TRAVELED:
        case RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START:
        case AVERAGE_TACK_LOSS_IN_METERS:
        case AVERAGE_JIBE_LOSS_IN_METERS:
        case AVERAGE_MANEUVER_LOSS_IN_METERS:
        case AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS:
        case AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS:
        case RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS:
        case RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS:
        case DISTANCE_TO_START_AT_RACE_START:
        case TOTAL_DISTANCE_TRAVELED:
        case DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_PASSING_START_IN_METERS:
        case RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_RACE_START:
        case DISTANCE_TO_START_LINE:
        case CURRENT_RIDE_HEIGHT_IN_METERS:
        case RACE_CURRENT_RIDE_HEIGHT_IN_METERS:
        case RACE_CURRENT_DISTANCE_FOILED_IN_METERS:
        case CURRENT_DEPTH_IN_METERS:
        case CURRENT_DISTANCE_FOILED_IN_METERS:
            return stringMessages.metersUnit();

        case CURRENT_DEFLECTOR_IN_MILLIMETERS:
            return stringMessages.millimetersUnit();
            
        case COURSE_OVER_GROUND_TRUE_DEGREES:
        case CURRENT_HEEL_IN_DEGREES:
        case CURRENT_PITCH_IN_DEGREES:
        case CURRENT_MAST_ROTATION_IN_DEGREES:
        case CURRENT_DRIFT_IN_DEGREES:
        case CURRENT_LEEWAY_IN_DEGREES:
        case CURRENT_RAKE_IN_DEGREES:
        case CURRENT_RUDDER_IN_DEGREES:
        case CURRENT_TACK_ANGLE_IN_DEGREES:
        case CURRENT_TARGET_HEEL_ANGLE_IN_DEGREES:
            return stringMessages.degreesShort();
        
        case CURRENT_PORT_DAGGERBOARD_RAKE:
        case CURRENT_STBD_DAGGERBOARD_RAKE:
        case CURRENT_PORT_RUDDER_RAKE:
        case CURRENT_STBD_RUDDER_RAKE:
        case CURRENT_SET:
            // We currently do not show a specific unit for these measures because these are specific "uninterpreted" values
            return "";

        case CURRENT_FORESTAY_LOAD:
        case CURRENT_FORESTAY_PRESSURE:
            return stringMessages.tonsUnit();
            
        case GAP_TO_LEADER_IN_SECONDS:
        case GAP_CHANGE_SINCE_LEG_START_IN_SECONDS:
        case RACE_GAP_TO_LEADER_IN_SECONDS:
        case ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS:
        case CORRECTED_TIME_TRAVELED:
        case RACE_CALCULATED_TIME_TRAVELED:
        case TIME_BETWEEN_RACE_START_AND_COMPETITOR_START:
        case RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD:
            return stringMessages.secondsUnit();

        case TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE:
            return stringMessages.secondsPerNauticalMileUnit();
            
        case RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL:
            return stringMessages.ratio();
            
        case TOTAL_TIME_SAILED_IN_SECONDS:
        case TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS:
        case TOTAL_TIME_SAILED_UPWIND_IN_SECONDS:
        case TOTAL_TIME_SAILED_REACHING_IN_SECONDS:
        case RACE_TIME_TRAVELED:
        case RACE_TIME_TRAVELED_DOWNWIND:
        case RACE_TIME_TRAVELED_REACHING:
        case RACE_TIME_TRAVELED_UPWIND:
        case TIME_TRAVELED:
        case CURRENT_DURATION_FOILED_IN_SECONDS:
        case RACE_CURRENT_DURATION_FOILED_IN_SECONDS:
            return stringMessages.hhmmssUnit();

        // Cases for detail types without unit, so that an empty string is returned.
        case RACE_RANK:
        case REGATTA_RANK:
        case OVERALL_RANK:
        case RACE_NET_POINTS:
        case REGATTA_NET_POINTS:
        case REGATTA_NET_POINTS_SUM:
        case CURRENT_LEG:
        case TACK:
        case START_TACK:
        case JIBE:
        case PENALTY_CIRCLE:
        case RANK_GAIN:
        case NUMBER_OF_MANEUVERS:
        case DISPLAY_LEGS:
        case SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED:
        case BEAT_ANGLE:
        case TIME_ON_TIME_FACTOR:
            return "";
        default:
            break;
        }
        // Throwing an exception to get notified if an implementation of
        // a detail type is missing.
        throw new UnsupportedOperationException("There is currently no support for the enum value '" + detailType
                + "' in this method.");
    }
    
    /**
     * Returns a tooltip text for the given detail type or an empty string, if there's none.
     * @param detailType
     * @return A tooltip string for the given detail type.
     */
    public static String getTooltip(DetailType detailType) {
        switch (detailType) {
        case AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.averageAbsoluteCrossTrackErrorInMetersTooltip();
        case AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.averageSignedCrossTrackErrorInMetersTooltip();
        case GAP_CHANGE_SINCE_LEG_START_IN_SECONDS:
            return stringMessages.gapChangeSinceLegStartInSecondsTooltip();
        case RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.raceAverageAbsoluteCrossTrackErrorInMetersTooltip();
        case RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS:
            return stringMessages.raceAverageSignedCrossTrackErrorInMetersTooltip();
        case SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED:
            return stringMessages.sideToWhichMarkAtLegStartWasRoundedTooltip();
        case VELOCITY_MADE_GOOD_IN_KNOTS:
            return stringMessages.velocityMadeGoodInKnotsTooltip();
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
        case DISTANCE_TRAVELED_INCLUDING_GATE_START:
            return stringMessages.distanceTraveledIncludingGateStartTooltip();
        case AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.averageSpeedInKnotsTooltip();
        case ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS:
            return stringMessages.estimatedTimeToNextWaypointInSecondsTooltip();
        case GAP_TO_LEADER_IN_SECONDS:
            return stringMessages.gapToLeaderInSecondsTooltip();
        case CORRECTED_TIME_TRAVELED:
            return stringMessages.calculatedTimeTraveledTooltip();
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
            return stringMessages.currentOrAverageSpeedOverGroundInKnotsTooltip();
        case RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
            return stringMessages.currentSpeedOverGroundInKnotsTooltip();
        case CURRENT_RIDE_HEIGHT_IN_METERS:
            return stringMessages.currentOrAverageRideHeightInMetersTooltip();
        case CURRENT_DISTANCE_FOILED_IN_METERS:
            return stringMessages.currentDistanceFoiledInMetersTooltip();
        case CURRENT_DURATION_FOILED_IN_SECONDS:
            return stringMessages.currentDurationFoiledTooltip();
        case RACE_CURRENT_RIDE_HEIGHT_IN_METERS:
            return stringMessages.currentRideHeightInMetersTooltip();
        case RACE_CURRENT_DURATION_FOILED_IN_SECONDS:
            return stringMessages.currentDurationFoiledTooltip();
        case RACE_CURRENT_DISTANCE_FOILED_IN_METERS:
            return stringMessages.currentDistanceFoiledInMetersTooltip();
        case CURRENT_PORT_DAGGERBOARD_RAKE:
            return stringMessages.currentPortDaggerboardRakeTooltip();
        case CURRENT_STBD_DAGGERBOARD_RAKE:
            return stringMessages.currentStbdDaggerboardRakeTooltip();
        case CURRENT_PORT_RUDDER_RAKE:
            return stringMessages.currentPortRudderRakeTooltip();
        case CURRENT_STBD_RUDDER_RAKE:
            return stringMessages.currentStbdRudderRakeTooltip();
        case CURRENT_MAST_ROTATION_IN_DEGREES:
            return stringMessages.currentMastRotationInDegreeTooltip();
        case CURRENT_DEPTH_IN_METERS:
            return stringMessages.currentDepthInMetersTooltip();
        case CURRENT_DRIFT_IN_DEGREES:
            return stringMessages.currentDriftInDegreesTooltip();
        case CURRENT_LEEWAY_IN_DEGREES:
            return stringMessages.currentLeewayInDegreesTooltip();
        case CURRENT_RUDDER_IN_DEGREES:
            return stringMessages.currentRudderInDegreesTooltip();
        case CURRENT_SET:
            return stringMessages.currentSetTooltip();
        case CURRENT_TACK_ANGLE_IN_DEGREES:
            return stringMessages.currentTackAngleInDegreesTooltip();
        case CURRENT_DEFLECTOR_IN_MILLIMETERS:
            return stringMessages.currentDeflectorInMillimetersTooltip();
        case CURRENT_DEFLECTOR_PERCENTAGE:
            return stringMessages.currentDeflectorPercentageTooltip();
        case CURRENT_FORESTAY_LOAD:
            return stringMessages.currentForestayLoadTooltip();
        case CURRENT_FORESTAY_PRESSURE:
            return stringMessages.currentForestayPressureTooltip();
        case CURRENT_RAKE_IN_DEGREES:
            return stringMessages.currentRakeInDegreesTooltip();
        case CURRENT_TARGET_BOATSPEED_PERCENTAGE:
            return stringMessages.currentTargetBoatspeedPercentageTooltip();
        case CURRENT_TARGET_HEEL_ANGLE_IN_DEGREES:
            return stringMessages.currentTargetHeelAngleInDegreesTooltip();
        case CURRENT_HEEL_IN_DEGREES:
            return stringMessages.currentHeelInDegreeTooltip();
        case CURRENT_PITCH_IN_DEGREES:
            return stringMessages.currentPitchDegreeTooltip();
        case RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS:
            return stringMessages.windwardDistanceToCompetitorFarthestAheadInMetersTooltip();
        case WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD:
            return stringMessages.windwardDistanceToCompetitorFarthestAheadInMetersTooltip();
        case DISTANCE_TO_START_AT_RACE_START:
            return stringMessages.distanceToLineAtRaceStartTooltip();
        case TIME_BETWEEN_RACE_START_AND_COMPETITOR_START:
            return stringMessages.timeBetweenRaceStartAndCompetitorStartInSecondsTooltip();
        case SPEED_OVER_GROUND_AT_RACE_START:
            return stringMessages.speedOverGroundAtRaceStartTooltip();
        case SPEED_OVER_GROUND_WHEN_PASSING_START:
            return stringMessages.speedOverGroundWhenPassingStartTooltip();
        case DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_PASSING_START_IN_METERS:
            return stringMessages.distanceToStarboardEndOfStartlineWhenPassingStartTooltip();
        case START_TACK:
            return stringMessages.startTackTooltip();
        case RACE_DISTANCE_TRAVELED:
            return stringMessages.raceDistanceTraveledTooltip();
        case RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START:
            return stringMessages.raceDistanceTraveledIncludingGateStartTooltip();
        case RACE_GAP_TO_LEADER_IN_SECONDS:
            return stringMessages.gapToLeaderInSecondsTooltip();
        case RACE_CALCULATED_TIME_TRAVELED:
            return stringMessages.calculatedTimeTraveledTooltip();
        case RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD:
            return stringMessages.raceCalculatedTimeAtEstimatedArrivalAtCompetitorFarthestAheadInSecondsTooltip();
        case RACE_RANK:
            return stringMessages.rankTooltip();
        case REGATTA_RANK:
            return stringMessages.regattaRankTooltip();
        case OVERALL_RANK:
            return stringMessages.overallRankTooltip();
        case RACE_NET_POINTS:
            return stringMessages.raceNetPointsTooltip();
        case REGATTA_NET_POINTS:
            return stringMessages.regattaNetPointsTooltip();            
        case REGATTA_NET_POINTS_SUM:
            return stringMessages.regattaNetPointsTooltip();            
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
        case TOTAL_DISTANCE_TRAVELED:
            return stringMessages.totalDistanceTraveledTooltip();
        case TOTAL_AVERAGE_SPEED_OVER_GROUND:
            return stringMessages.totalAverageSpeedOverGroundTooltip();
        case TOTAL_DURATION_FOILED_IN_SECONDS:
            return stringMessages.totalDurationFoiledInSecondsTooltip();
        case TOTAL_DISTANCE_FOILED_IN_METERS:
            return stringMessages.totalDistanceFoiledInMetersTooltip();
        case TIME_ON_TIME_FACTOR:
            return stringMessages.timeOnTimeFactorTooltip();
        case TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE:
            return stringMessages.timeOnDistanceAllowanceInSecondsPerNauticalMileTooltip();
        case WINDWARD_DISTANCE_TO_GO_IN_METERS:
            return stringMessages.windwardDistanceToGoInMetersTooltip();
        case RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL:
            return stringMessages.ratioBetweenTimeSinceLastPositionFixAndAverageSamplingIntervalTooltip();
        case RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_RACE_START:
            return stringMessages.distanceToLineFiveSecondsBeforeStartTooltip();
        case RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START:
            return stringMessages.speedOverGroundFiveSecondsBeforeStartTooltip();
        case RACE_TIME_TRAVELED:
            return stringMessages.raceTimeTooltip();
        case RACE_TIME_TRAVELED_DOWNWIND:
            return stringMessages.raceTimeDownwindTooltip();
        case RACE_TIME_TRAVELED_REACHING:
            return stringMessages.raceTimeReachingTooltip();
        case RACE_TIME_TRAVELED_UPWIND:
            return stringMessages.raceTimeUpwindTooltip();
        case BEAT_ANGLE:
            return stringMessages.beatAngleTooltip();
        case DISTANCE_TO_START_LINE:
            return "";
        case COURSE_OVER_GROUND_TRUE_DEGREES:
            return stringMessages.courseOverGroundTrueDegreesTooltip();
        }
        
        return "";
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
