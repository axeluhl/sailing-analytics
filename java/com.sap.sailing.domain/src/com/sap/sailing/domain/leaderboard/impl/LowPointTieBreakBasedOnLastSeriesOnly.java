package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;

public class LowPointTieBreakBasedOnLastSeriesOnly extends LowPoint {
    private static final long serialVersionUID = -2837189757937229157L;

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_TIE_BREAK_BASED_ON_LAST_SERIES_ONLY;
    }
}
