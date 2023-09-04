package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;

public class LowPointWithEliminatingMedalSeriesPromotingOneToFinalAndTwoToSemifinal
        extends LowPointWithEliminatingMedalSeriesWithPromotions {
    private static final long serialVersionUID = -6753499035563730886L;

    public LowPointWithEliminatingMedalSeriesPromotingOneToFinalAndTwoToSemifinal() {
        super(new int[] { 2, 1 });
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_WITH_ELIMINATING_MEDAL_SERIES_PROMOTING_ONE_TO_FINAL_AND_TWO_TO_SEMIFINAL;
    }
}
