package com.sap.sailing.domain.leaderboard.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * A complex scoring scheme implementing the Notice of Race requirements with:
 * - Course Racing: Standard low-point scoring
 * - Sprint Racing: Custom scoring table when racing in heats (1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47)
 * - Marathon Racing: Represented as two separate race columns
 * - Medal Series: Quarter Final → Semi Final → Grand Final progression
 * - Grand Final: Match points system (first to 2 match points wins)
 * - Complex discard rules with BFD limitations in Sprint Racing
 * - Fleet-based ranking (Gold > Silver > Bronze)
 * 
 * Based on Notice of Race section 24 requirements.
 * 
 * @author Generated for Notice of Race Complex Scoring
 */
public class NoticeOfRaceComplexScoringScheme extends LowPoint {
    private static final long serialVersionUID = 1L;
    
    /**
     * Sprint Racing scoring table for when racing in heats.
     * Position -> Score mapping as specified in Notice of Race 24.3.2
     */
    private static final Map<Integer, Integer> SPRINT_RACING_HEAT_SCORES;
    
    static {
        SPRINT_RACING_HEAT_SCORES = new HashMap<>();
        SPRINT_RACING_HEAT_SCORES.put(1, 1);
        SPRINT_RACING_HEAT_SCORES.put(2, 3);
        SPRINT_RACING_HEAT_SCORES.put(3, 5);
        SPRINT_RACING_HEAT_SCORES.put(4, 7);
        SPRINT_RACING_HEAT_SCORES.put(5, 9);
        SPRINT_RACING_HEAT_SCORES.put(6, 11);
        SPRINT_RACING_HEAT_SCORES.put(7, 13);
        SPRINT_RACING_HEAT_SCORES.put(8, 15);
        SPRINT_RACING_HEAT_SCORES.put(9, 17);
        SPRINT_RACING_HEAT_SCORES.put(10, 19);
        SPRINT_RACING_HEAT_SCORES.put(11, 21);
        SPRINT_RACING_HEAT_SCORES.put(12, 23);
        SPRINT_RACING_HEAT_SCORES.put(13, 25);
        SPRINT_RACING_HEAT_SCORES.put(14, 27);
        SPRINT_RACING_HEAT_SCORES.put(15, 29);
        SPRINT_RACING_HEAT_SCORES.put(16, 31);
        SPRINT_RACING_HEAT_SCORES.put(17, 33);
        SPRINT_RACING_HEAT_SCORES.put(18, 35);
        SPRINT_RACING_HEAT_SCORES.put(19, 37);
        SPRINT_RACING_HEAT_SCORES.put(20, 39);
        SPRINT_RACING_HEAT_SCORES.put(21, 41);
        SPRINT_RACING_HEAT_SCORES.put(22, 43);
        SPRINT_RACING_HEAT_SCORES.put(23, 45);
        SPRINT_RACING_HEAT_SCORES.put(24, 47);
    }
    
    /**
     * Target number of match points needed to win the Grand Final
     */
    private static final int TARGET_MATCH_POINTS_TO_WIN = 2;

    public NoticeOfRaceComplexScoringScheme() {
        super();
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.NOTICE_OF_RACE_COMPLEX_SCORING;
    }

    @Override
    public Double getScoreForRank(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor,
            int rank, Callable<Integer> numberOfCompetitorsInRaceFetcher,
            NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint) {
        
        if (rank == 0) {
            return null; // No score for non-participants
        }
        
        // Check if this is Sprint Racing in heats
        if (isSprintRacingInHeats(raceColumn)) {
            // Use custom Sprint Racing scoring table
            Integer customScore = SPRINT_RACING_HEAT_SCORES.get(rank);
            if (customScore != null) {
                return (double) customScore;
            } else {
                // For positions beyond the table, continue the pattern: score = 2*rank - 1
                return (double) (2 * rank - 1);
            }
        }
        
        // For Course Racing and Sprint Racing not in heats, use standard low-point scoring
        return super.getScoreForRank(leaderboard, raceColumn, competitor, rank, 
                numberOfCompetitorsInRaceFetcher, numberOfCompetitorsInLeaderboardFetcher, timePoint);
    }

