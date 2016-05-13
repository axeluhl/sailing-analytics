package com.sap.sailing.domain.base.impl;

import java.util.HashMap;

public class RaceColumnFactorImpl {

    private final String leaderboard_name;
    private final String leaderboard_display_name;
    private final HashMap<String, RaceColumn> race_columns;

    public RaceColumnFactorImpl(String leaderboard_name, String leaderboard_display_name) {
        this.leaderboard_name = leaderboard_name;
        this.leaderboard_display_name = leaderboard_display_name;
        race_columns = new HashMap<>();
    }

    public String getLeaderboardName() {
        return leaderboard_name;
    }

    public String getLeaderboardDisplayName() {
        return leaderboard_display_name;
    }

    public HashMap<String, RaceColumn> getRaceColumns() {
        return race_columns;
    }

    public void addRaceColumn(String race_column_name, Double explicit_factor, Double factor) {
        race_columns.put(race_column_name, new RaceColumn(race_column_name, explicit_factor, factor));
    }

    public class RaceColumn {

        private final String race_column_name;
        private final Double explicit_factor;
        private final Double factor;

        public RaceColumn(String race_column_name, Double explicit_factor, Double factor) {
            this.race_column_name = race_column_name;
            this.explicit_factor = explicit_factor;
            this.factor = factor;
        }

        public String getRaceColumnName() {
            return race_column_name;
        }

        public Double getExplicitFactor() {
            return explicit_factor;
        }

        public Double getFactor() {
            return factor;
        }
    }
}
