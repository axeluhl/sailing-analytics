package com.sap.sailing.domain.leaderboard;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;

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
    Double getScoreForRank(RaceColumn raceColumn, Competitor competitor, int rank);
    
    /**
     * If a competitor is disqualified, a penalty score is attributed by this scoring scheme. Some schemes require to
     * know the number of competitors in the race, some need to know the total number of competitors in the leaderboard
     * or regatta.
     */
    Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason, int numberOfCompetitorsInLeaderboard);
    
    ScoringSchemeType getType();
}
