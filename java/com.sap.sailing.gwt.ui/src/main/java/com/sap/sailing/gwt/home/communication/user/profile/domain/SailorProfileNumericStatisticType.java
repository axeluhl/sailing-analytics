package com.sap.sailing.gwt.home.communication.user.profile.domain;

public enum SailorProfileNumericStatisticType {
    MAX_SPEED(StatisticType.HIGHEST_IS_BEST),
    BEST_DISTANCE_TO_START(StatisticType.LOWEST_IS_BEST),
    BEST_STARTLINE_SPEED(StatisticType.HIGHEST_IS_BEST),
    AVERAGE_STARTLINE_DISTANCE(StatisticType.AVERAGE);

    private StatisticType type;

    SailorProfileNumericStatisticType(StatisticType type) {
        this.type = type;
    }

    public StatisticType getAggregationType() {
        return type;
    }

    public static enum StatisticType {
        LOWEST_IS_BEST,
        HIGHEST_IS_BEST,
        AVERAGE
    }

    public String getPlayerMode() {
        return "PLAYER";
    }
}
