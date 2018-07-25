package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * A variant of the {@link HighPoint} scoring scheme which breaks ties differently and which assigns a score of 10 to
 * the winner of a regatta, and one less for each subsequent position. This scheme is used particularly by the Extreme
 * Sailing Series' overall leaderboard and can only be applied to {@link MetaLeaderboard}s.
 * <p>
 * 
 * From the Notices of Race: "13.5: If there is a tie in the Series score between two or more boats at any time, the tie
 * shall be broken in favour of the boat that has won the most Regattas. If a tie still remains, it shall be broken in
 * favour of the boat that had the better place at the last Regatta sailed."
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public abstract class AbstractHighPointExtremeSailingSeriesOverall extends HighPoint {
    private static final long serialVersionUID = -2500858156511889174L;

    private final int maxPoints;
    
    private static final int MIN_RACES_REQUIRED_TO_BE_SCORED = 5;

    protected AbstractHighPointExtremeSailingSeriesOverall(int maxPoints) {
        super();
        this.maxPoints = maxPoints;
    }

    @Override
    public Double getScoreForRank(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor,
            int rank, Callable<Integer> numberOfCompetitorsInRaceFetcher, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint) {
        Double result;
        if (rank == 0) {
            result = null;
        } else {
            result = Math.max(1.0, (double) (maxPoints - rank + 1));
        }
        return result;
    }

    /**
     * Implements rule 13.5 of the Extreme Sailing Series notice of race as of August 2012.
     */
    @Override
    public int compareByBetterScore(Competitor o1, List<com.sap.sse.common.Util.Pair<RaceColumn, Double>> o1Scores, Competitor o2, List<com.sap.sse.common.Util.Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter, TimePoint timePoint, Leaderboard leaderboard) {
        int o1Wins = getWins(o1Scores);
        int o2Wins = getWins(o2Scores);
        int result = o2Wins - o1Wins;
        if (result == 0 && o1Scores.size() >= 1 && o2Scores.size() >= 1) {
            result = -o1Scores.get(o1Scores.size()-1).getB().compareTo(o2Scores.get(o2Scores.size()-1).getB());
        }
        return result;
    }

    /**
     * Counts a competitor's wins by comparing the scores to {@link #maxPoints} which is the score attributed to a race
     * won
     */
    private int getWins(List<com.sap.sse.common.Util.Pair<RaceColumn, Double>> scores) {
        int wins = 0;
        for (com.sap.sse.common.Util.Pair<RaceColumn, Double> score : scores) {
            if (Math.abs(score.getB() - maxPoints * getScoreFactor(score.getA())) < 0.0000001) {
                wins++;
            }
        }
        return wins;
    }

    /**
     * Generally, in the Extreme Sailing Series the number of races scored doesn't matter. However, if a competitor
     * scored fewer than five regattas, the competitor doesn't participate in the overall ranking and is therefore to be
     * sorted worse than all competitors completing five or more regattas. Note that here, for an "overall" leaderboard,
     * a column represents a regatta, not a single race.
     */
    @Override
    public int compareByNumberOfRacesScored(int competitor1NumberOfRacesScored, int competitor2NumberOfRacesScored) {
        int result;
        if (competitor1NumberOfRacesScored >= MIN_RACES_REQUIRED_TO_BE_SCORED) {
            if (competitor2NumberOfRacesScored >= MIN_RACES_REQUIRED_TO_BE_SCORED) {
                result = 0;
            } else {
                result = -1; // competitor1 is better ("less") because it's the only one of the two scoring more than MIN_RACES_REQUIRED_TO_BE_SCORED
            }
        } else {
            if (competitor2NumberOfRacesScored >= MIN_RACES_REQUIRED_TO_BE_SCORED) {
                result = 1; // competitor2 is better ("less") because it's the only one of the two scoring more than MIN_RACES_REQUIRED_TO_BE_SCORED
            } else {
                result = 0;
            }
        }
        return result;
    }

    /**
     * Notice of Race (NOR) section 13.5 specifies for the series score: "If a tie still remains, it shall be broken in
     * favor of the boat that had the better place at the last Regatta sailed."
     * @throws NoWindException 
     */
    @Override
    public int compareByLatestRegattaInMetaLeaderboard(Leaderboard leaderboard, Competitor o1, Competitor o2, TimePoint timePoint) {
        assert leaderboard instanceof MetaLeaderboard;
        // compare by last regatta if this leaderboard is a meta leaderboard
        final int result;
        if (leaderboard instanceof MetaLeaderboard) {
            MetaLeaderboard overallLeaderboard = (MetaLeaderboard) leaderboard;
            List<Double> o1PointsInLeaderboardsOfTheGroup = new ArrayList<Double>();
            List<Double> o2PointsInLeaderboardsOfTheGroup = new ArrayList<Double>();
            List<Leaderboard> randomAccessLeaderboardList = new ArrayList<Leaderboard>(Util.size(overallLeaderboard.getLeaderboards()));
            Util.addAll(overallLeaderboard.getLeaderboards(), randomAccessLeaderboardList);
            for (Leaderboard leaderboardInOverall : overallLeaderboard.getLeaderboards()) {
                o1PointsInLeaderboardsOfTheGroup.add(leaderboardInOverall.getNetPoints(o1, timePoint));
                o2PointsInLeaderboardsOfTheGroup.add(leaderboardInOverall.getNetPoints(o2, timePoint));
            }
            int localResult = 0;
            for (ListIterator<Leaderboard> reverseLeaderbaordIterator=randomAccessLeaderboardList.listIterator(randomAccessLeaderboardList.size());
                    reverseLeaderbaordIterator.hasPrevious(); ) {
                Leaderboard leaderboardInOverall = reverseLeaderbaordIterator.previous();
                final Double o1PointsForLeaderboard = leaderboardInOverall.getNetPoints(o1, timePoint);
                final Double o2PointsForLeaderboard = leaderboardInOverall.getNetPoints(o2, timePoint);
                if (o1PointsForLeaderboard != null && o2PointsForLeaderboard != null) {
                    // we're in a scheme where points never get 0 so we can safely assume
                    // that the last total points that are no 0 are the ones that we want to
                    // look at. We also assume that the ordering matches the one in the group
                    if (o1PointsForLeaderboard > 0 && o2PointsForLeaderboard > 0) {
                        localResult = -o1PointsForLeaderboard.compareTo(o2PointsForLeaderboard);
                        break;
                    }
                }
            }
            result = localResult;
        } else {
            result = 0;
        }
        return result;
    }
}
