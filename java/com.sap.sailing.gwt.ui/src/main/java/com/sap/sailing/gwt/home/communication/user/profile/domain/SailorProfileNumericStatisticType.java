package com.sap.sailing.gwt.home.communication.user.profile.domain;

import com.sap.sailing.gwt.ui.raceboard.RaceBoardModes;

public enum SailorProfileNumericStatisticType {
    MAX_SPEED(StatisticType.HIGHEST_IS_BEST, RaceBoardModes.PLAYER),
    BEST_DISTANCE_TO_START(StatisticType.LOWEST_IS_BEST, RaceBoardModes.START_ANALYSIS),
    BEST_STARTLINE_SPEED(StatisticType.HIGHEST_IS_BEST, RaceBoardModes.START_ANALYSIS),
    AVERAGE_STARTLINE_DISTANCE(StatisticType.AVERAGE, null);

    private StatisticType type;
    private RaceBoardModes mode;

    SailorProfileNumericStatisticType(StatisticType type, RaceBoardModes mode) {
        this.type = type;
        this.mode = mode;
    }

    public StatisticType getAggregationType() {
        return type;
    }

    public RaceBoardModes getPlayerMode() {
        return this.mode;
    }

    public static enum StatisticType {
        LOWEST_IS_BEST,
        HIGHEST_IS_BEST,
        AVERAGE
    }

}