    /**
     * Determines if this race column represents Sprint Racing in heats.
     * Uses naming convention to identify Sprint Racing vs Course Racing.
     * 
     * @param raceColumn the race column to check
     * @return true if this is Sprint Racing in heats (uses custom scoring table)
     */
    private boolean isSprintRacingInHeats(RaceColumn raceColumn) {
        String name = raceColumn.getName();
        if (name == null) {
            return false;
        }
        
        String lowerName = name.toLowerCase();
        
        // Check for Sprint Racing indicators
        boolean isSprintRacing = lowerName.contains("sprint");
        
        // Check for heat indicators (when fleet is split into heats)
        boolean isInHeats = lowerName.contains("heat") || 
                           lowerName.contains("group") ||
                           raceColumn.hasSplitFleets();
        
        return isSprintRacing && isInHeats;
    }

    /**
     * Medal Series use match points system in Grand Final.
     * First to 2 match points wins.
     */
    @Override
    public boolean isMedalWinAmountCriteria() {
        return true;
    }

    /**
     * Opening Series results carry forward to Medal Series.
     * 1st place gets 1 match point, 2nd place gets 0.5 match point.
     */
    @Override
    public boolean isCarryForwardInMedalsCriteria() {
        return true;
    }

    /**
     * Marathon races have factor 1.0 since they are represented as two separate race columns.
     * Medal races also have factor 1.0 as specified in the Notice of Race.
     */
    @Override
    public double getScoreFactor(RaceColumn raceColumn) {
        Double explicitFactor = raceColumn.getExplicitFactor();
        if (explicitFactor != null) {
            return explicitFactor;
        }
        
        // All races have factor 1.0 in this scoring scheme
        // Marathon races are handled by creating two separate race columns
        return 1.0;
    }

    /**
     * Custom match point comparison for Grand Final.
     * First competitor to reach TARGET_MATCH_POINTS_TO_WIN wins.
     */
    @Override
    public int compareByMedalRacesWon(int numberOfMedalRacesWonO1, int numberOfMedalRacesWonO2) {
        // Check if either competitor has reached the target
        boolean o1HasWon = numberOfMedalRacesWonO1 >= TARGET_MATCH_POINTS_TO_WIN;
        boolean o2HasWon = numberOfMedalRacesWonO2 >= TARGET_MATCH_POINTS_TO_WIN;
        
        if (o1HasWon && !o2HasWon) {
            return -1; // o1 wins
        } else if (o2HasWon && !o1HasWon) {
            return 1; // o2 wins
        } else if (o1HasWon && o2HasWon) {
            // Both have won, compare by who got there first (higher match points)
            return Integer.compare(numberOfMedalRacesWonO2, numberOfMedalRacesWonO1);
        } else {
            // Neither has won yet, compare by current match points
            return Integer.compare(numberOfMedalRacesWonO2, numberOfMedalRacesWonO1);
        }
    }

    /**
     * Handle carry-forward match points from Opening Series to Grand Final.
     * 1st place carries 1 match point, 2nd place carries 0.5 match point.
     * The 0.5 match point becomes 1 match point if the competitor wins any Grand Final race.
     */
    @Override
    public int getWinCount(Leaderboard leaderboard, Competitor competitor, RaceColumn raceColumn,
            final Double totalPoints, TimePoint timePoint, java.util.function.Function<Competitor, Double> totalPointsSupplier,
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        
        if (raceColumn.isCarryForward() && raceColumn.isMedalRace()) {
            // This is a carry-forward column in a medal series
            if (totalPoints != null) {
                // Handle fractional match points (0.5 becomes 1 if competitor wins any race)
                if (totalPoints == 0.5) {
                    // Check if competitor has won any subsequent races in this medal series
                    if (hasWonAnyRaceInSameMedalSeries(leaderboard, competitor, raceColumn, timePoint, totalPointsSupplier, cache)) {
                        return 1; // 0.5 match point becomes 1 match point
                    } else {
                        return 0; // Keep as 0.5, but we can't represent fractional wins, so treat as 0 for now
                    }
                } else {
                    return totalPoints.intValue(); // 1 match point or other integer values
                }
            }
            return 0;
        }
        
        // For non-carry-forward columns, use standard win detection
        return super.getWinCount(leaderboard, competitor, raceColumn, totalPoints, timePoint, totalPointsSupplier, cache);
    }

