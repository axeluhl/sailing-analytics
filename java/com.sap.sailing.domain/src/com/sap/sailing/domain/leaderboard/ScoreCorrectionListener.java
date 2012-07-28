package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;

public interface ScoreCorrectionListener {
    void correctedScoreChanced(Competitor competitor, Double oldCorrectedScore, Double newCorrectedScore);

    void maxPointsReasonChanced(Competitor competitor, MaxPointsReason oldMaxPointsReason, MaxPointsReason newMaxPointsReason);
}
