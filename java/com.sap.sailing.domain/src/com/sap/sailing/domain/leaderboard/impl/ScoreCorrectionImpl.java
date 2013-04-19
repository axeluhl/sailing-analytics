package com.sap.sailing.domain.leaderboard.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.ScoreCorrectionListener;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Implements the basic logic of assigning a maximum score to a competitor in a race if that competitor was
 * disqualified, did not start or did not finish. The maximum score is determined by counting the number of competitors
 * listed in the regatta to which the race belongs.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class ScoreCorrectionImpl implements SettableScoreCorrection {
    private static final long serialVersionUID = -7088305215528928135L;

    /**
     * If no max point reason is provided for a competitor/race, {@link MaxPointsReason#NONE} should be the default.
     */
    private final Map<Pair<Competitor, RaceColumn>, MaxPointsReason> maxPointsReasons;

    /**
     * If no score correction is provided here, the uncorrected points are the default.
     */
    private final Map<Pair<Competitor, RaceColumn>, Double> correctedScores;

    /**
     * If <code>null</code>, despite a non-<code>null</code> {@link #timePointOfLastCorrectionsValidity} value the
     * result have to be assumed to be preliminary and need to be displayed with a corresponding hint.
     */
    private String comment;

    /**
     * Tells when the score correction was last updated. This should usually be the "validity time" and not the
     * "transaction time." In other words, if scores provided by the race committee are updated to this score correction
     * at time X, and the race committee's scores are tagged with time Y, then this method should return Y, not X. If Y
     * is not available for some reason, X may be used as a default.
     */
    private TimePoint timePointOfLastCorrectionsValidity;

    private transient Set<ScoreCorrectionListener> scoreCorrectionListeners;

    public ScoreCorrectionImpl() {
        this.maxPointsReasons = new HashMap<Util.Pair<Competitor, RaceColumn>, MaxPointsReason>();
        this.correctedScores = new HashMap<Util.Pair<Competitor, RaceColumn>, Double>();
        this.scoreCorrectionListeners = new HashSet<ScoreCorrectionListener>();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.scoreCorrectionListeners = new HashSet<ScoreCorrectionListener>();
    }
    
    private Set<ScoreCorrectionListener> getScoreCorrectionListeners() {
        synchronized (scoreCorrectionListeners) {
            return new HashSet<ScoreCorrectionListener>(scoreCorrectionListeners);
        }
    }

    @Override
    public void addScoreCorrectionListener(ScoreCorrectionListener listener) {
        synchronized (scoreCorrectionListeners) {
            scoreCorrectionListeners.add(listener);
        }
    }

    @Override
    public void removeScoreCorrectionListener(ScoreCorrectionListener listener) {
        synchronized (scoreCorrectionListeners) {
            scoreCorrectionListeners.remove(listener);
        }
    }

    protected void notifyListeners(Competitor competitor, RaceColumn raceColumn, Double oldCorrectedScore, Double newCorrectedScore) {
        for (ScoreCorrectionListener listener : getScoreCorrectionListeners()) {
            listener.correctedScoreChanced(competitor, raceColumn, oldCorrectedScore, newCorrectedScore);
        }
    }

    protected void notifyListeners(Competitor competitor, MaxPointsReason oldMaxPointsReason,
            MaxPointsReason newMaxPointsReason) {
        for (ScoreCorrectionListener listener : getScoreCorrectionListeners()) {
            listener.maxPointsReasonChanced(competitor, oldMaxPointsReason, newMaxPointsReason);
        }
    }

    @Override
    public void notifyListenersAboutCarriedPointsChange(Competitor competitor, Double oldCarriedPoints, Double newCarriedPoints) {
        for (ScoreCorrectionListener listener : getScoreCorrectionListeners()) {
            listener.carriedPointsChanged(competitor, oldCarriedPoints, newCarriedPoints);
        }
    }

    @Override
    public void notifyListenersAboutIsSuppressedChange(Competitor competitor, boolean suppressed) {
        for (ScoreCorrectionListener listener : getScoreCorrectionListeners()) {
            listener.isSuppressedChanged(competitor, suppressed);
        }
    }

    @Override
    public void setMaxPointsReason(Competitor competitor, RaceColumn raceColumn, MaxPointsReason reason) {
        Pair<Competitor, RaceColumn> key = raceColumn.getKey(competitor);
        MaxPointsReason oldMaxPointsReason;
        if (reason == null) {
            oldMaxPointsReason = maxPointsReasons.remove(key);
        } else {
            oldMaxPointsReason = maxPointsReasons.put(key, reason);
        }
        notifyListeners(competitor, oldMaxPointsReason, reason);
    }

    @Override
    public void correctScore(Competitor competitor, RaceColumn raceColumn, double points) {
        Double oldScore = correctedScores.put(raceColumn.getKey(competitor), points);
        notifyListeners(competitor, raceColumn, oldScore, points);
    }

    @Override
    public boolean isScoreCorrected(Competitor competitor, RaceColumn raceColumn) {
        Pair<Competitor, RaceColumn> key = raceColumn.getKey(competitor);
        return correctedScores.containsKey(key) || maxPointsReasons.containsKey(key);
    }

    @Override
    public void uncorrectScore(Competitor competitor, RaceColumn raceColumn) {
        Double oldScore = correctedScores.remove(raceColumn.getKey(competitor));
        notifyListeners(competitor, raceColumn, oldScore, null);
    }

    @Override
    public MaxPointsReason getMaxPointsReason(Competitor competitor, RaceColumn raceColumn) {
        MaxPointsReason result = maxPointsReasons.get(raceColumn.getKey(competitor));
        if (result == null) {
            result = MaxPointsReason.NONE;
        }
        return result;
    }

    /**
     * If the {@link #getMaxPointsReason(Competitor, TrackedRace)} for the <code>competitor</code> for the
     * <code>raceColumn</code>'s tracked race is not {@link MaxPointsReason#NONE}, the
     * {@link #getMaxPoints(TrackedRace) maximum score} is computed for the competitor. Otherwise, the
     * <code>uncorrectedScore</code> is returned.
     * <p>
     */
    @Override
    public Result getCorrectedScore(Callable<Integer> trackedRankProvider, final Competitor competitor,
            final RaceColumn raceColumn, TimePoint timePoint, int numberOfCompetitorsInLeaderboard,
            ScoringScheme scoringScheme) {
        Double result;
        final MaxPointsReason maxPointsReason = getMaxPointsReason(competitor, raceColumn);
        if (maxPointsReason == MaxPointsReason.NONE) {
            result = getCorrectedNonMaxedScore(competitor, raceColumn, trackedRankProvider, scoringScheme, numberOfCompetitorsInLeaderboard);
        } else {
            // allow explicit override even when max points reason is specified; calculation may be wrong,
            // e.g., in case we have an untracked race and the number of competitors is estimated incorrectly
            Double correctedNonMaxedScore = correctedScores.get(raceColumn.getKey(competitor));
            if (correctedNonMaxedScore == null) {
                result = scoringScheme.getPenaltyScore(raceColumn, competitor, maxPointsReason,
                        getNumberOfCompetitorsInRace(raceColumn, competitor, numberOfCompetitorsInLeaderboard),
                        numberOfCompetitorsInLeaderboard);
            } else {
                result = correctedNonMaxedScore;
            }
        }
        final Double correctedScore = result;
        return new Result() {
            @Override
            public MaxPointsReason getMaxPointsReason() {
                return maxPointsReason;
            }

            @Override
            public Double getCorrectedScore() {
                return correctedScore;
            }

            @Override
            public boolean isCorrected() {
                return isScoreCorrected(competitor, raceColumn);
            }
        };
    }

    protected Integer getNumberOfCompetitorsInRace(RaceColumn raceColumn, Competitor competitor, int numberOfCompetitorsInLeaderboard) {
        Integer result;
        final TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
        if (trackedRace == null) {
            result = numberOfCompetitorsInLeaderboard;
        } else {
            result = Util.size(trackedRace.getRace().getCompetitors());
        }
        return result;
    }

    /**
     * Under the assumption that the competitor is not assigned the maximum score due to disqualification or other
     * reasons, computes the corrected score. If {@link #correctedScores} contains an entry for the
     * <code>competitor</code>'s key, it is used. Otherwise, the <code>uncorrectedScore</code> is returned.
     * @param scoringScheme
     *            used to transform the tracked rank into a score if there is no score correction applied
     * 
     * @return <code>null</code> in case the <code>competitor</code> has no score assigned in that race which is the
     * case if the score is not corrected by these score corrections, and the <code>trackedRankProvider</code> delivers 0
     * as the rank, or if the score is not corrected and the scoring scheme cannot find the competitor in any tracked race
     * of the <code>raceColumn</code>, meaning there cannot be a tracked rank for the competitor regardless what
     * <code>trackedRankProvider</code> delivers.
     */
    protected Double getCorrectedNonMaxedScore(Competitor competitor, RaceColumn raceColumn,
            Callable<Integer> trackedRankProvider, ScoringScheme scoringScheme, int numberOfCompetitorsInLeaderboard) {
        Double correctedNonMaxedScore = correctedScores.get(raceColumn.getKey(competitor));
        Double result;
        if (correctedNonMaxedScore == null) {
            try {
                int trackedRank = trackedRankProvider.call();
                result = scoringScheme.getScoreForRank(raceColumn, competitor, trackedRank,
                        getNumberOfCompetitorsInRace(raceColumn, competitor, numberOfCompetitorsInLeaderboard));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            result = correctedNonMaxedScore;
        }
        return result;
    }

    @Override
    public Double getExplicitScoreCorrection(Competitor competitor, RaceColumn raceColumn) {
        return correctedScores.get(raceColumn.getKey(competitor));
    }

    @Override
    public boolean hasCorrectionFor(RaceColumn raceInLeaderboard) {
        for (Pair<Competitor, RaceColumn> correctedScoresKey : correctedScores.keySet()) {
            if (correctedScoresKey.getB() == raceInLeaderboard) {
                return true;
            }
        }
        for (Pair<Competitor, RaceColumn> maxPointsReasonsKey : maxPointsReasons.keySet()) {
            if (maxPointsReasonsKey.getB() == raceInLeaderboard) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TimePoint getTimePointOfLastCorrectionsValidity() {
        return timePointOfLastCorrectionsValidity;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setTimePointOfLastCorrectionsValidity(TimePoint timePointOfLastCorrectionsValidity) {
        this.timePointOfLastCorrectionsValidity = timePointOfLastCorrectionsValidity;
    }

    @Override
    public void setComment(String scoreCorrectionComment) {
        this.comment = scoreCorrectionComment;
    }

}