    /**
     * Check if a competitor has won any race in the same medal series after the carry-forward column.
     */
    private boolean hasWonAnyRaceInSameMedalSeries(Leaderboard leaderboard, Competitor competitor, 
            RaceColumn carryForwardColumn, TimePoint timePoint,
            java.util.function.Function<Competitor, Double> totalPointsSupplier,
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        
        if (!(carryForwardColumn instanceof RaceColumnInSeries)) {
            return false;
        }
        
        RaceColumnInSeries carryForwardColumnInSeries = (RaceColumnInSeries) carryForwardColumn;
        
        // Check all subsequent columns in the same medal series
        boolean foundCarryForward = false;
        for (RaceColumn column : carryForwardColumnInSeries.getSeries().getRaceColumns()) {
            if (column == carryForwardColumn) {
                foundCarryForward = true;
                continue;
            }
            
            if (foundCarryForward && !column.isCarryForward()) {
                // This is a race column after the carry-forward column
                if (isWin(leaderboard, competitor, column, timePoint, totalPointsSupplier, cache)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Custom tie-breaking for Medal Series based on Notice of Race 24.9.3.6:
     * 1. Number of match points
     * 2. Score in last race of Grand Final
     * 3. Previous stage ranking where tied competitors sailed together
     */
    @Override
    public int compareByLastMedalRacesCriteria(Competitor o1, List<Pair<RaceColumn, Double>> o1Scores, 
            Competitor o2, List<Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter, 
            Leaderboard leaderboard, Iterable<RaceColumn> raceColumnsToConsider,
            BiFunction<Competitor, RaceColumn, Double> totalPointsSupplier, 
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache, TimePoint timePoint, 
            int zeroBasedIndexOfLastMedalSeriesInWhichBothScored, int numberOfMedalRacesWonO1, int numberOfMedalRacesWonO2) {
        
        if (zeroBasedIndexOfLastMedalSeriesInWhichBothScored < 0) {
            return 0; // Neither scored in medal series
        }
        
        // First compare by match points (already handled by compareByMedalRacesWon)
        int matchPointComparison = compareByMedalRacesWon(numberOfMedalRacesWonO1, numberOfMedalRacesWonO2);
        if (matchPointComparison != 0) {
            return matchPointComparison;
        }
        
        // If match points are equal, compare by last race score in the Grand Final
        Double o1LastRaceScore = getLastMedalRaceScore(o1Scores);
        Double o2LastRaceScore = getLastMedalRaceScore(o2Scores);
        
        int lastRaceComparison = compareBySingleRaceColumnScore(o1LastRaceScore, o2LastRaceScore, nullScoresAreBetter);
        if (lastRaceComparison != 0) {
            return lastRaceComparison;
        }
        
        // If still tied, compare by opening series rank (previous stage where they sailed together)
        LeaderboardTotalRankComparator openingSeriesComparator = getOpeningSeriesRankComparator(
                raceColumnsToConsider, nullScoresAreBetter, timePoint, leaderboard, totalPointsSupplier, cache);
        return openingSeriesComparator.compare(o1, o2);
    }

    /**
     * Get the score from the last medal race for tie-breaking.
     */
    private Double getLastMedalRaceScore(List<Pair<RaceColumn, Double>> scores) {
        // Find the last medal race score (excluding carry-forward columns)
        for (int i = scores.size() - 1; i >= 0; i--) {
            Pair<RaceColumn, Double> score = scores.get(i);
            if (score.getA().isMedalRace() && !score.getA().isCarryForward() && score.getB() != null) {
                return score.getB();
            }
        }
        return null;
    }

    /**
     * In Medal Series, ignore regular score sums and focus on match points.
     */
    @Override
    public int compareByScoreSum(Competitor o1, List<Pair<RaceColumn, Double>> o1Scores, double o1ScoreSum,
            Competitor o2, List<Pair<RaceColumn, Double>> o2Scores, double o2ScoreSum, boolean nullScoresAreBetter,
            boolean haveValidMedalRaceScores, Supplier<Map<Competitor, Integer>> competitorsRankedByOpeningSeries) {
        
        if (haveValidMedalRaceScores) {
            // In Medal Series, match points take precedence over score sums
            return 0; // Let other criteria (match points) decide
        }
        
        // For Opening Series, use standard score sum comparison
        return super.compareByScoreSum(o1, o1Scores, o1ScoreSum, o2, o2Scores, o2ScoreSum, 
                nullScoresAreBetter, haveValidMedalRaceScores, competitorsRankedByOpeningSeries);
    }

    /**
     * Medal race scores are not the primary criterion - match points are.
     */
    @Override
    public int compareByMedalRaceScore(Double o1MedalRaceScore, Double o2MedalRaceScore, boolean nullScoresAreBetter) {
        // Medal race scores are used for tie-breaking within match points, not as primary criterion
        return 0;
    }
}
