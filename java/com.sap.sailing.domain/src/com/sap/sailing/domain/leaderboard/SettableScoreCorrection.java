package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.TimePoint;

public interface SettableScoreCorrection extends ScoreCorrection {
    /**
     * The listeners added are transient and do not need to be {@link Serializable}. When this object is
     * de-serialized, it starts out with an empty set of listeners.
     */
    void addScoreCorrectionListener(ScoreCorrectionListener listener);
    
    void removeScoreCorrectionListener(ScoreCorrectionListener listener);

    /**
     * @param reason
     *            if <code>null</code>, any existing max-points reason is removed; while
     *            {@link #getMaxPointsReason(Competitor, RaceColumn, TimePoint)} will return {@link MaxPointsReason#NONE}, a
     *            call to {@link #isScoreCorrected(Competitor, RaceColumn, TimePoint)} will return <code>false</code> if no
     *            other explicit score correction was made for the <code>competitor</code>.
     */
    void setMaxPointsReason(Competitor competitor, RaceColumn raceColumn, MaxPointsReason reason);
    
    void correctScore(Competitor competitor, RaceColumn raceColumn, double points);

    /**
     * Removes a score correction which makes the competitor's score for <code>raceColumn</code> to fall back to the score
     * determined by the tracking data.
     */
    void uncorrectScore(Competitor competitor, RaceColumn raceColumn);
    
    /**
     * @return <code>null</code> if not set for the competitor, e.g., because no correction was made or only a
     *         {@link MaxPointsReason} but no explicit score was provided for the competitor.
     */
    Double getExplicitScoreCorrection(Competitor competitor, RaceColumn raceColumn);

    void setTimePointOfLastCorrectionsValidity(TimePoint timePointOfLastCorrectionsValidity);
    
    void setComment(String scoreCorrectionComment);
    
    void notifyListenersAboutCarriedPointsChange(Competitor competitor, Double oldCarriedPoints, Double newCarriedPoints);

    void notifyListenersAboutIsSuppressedChange(Competitor competitor, boolean suppressed);
    
    void notifyListenersAboutLastCorrectionsValidityChanged(TimePoint oldTimePointOfLastCorrectionsValidity, TimePoint newTimePointOfLastCorrectionsValidity);

    void notifyListenersAboutCommentChanged(String oldComment, String newComment);
}
