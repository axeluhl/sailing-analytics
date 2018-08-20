package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;




import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.meta.MetaLeaderboardColumn;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;


/**
 * To be used for national sailing leagues' overall (season) leaderboard. Ties are broken by comparing the point sums
 * of the act leaderboards. When those are equal, too, the regatta rank during the last act is used to break the tie.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LowPointForLeagueOverallLeaderboard extends LowPoint {
    private static final long serialVersionUID = -2767385186133743330L;

    public LowPointForLeagueOverallLeaderboard() {
        super();
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_LEAGUE_OVERALL;
    }

    /**
     * When this method is called, we can assume that the points sum for both competitors is equal.
     * 
     */
    @Override
    public int compareByBetterScore(Competitor o1, List<Pair<RaceColumn, Double>> o1Scores,
            Competitor o2, List<Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter, TimePoint timePoint, Leaderboard leaderboard) {
        final Double o1ScoreSum = o1Scores.stream().collect(Collectors.summingDouble(rcAndScore->rcAndScore.getB()));
        final Double o2ScoreSum = o2Scores.stream().collect(Collectors.summingDouble(rcAndScore->rcAndScore.getB()));
        assert Math.abs(o1ScoreSum - o2ScoreSum) < 0.00001;
        assert o1Scores.stream().allMatch(rcAndScore->rcAndScore.getA() instanceof MetaLeaderboardColumn);
        assert o2Scores.stream().allMatch(rcAndScore->rcAndScore.getA() instanceof MetaLeaderboardColumn);
        double netPointsSumO1 = getNetPoints(o1, o1Scores.stream().map(rcAndScore->rcAndScore.getA()), timePoint);
        double netPointsSumO2 = getNetPoints(o2, o1Scores.stream().map(rcAndScore->rcAndScore.getA()), timePoint);
        return getScoreComparator(nullScoresAreBetter).compare(netPointsSumO1, netPointsSumO2);
    }

    private double getNetPoints(Competitor competitor, Stream<RaceColumn> raceColumns, TimePoint timePoint) {
        return raceColumns.collect(Collectors.summingDouble(rc->((MetaLeaderboardColumn) rc).getLeaderboard().getNetPoints(competitor, timePoint)));
    }
}
