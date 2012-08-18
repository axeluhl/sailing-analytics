package com.sap.sailing.domain.leaderboard.meta;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;

public class MetaLeaderboardScoreCorrection extends ScoreCorrectionImpl {
    private static final long serialVersionUID = 3773423384260065869L;

    @Override
    protected void notifyListeners(Competitor competitor, Double oldCorrectedScore, Double newCorrectedScore) {
        super.notifyListeners(competitor, oldCorrectedScore, newCorrectedScore);
    }

    @Override
    protected void notifyListeners(Competitor competitor, MaxPointsReason oldMaxPointsReason,
            MaxPointsReason newMaxPointsReason) {
        super.notifyListeners(competitor, oldMaxPointsReason, newMaxPointsReason);
    }
}
