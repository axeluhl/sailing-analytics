package com.sap.sailing.gwt.home.communication.user.profile.domain;

import com.sap.sailing.gwt.ui.raceboard.RaceBoardModes;

/** current types of statistics currently supported in the SailorProfiles and the corresponding backend service */
public enum SailorProfileNumericStatisticType {
    MAX_SPEED(StatisticType.HIGHEST_IS_BEST, RaceBoardModes.PLAYER, false), 
    BEST_DISTANCE_TO_START(StatisticType.LOWEST_IS_BEST, RaceBoardModes.START_ANALYSIS, true), 
    BEST_STARTLINE_SPEED(StatisticType.HIGHEST_IS_BEST, RaceBoardModes.START_ANALYSIS, false), 
    AVERAGE_STARTLINE_DISTANCE(StatisticType.AVERAGE, null, true),
    
//    // added
    AVERAGE_STARTLINE_DISTANCE_WITH_VALIDATION(StatisticType.AVERAGE, null, true),
    FIELD_AVERAGE_STARTLINE_DISTANCE_WITH_VALIDATION(StatisticType.AVERAGE, null, true);
//    AVERAGE_VMG(StatisticType.AVERAGE, null);
//    // added

    
    private StatisticType type;
    private RaceBoardModes mode;
    // added
    private boolean lowerIsBetter;

    SailorProfileNumericStatisticType(StatisticType type, RaceBoardModes mode, boolean lowerIsBetter) {
        this.type = type;
        this.mode = mode;
        // added 
        this.lowerIsBetter = lowerIsBetter;
    }

    public StatisticType getAggregationType() {
        return type;
    }

    public RaceBoardModes getPlayerMode() {
        return this.mode;
    }
    
    // added
    public boolean isLowerIsBetter() {  
        return lowerIsBetter;
    }

    public static enum StatisticType {
        LOWEST_IS_BEST, HIGHEST_IS_BEST, AVERAGE
    }

}
