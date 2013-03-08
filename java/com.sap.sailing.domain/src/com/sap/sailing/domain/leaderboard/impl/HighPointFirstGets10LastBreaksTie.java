package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;

public class HighPointFirstGets10 extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = 1L;

    public HighPointFirstGets10() {
        super(10.0);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_FIRST_GETS_TEN;
    }
}
