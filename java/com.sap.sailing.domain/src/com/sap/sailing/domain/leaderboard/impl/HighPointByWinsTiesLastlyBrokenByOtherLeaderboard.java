package com.sap.sailing.domain.leaderboard.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithOtherTieBreakingLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * Race winners get scored one point, all others zero points, just like in {@link HighPointMatchRacing}. Additionally,
 * when the number of wins compares equal, the competitors are compared by their "finishing order" in their last race.
 * As this scoring scheme disregards numbers of races scored as a ranking criterion, two competitors tied on the number
 * of wins may have different numbers of races scored; therefore, comparing their "last" race may compare races from
 * different race columns.
 * <p>
 * 
 * The "finishing order" is determined by first checking for a penalty code ({@link MaxPointsReason}). If a
 * non-{@code null}. non-{@link MaxPointsReason#NONE NONE} penalty code is found, the competitor is considered to not
 * have finished. Otherwise, the finishing rank is determined by looking at the competitor's {@link TrackedRace} for the
 * last race. If no tracked race is found for a competitor's last race, {@code null} is assumed for the finishing order,
 * representing "not finished." The finishing ranks are then compared. If they compare equal, this scoring scheme
 * assumes that the ranking comparator will eventually break the tie by calling
 * {@link #compareByOtherTieBreakingLeaderboard(RegattaLeaderboardWithOtherTieBreakingLeaderboard, Competitor, Competitor, TimePoint)}.<p>
 * 
 * Columns marked as "medal race" are <em>not</em> doubled automatically by this scoring scheme.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class HighPointByWinsTiesLastlyBrokenByOtherLeaderboard extends HighPointMatchRacing {
    private static final long serialVersionUID = -2930982687072741643L;

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_BY_WINS_TIES_LASTLY_BROKEN_BY_OTHER_LEADERBOARD;
    }

    /**
     * No A8.1 comparison based on sorted scores happens for this scoring scheme
     */
    @Override
    public int compareByBetterScore(Competitor o1, List<Pair<RaceColumn, Double>> o1Scores, Competitor o2,
            List<Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter, TimePoint timePoint,
            Leaderboard leaderboard, Map<Competitor, Set<RaceColumn>> discardedRaceColumnsPerCompetitor, BiFunction<Competitor, RaceColumn, Double> totalPointsSupplier, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return 0;
    }

    /**
     * Comparing by "last race" here assumes explicitly that the competitors may have sailed a different number of
     * races, e.g., in the 2024 Foiling Kite Olympic Semifinal format. The comparison by last race then may compare
     * results from different races. Furthermore, the rules specify that not the score / points but the <em>finishing order</em>
     * is to be compared. Should two boats not have finished their respective last race (which usually leads to a
     * penalty such as {@link MaxPointsReason#DNF DNF}) or should they not have started ({@link MaxPointsReason#DNS
     * DNS}) or received some other penalty, that penalty leads to a score of {@code 0.0} by default and will lead to
     * such competitors to be considered "tied" based on their last race. In this, a penalty code that exists for a
     * competitor in the last race takes precedence over a finish mark passing and the corresponding order implied
     * by the tracked race's finishing ranks.
     * <p>
     * 
     * Should by some definition one penalty code be considered "better" than another, manual intervention based on
     * 1/1000th of a point may be required, or the implementation of
     * {@link ScoringScheme#getPenaltyScore(RaceColumn, Competitor, MaxPointsReason, Integer, com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher, TimePoint, com.sap.sailing.domain.leaderboard.Leaderboard)}
     * must be overridden accordingly here to provide those 1/1000th of a point automatically.
     */
    @Override
    public int compareByLastRace(List<Pair<RaceColumn, Double>> o1ScoresIncludingDiscarded,
            List<Pair<RaceColumn, Double>> o2ScoresIncludingDiscarded, boolean nullScoresAreBetter, Competitor o1,
            Competitor o2, TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        int result = 0;
        if (!o1ScoresIncludingDiscarded.isEmpty() && !o2ScoresIncludingDiscarded.isEmpty()) {
            final Comparator<Double> doubleComparatorNullsLast = Comparator.nullsLast(Comparator.naturalOrder());
            final Double o1LastRaceScore = o1ScoresIncludingDiscarded.get(o1ScoresIncludingDiscarded.size() - 1).getB();
            final Double o2LastRaceScore = o2ScoresIncludingDiscarded.get(o2ScoresIncludingDiscarded.size() - 1).getB();
            result = doubleComparatorNullsLast.compare(o1LastRaceScore, o2LastRaceScore);
            // the scores would differ only if one competitor won its last race and the other didn't
            if (result == 0 && o1LastRaceScore != null && o1LastRaceScore != 1.0) { // FIXME use isWin which has to move from Leaderboard to ScoringScheme
                final TrackedRace tr1 = o1ScoresIncludingDiscarded.get(o1ScoresIncludingDiscarded.size()-1).getA().getTrackedRace(o1);
                final int rank1;
                final Integer finishingRank1 = tr1 == null ? null : (rank1=tr1.getRank(o1, timePoint, cache))==0?null:rank1;
                final TrackedRace tr2 = o2ScoresIncludingDiscarded.get(o2ScoresIncludingDiscarded.size()-1).getA().getTrackedRace(o2);
                final int rank2;
                final Integer finishingRank2 = tr2 == null ? null : (rank2=tr2.getRank(o2, timePoint, cache))==0?null:rank2;
                final Comparator<Integer> intComparatorNullsLast = Comparator.nullsLast(Comparator.naturalOrder());
                result = intComparatorNullsLast.compare(finishingRank1, finishingRank2);
            }
        }
        return result;
    }

    @Override
    public double getScoreFactor(RaceColumn raceColumn) {
        return 1.0;
    }

    @Override
    public int compareByOtherTieBreakingLeaderboard(RegattaLeaderboardWithOtherTieBreakingLeaderboard leaderboard,
            Competitor o1, Competitor o2, TimePoint timePoint) {
        final int result;
        final int o1RankInOtherTieBreakingLeaderboard = leaderboard.getOtherTieBreakingLeaderboard().getTotalRankOfCompetitor(o1, timePoint);
        final int o2RankInOtherTieBreakingLeaderboard = leaderboard.getOtherTieBreakingLeaderboard().getTotalRankOfCompetitor(o2, timePoint);
        if (o1RankInOtherTieBreakingLeaderboard == o2RankInOtherTieBreakingLeaderboard) {
            result = 0;
        } else {
            if (o1RankInOtherTieBreakingLeaderboard == 0) {
                result = 1; // o1 has no rank; this is worse ("greater") than any valid rank
            } else if (o2RankInOtherTieBreakingLeaderboard == 0) {
                result = -1;
            } else {
                result = Integer.compare(o1RankInOtherTieBreakingLeaderboard, o2RankInOtherTieBreakingLeaderboard);
            }
        }
        return result;
    }
}
