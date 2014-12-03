package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;

public class HighPointWinnerGetsEight extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = 330293087596268557L;

    public HighPointWinnerGetsEight() {
        super(/* scoreForRaceWinner */ 8);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_WINNER_GETS_EIGHT;
    }
}
