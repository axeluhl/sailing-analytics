package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;

public class HighPointWinnerGetsSix extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = -4116295702464748719L;

    public HighPointWinnerGetsSix() {
        super(/* scoreForRaceWinner */ 6);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_WINNER_GETS_SIX;
    }
}
