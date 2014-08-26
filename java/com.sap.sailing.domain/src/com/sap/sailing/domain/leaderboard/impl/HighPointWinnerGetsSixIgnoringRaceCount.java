package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;

public class HighPointWinnerGetsSixIgnoringRaceCount extends HighPointWinnerGetsSix {
    private static final long serialVersionUID = -6974180846946052247L;

    /**
     * Ignores the number of races scored, making the simple total points the first ranking criterion.
     */
    @Override
    public int compareByNumberOfRacesScored(int competitor1NumberOfRacesScored, int competitor2NumberOfRacesScored) {
        return 0;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_WINNER_GETS_SIX_IGNORING_RACE_COUNT;
    }
}
