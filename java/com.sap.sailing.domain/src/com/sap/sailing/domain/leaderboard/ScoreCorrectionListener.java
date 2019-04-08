package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.TimePoint;

public interface ScoreCorrectionListener {
    void correctedScoreChanged(Competitor competitor, RaceColumn raceColumn, Double oldCorrectedScore, Double newCorrectedScore);

    void maxPointsReasonChanged(Competitor competitor, RaceColumn raceColumn, MaxPointsReason oldMaxPointsReason, MaxPointsReason newMaxPointsReason);
    
    void carriedPointsChanged(Competitor competitor, Double oldCarriedPoints, Double newCarriedPoints);
    
    void isSuppressedChanged(Competitor competitor, boolean newIsSuppressed);
    
    void timePointOfLastCorrectionsValidityChanged(TimePoint oldTimePointOfLastCorrectionsValidity, TimePoint newTimePointOfLastCorrectionsValidity);
    
    void commentChanged(String oldComment, String newComment);
}
