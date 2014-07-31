package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;

public class HighPointWinnerGetsFiveIgnoringRaceCount extends HighPointWinnerGetsFive {
    private static final long serialVersionUID = 3936338558024129174L;

    /**
     * Ignores the number of races scored, making the simple total points the first ranking criterion.
     */
    @Override
    public int compareByNumberOfRacesScored(int competitor1NumberOfRacesScored, int competitor2NumberOfRacesScored) {
        return 0;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_WINNER_GETS_FIVE_IGNORING_RACE_COUNT;
    }
}
