package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
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
     * An incremental score correction is applied when an absolute score correction set through
     * {@link #correctScore(Competitor, RaceColumn, double)} is not available or not (yet) applied, e.g., because the
     * {@link #getMaxPointsReason(Competitor, RaceColumn, TimePoint) invalid result marker (IRM)} suggests that the
     * score correction should be applied only, e.g., after the race and the time point for which the score is requested
     * is still before the competitor's finishing time.
     * <p>
     * 
     * For {@link LowPoint} schemes the {@code points} provided here will be <em>added</em> to a competitor's score as
     * obtained by the scoring scheme from the competitor's rank in the race. For {@link HighPoint} schemes it will be
     * subtracted.
     * <p>
     * 
     * Any {@link ScoreCorrectionListener} registered will be
     * {@link ScoreCorrectionListener#incrementalScoreCorrectionChanged(Competitor, RaceColumn, Double, Double)
     * notified}.
     */
    void correctScoreIncrementally(Competitor competitor, RaceColumn raceColumn, double scoreOffsetInPoints);

    /**
     * Removes an incremental score correction set with
     * {@link #correctScoreIncrementally(Competitor, RaceColumn, double)} which makes the competitor's live for
     * <code>raceColumn</code> to fall back to the score determined by the tracking data, unless an absolute
     * {@link #correctScore(Competitor, RaceColumn, double) score correction} has been provided that is to be applied
     * already for the query time point.
     * <p>
     * 
     * Any {@link ScoreCorrectionListener} registered will be
     * {@link ScoreCorrectionListener#incrementalScoreCorrectionChanged(Competitor, RaceColumn, Double, Double)
     * notified}.
     */
    void uncorrectScoreIncrementally(Competitor competitor, RaceColumn raceColumn);
    
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
