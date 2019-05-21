package com.sap.sailing.domain.leaderboard;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * A leaderboard has a scoring scheme that decides how race ranks map to scores, how penalties are to be scored,
 * and how scores are to be compared (are lower or higher scores better?). The scoring scheme can either be
 * provided by the {@link Regatta} or by a {@link FlexibleLeaderboard}. In any case, it is reachable through
 * the {@link Leaderboard} interface.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ScoringScheme extends Serializable {
    /**
     * The factor by which a medal race score is multiplied by default in the overall point scheme.
     * 
     * @see #getFactor()
     */
    static final double DEFAULT_MEDAL_RACE_FACTOR = 2.0;
    
    /**
     * If this returns <code>true</code>, a higher score is better. For example, the Extreme Sailing Series uses this
     * scoring scheme, as opposed to the olympic sailing classes which use a low-point system.
     */
    boolean isHigherBetter();
    
    /**
     * A comparator in line with the result of {@link #isHigherBetter()}. The comparator returns "less" for results
     * considered "better."
     * 
     * @param nullScoresAreBetter
     *            if <code>true</code>, a <code>null</code> score will be considered "better" ("less") than a non-
     *            <code>null</code> score; otherwise, <code>null</code> scores will be considered "worse" ("greater")
     *            than non-<code>null</code> scores.
     */
    Comparator<Double> getScoreComparator(boolean nullScoresAreBetter);
    
    /**
     * For a <code>rank</code> that a <code>competitor</code> achieved in a race, returns the score attributed to this
     * rank according to this scoring scheme. A scoring scheme may need to know the competitor list for the race. Therefore,
     * the race column as well as the competitor need to be passed although some trivial scoring schemes may not need them.<p>
     * 
     * If the <code>competitor</code> has no {@link RaceColumn#getTrackedRace(Competitor) tracked race} in the column in which
     * the competitor participated, <code>null</code> is returned, meaning the competitor has no score assigned for that
     * race.
     */
    Double getScoreForRank(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor,
            int rank, Callable<Integer> numberOfCompetitorsInRaceFetcher,
            NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint);
    
    /**
     * If a competitor is disqualified, a penalty score is attributed by this scoring scheme. Some schemes require to
     * know the number of competitors in the race, some need to know the total number of competitors in the leaderboard
     * or regatta.
     * 
     * @param numberOfCompetitorsInLeaderboardFetcher
     *            if it returns <code>null</code>, the caller cannot determine the number of competitors in the single
     *            race; otherwise, this parameter tells the number of competitors in the same race as
     *            <code>competitor</code>, not in the entire <code>raceColumn</code> (those may be more in case of split
     *            fleets). The scoring scheme may use this number, if available, to infer a penalty score.
     * @param timePoint
     *            an optional timePoint that may help the scheme to determine the penalty related to a certain point in
     *            time only.
     * @param leaderboard
     *            may be required in case a "penalty" such as a redress needs to inspect the scores of other race
     *            columns as well; implementations need to take great care not to cause endless recursions by
     *            naively asking the leaderboard for scores which would recurse into this method
     */
    Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason,
            Integer numberOfCompetitorsInRace, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher,
            TimePoint timePoint, Leaderboard leaderboard);

    /**
     * @param competitor1Scores scores of the first competitor, in the order of race columns in the leaderboard
     * @param competitor2Scores scores of the second competitor, in the order of race columns in the leaderboard
     * @param leaderboard TODO
     */
    int compareByBetterScore(Competitor o1,
            List<Util.Pair<RaceColumn, Double>> competitor1Scores, Competitor o2, List<Util.Pair<RaceColumn, Double>> competitor2Scores, boolean nullScoresAreBetter, TimePoint timePoint, Leaderboard leaderboard);

    /**
     * In case two competitors scored in different numbers of races, this scoring scheme decides whether this
     * decides terminally their mutual ranking. If not, <code>0</code> is returned and the comparator needs to look
     * at other criteria to compare the competitors.
     */
    int compareByNumberOfRacesScored(int competitor1NumberOfRacesScored, int competitor2NumberOfRacesScored);
    
    ScoringSchemeType getType();

    /**
     * Usually, when all other sorting criteria end up in a tie, the last race sailed is used to decide, and from there
     * backwards. This implements Racing Rules of Sailing (RRS) rule A8.2:
     * <p>
     * 
     * <em>"A8.2 If a tie remains between two or more boats, they shall be ranked in order of their scores in the last
     * race. Any remaining ties shall be broken by using the tied boats’ scores in the next-to-last race and so on until
     * all ties are broken. These scores shall be used even if some of them are excluded scores."</em>
     */
    int compareByLastRace(List<Util.Pair<RaceColumn, Double>> o1Scores, List<Util.Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter, Competitor o1, Competitor o2);

    /**
     * Under certain circumstances, a scoring scheme may decide that the scores of a column are not (yet) to be used
     * for the leaderboard's net scores. This may, e.g., be the case if a column is split into more than one fleet and
     * those fleets are unordered. In that case, scores need to be available for all fleets before the column counts
     * for the net scores. Another example is a scoring scheme that defines elimination rounds and awards no points
     * to a competitor in a round from which the competitor got promoted to the next round. Such promotion columns
     * then have no scores and don't count in the number of races starting from where discards are applied.<p>
     */
    boolean isValidInNetScore(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor, TimePoint at);

    /**
     * Some scoring schemes are applied to {@link LeaderboardGroupMetaLeaderboard} instances. These instances of a
     * leaderboard are based on other leaderboards grouped in a {@link LeaderboardGroup}. It can happen that the
     * {@link ScoringScheme} needs to have a look at the total points of the other leaderboards in that group. The
     * ordering of the list containing the total points matches the order in the group.
     * @throws NoWindException 
     */
    int compareByLatestRegattaInMetaLeaderboard(Leaderboard leaderboard, Competitor o1, Competitor o2, TimePoint timePoint);

    /**
     * Returning {@code true} makes the number of wins in a medal series the primary ranking criteria.
     * The number of wins that makes a competitor the overall winner must be returned by {@link #getTargetAmountOfMedalRaceWins()}.
     */
    default boolean isMedalWinAmountCriteria() {
        return false;
    }
    
    /**
     * Returning {@code true} makes the {@link RaceColumn#isCarryForward() carry forward score} in a
     * {@link Series#isMedal() medal series} a secondary ranking criteria for competitors that have an equal overall
     * score.
     */
    default boolean isCarryForwardInMedalsCriteria() {
        return false;
    }
    
    /**
     * Returning {@code true} makes the last medal race (having valid scores) a secondary ranking criteria for
     * competitors that have an equal overall score.
     */
    default boolean isLastMedalRaceCriteria() {
        return false;
    }

    /**
     * If {@link #isMedalWinAmountCriteria()} returns {@code true}, this will be the amount of races that must be won,
     * in order to win the medal series instantly
     */
    default int getTargetAmountOfMedalRaceWins() {
        throw new IllegalStateException("This call is not valid for this scoringSheme");
    }

    /**
     * Usually, the scores in each leaderboard column count as they are for the overall score. However, if a column is a
     * medal race column it usually counts double. Under certain circumstances, columns may also count with factors
     * different from 1 or 2. For example, we've seen cases in the Extreme Sailing Series where the race committee
     * defined that in the overall series leaderboard the last two columns each count 1.5 times their scores.
     */
    default double getScoreFactor(RaceColumn raceColumn) {
        Double factor = raceColumn.getExplicitFactor();
        if (factor == null) {
            factor = raceColumn.isMedalRace() ? DEFAULT_MEDAL_RACE_FACTOR : 1.0;
        }
        return factor;
    }
}
