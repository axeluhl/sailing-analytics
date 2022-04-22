package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;

/**
 * Like {@link LowPointFirstToWinTwoRaces}, but three races are needed to win.
 */
public class LowPointFirstToWinThreeRaces extends LowPointFirstToWinTwoRaces {
    private static final long serialVersionUID = 7072175334160798617L;

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_FIRST_TO_WIN_THREE_RACES;
    }

    @Override
    public int getTargetAmountOfMedalRaceWins() {
        return 3;
    }
}
