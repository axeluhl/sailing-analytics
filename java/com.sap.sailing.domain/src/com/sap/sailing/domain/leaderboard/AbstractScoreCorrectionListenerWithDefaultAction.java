package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.TimePoint;

public abstract class AbstractScoreCorrectionListenerWithDefaultAction implements ScoreCorrectionListener {

    @Override
    public void correctedScoreChanged(Competitor competitor, RaceColumn raceColumn, Double oldCorrectedScore,
            Double newCorrectedScore) {
        defaultAction();
    }

    @Override
    public void incrementalScoreCorrectionChanged(Competitor competitor, RaceColumn raceColumn,
            Double oldScoreOffsetInPoints, Double newScoreOffsetInPoints) {
        defaultAction();
    }

    @Override
    public void maxPointsReasonChanged(Competitor competitor, RaceColumn raceColumn, MaxPointsReason oldMaxPointsReason,
            MaxPointsReason newMaxPointsReason) {
        defaultAction();
    }

    @Override
    public void carriedPointsChanged(Competitor competitor, Double oldCarriedPoints, Double newCarriedPoints) {
        defaultAction();
    }

    @Override
    public void isSuppressedChanged(Competitor competitor, boolean newIsSuppressed) {
        defaultAction();
    }

    @Override
    public void timePointOfLastCorrectionsValidityChanged(TimePoint oldTimePointOfLastCorrectionsValidity,
            TimePoint newTimePointOfLastCorrectionsValidity) {
        defaultAction();
    }

    @Override
    public void commentChanged(String oldComment, String newComment) {
        defaultAction();
    }

    abstract protected void defaultAction();
}
