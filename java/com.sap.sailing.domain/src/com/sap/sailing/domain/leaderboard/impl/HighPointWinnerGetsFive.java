package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;

public class HighPointWinnerGetsFive extends HighPointFirstGetsFixedScore {
	private static final long serialVersionUID = 1367473387431601943L;

	public HighPointWinnerGetsFive() {
        super(/* scoreForRaceWinner */ 5);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_WINNER_GETS_FIVE;
    }
}
