package com.sap.sailing.domain.leaderboard;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

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
    Double getScoreForRank(RaceColumn raceColumn, Competitor competitor, int rank, Integer numberOfCompetitorsInRace);
    
    /**
     * If a competitor is disqualified, a penalty score is attributed by this scoring scheme. Some schemes require to
     * know the number of competitors in the race, some need to know the total number of competitors in the leaderboard
     * or regatta.
     * 
     * @param numberOfCompetitorsInRace
     *            if <code>null</code>, the caller cannot determine the number of competitors in the single race;
     *            otherwise, this parameter tells the number of competitors in the same race as <code>competitor</code>,
     *            not in the entire <code>raceColumn</code> (those may be more in case of split fleets). The scoring scheme
     *            may use this number, if available, to infer a penalty score.
     */
    Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason,
            Integer numberOfCompetitorsInRace, int numberOfCompetitorsInLeaderboard);

    /**
     * @param competitor1Scores scores of the first competitor, in the order of race columns in the leaderboard
     * @param competitor2Scores scores of the second competitor, in the order of race columns in the leaderboard
     */
    int compareByBetterScore(List<Double> competitor1Scores, List<Double> competitor2Scores, boolean nullScoresAreBetter);

    /**
     * In case two competitors scored in different numbers of races, this scoring scheme decides whether this
     * decides terminally their mutual ranking. If not, <code>0</code> is returned and the comparator needs to look
     * at other criteria to compare the competitors.
     */
    int compareByNumberOfRacesScored(int competitor1NumberOfRacesScored, int competitor2NumberOfRacesScored);
    
    ScoringSchemeType getType();

    /**
     * Usually, when all other sorting criteria end up in a tie, the last race sailed is used to decide.
     * @param nullScoresAreBetter TODO
     */
    int compareByLastRace(List<Double> o1Scores, List<Double> o2Scores, boolean nullScoresAreBetter);
}
