package com.sap.sailing.selenium.pages.leaderboard;

public enum DetailCheckboxInfo {

    //Overall details
    REGATTA_RANK("RegattaRankCheckBox", "Regatta Rank"), TOTAL_DISTANCE("TotalDistanceTraveledCheckBox", "Total distance"), TOTAL_AVERAGE_SPEED_OVER_GROUND("TotalAverageSpeedOverGroundCheckBox", "∅ SOG"), TOTAL_TIME("TotalTimeSailedInSecondsCheckBox", "Total time"), MAXIMUM_SPEED_OVER_GROUND("MaximumSpeedOverGroundInKnotsCheckBox", "Max SOG"), TIME_ON_TIME_FACTOR("TimeOnTimeFactorCheckBox", "ToT Factor"), TIME_ON_DISTANCE_ALLOWANCE("TimeOnDistanceAllowanceInSecondsPerNauticalMileCheckBox", "ToD Allowance"),
    
    //Race details
    RACE_GAP_TO_LEADER("RaceGapToLeaderInSecondsCheckBox", "Gap to leader"), RACE_AVERAGE_SPEED_OVER_GROUND("RaceAverageSpeedOverGroundInKnotsCheckBox", "∅ Speed"), RACE_DISTANCE("RaceDistanceTraveledCheckBox", "Distance"), RACE_DISTANCE_INCLUDING_GATE_START("RaceDistanceTraveledIncludingGateStartCheckBox", "Distance (w/ Gate Start)"), RACE_TIME("RaceTimeTraveledCheckBox", "Time"), RACE_CALCULATED_TIME("RaceCalculatedTimeTraveledCheckBox", "Calculated Time"), RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD("RaceCalculatedTimeAtEstimatedArrivalAtCompetitorFarthestAheadCheckBox", "Calc.Time @ Fastest"), RACE_CURRENT_SPEED_OVER_GROUND("RaceCurrentSpeedOverGroundInKnotsCheckBox", "SOG"), RACE_CURRENT_RIDE_HEIGHT("RaceCurrentRideHeightInMetersCheckBox", "Ride Height"), RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD("RaceDistanceToCompetitorFarthestAheadInMetersCheckBox", "Gap"), NUMBER_OF_MANEUVERS("NumberOfManeuversCheckBox", "Maneuvers"), DISPLAY_LEGS("DisplayLegsCheckBox", "Legs"), CURRENT_LEG("CurrentLegCheckBox", "On Leg"), RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR("RaceAverageAbsoluteCrossTrackErrorInMetersCheckBox", "∅ XTE"), RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR("RaceAverageSignedCrossTrackErrorInMetersCheckBox", "∅ XTE +/-"), RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL("RaceRatioBetweenTimeSinceLastPositionFixAndAverageSamplingIntervalCheckBox", "GPS Lag"), 
    
    //Race Start Analysis
    RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_START("RaceDistanceToStartFiveSecondsBeforeRaceStartCheckBox", "Distance to line 5s before start"), RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START("RaceSpeedOverGroundFiveSecondsBeforeStartCheckBox", "SOG 5s before start"), DISTANCE_TO_START_AT_RACE_START("DistanceToStartAtRaceStartCheckBox", "Distance to line at Start"), TIME_BETWEEN_RACE_START_AND_COMPETITOR_START("TimeBetweenRaceStartAndCompetitorStartCheckBox", "Start delay"), SPEED_OVER_GROUND_AT_RACE_START("SpeedOverGroundAtRaceStartCheckBox", "SOG at Start"), SPEED_OVER_GROUND_WHEN_STARTING("SpeedOverGroundWhenPassingStartCheckBox", "SOG when Starting"), DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_STARTING("DistanceToStarboardEndOfStartlineWhenPassingStartInMetersCheckBox", "Dist. Stb-Side of Line when Starting"), START_TACK("StartTackCheckBox", "Start Tack"), 
    
    //Leg Details
    AVERAGE_SPEED_OVER_GROUND("AverageSpeedOverGroundInKnotsCheckBox", "∅ Speed"), DISTANCE("DistanceTraveledCheckBox", "Distance"), DISTANCE_INCLUDING_START("DistanceTraveledIncludingGateStartCheckBox", "Distance (w/ Gate Start)"), GAP_TO_LEADER("GapToLeaderInSecondsCheckBox", "Gap to leader"), GAP_CHANGE_SINCE_LEG_START("GapChangeSinceLegStartInSecondsCheckBox", "Gap Change"), SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED("SideToWhichMarkAtLegStartWasRoundedCheckBox", "Mark rounded to"), CURRENT_SPEED_OVER_GROUND("CurrentSpeedOverGroundInKnotsCheckBox", "SOG (∅ at end)"), CURRENT_RIDE_HEIGHT("CurrentRideHeightInMetersCheckBox", "Ride Height (∅ at end)"), WINDWARD_DISTANCE_TO_GO("WindwardDistanceToGoInMetersCheckBox", "Windward distance to go"), NUMBER_OF_MANEVEURS("NumberOfManeuversCheckBox", "Maneuvers"), ESTIMATED_TIME_TO_NEXT_WAYPOINT("EstimatedTimeToNextWaypointInSecondsCheckBox", "ETA"), VELOCITY_MADE_GOOD("VelocityMadeGoodInKnotsCheckBox", "VMG"), TIME("TimeTraveledCheckBox", "Time"), CORRECTED_TIME("CorrectedTimeTraveledCheckBox", "Calculated Time"), AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR("AverageAbsoluteCrossTrackErrorInMetersCheckBox", "∅ XTE"), AVERAGE_SIGNED_CROSS_TRACK_ERROR("AverageSignedCrossTrackErrorInMetersCheckBox", "∅ XTE +/-"), RANK_GAIN("RankGainCheckBox", "Rank Gain"),
    
    //Maneuvers
    TACK("TackCheckBox", "Tack"), AVERAGE_TACK_LOSS("AverageTackLossInMetersCheckBox", "∅ Tack Loss"), JIBE("JibeCheckBox", "Jibe"), AVERAGE_JIBE_LOSS("AverageJibeLossInMetersCheckBox", "∅ Jibe Loss"), PENALTY_CIRCLE("PenaltyCircleCheckBox", "Penalty circle"), AVERAGE_MANEUVER_LOSS("AverageManeuverLossInMetersCheckBox", "∅ Maneuver Loss");
    
    private final String id;
    private final String label;
    
    private DetailCheckboxInfo(String id, String label) {
        this.id = id;
        this.label = label;
    }
    
    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
}
