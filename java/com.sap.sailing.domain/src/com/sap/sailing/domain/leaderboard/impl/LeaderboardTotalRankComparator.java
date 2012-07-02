package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;

/**
 * Compares two competitors that occur in a {@link Leaderboard#getCompetitors()} set in the context of the
 * {@link Leaderboard} according to their total rank at a given point in time. "Better" is represented as "lesser."
 * <p>
 * 
 * For a general {@link Leaderboard} we don't know about Series. In a {@link Leaderboard}, all we have are the columns
 * with their fleets which can be compared but may compare equal; also, we know if a column is a medal race column.
 * Participants of a medal race always score better than all remaining competitors. (We only know the medal race
 * participants if the column has a tracked race.) If both competitors scored in a medal race, this score will be
 * compared, regardless of the sum of any other scores.
 * <p>
 * 
 * If that hasn't decided the order yet, then as soon as we find a column with more than one tracked race with the fleets
 * comparing non-equal, this decides the order if the two competitors belong to different fleets.
 * <p>
 * 
 * If that still hasn't decided the order, the scores will decide. If the competitors scored in a different number of
 * races, the competitor scoring in more races is considered better (lesser). If they scored in an equal number of races,
 * the score sums are compared using {@link #compareByScoreSum(int, int)}. If that still doesn't decide the order, the scores
 * are sorted from best to worst and compared one by one. The first differing score decides. If all scores are equal pairwise,
 * both competitors are ranked equal.<p>
 * 
 * For a RegattaLeaderboard, if a column in a series has tracked races for all of its fleets, these competitors rank
 * better than all remaining competitors that appear in prior series. This is probably the generalization of the
 * "medal race" rule where the medal "series" has one race, and if it's tracked, its participants rank better than all
 * others in prior series who did not reach the medal race.
 * 
 * @author Axel Uhl (D043530)
 */
public class LeaderboardTotalRankComparator implements Comparator<Competitor> {
    private final Leaderboard leaderboard;
    private final Comparator<Integer> scoreComparator;
    private final Map<Pair<Competitor, RaceColumn>, Integer> totalPointsCache;
    
    public LeaderboardTotalRankComparator(Leaderboard leaderboard, TimePoint timePoint, Comparator<Integer> scoreComparator) throws NoWindException {
        super();
        this.leaderboard = leaderboard;
        this.scoreComparator = scoreComparator;
        totalPointsCache = new HashMap<Pair<Competitor, RaceColumn>, Integer>();
        for (Competitor competitor : leaderboard.getCompetitors()) {
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                totalPointsCache.put(new Pair<Competitor, RaceColumn>(competitor, raceColumn),
                        leaderboard.getTotalPoints(competitor, raceColumn, timePoint));
            }
        }
    }
    
    protected Leaderboard getLeaderboard() {
        return leaderboard;
    }
    
    @Override
    public int compare(Competitor o1, Competitor o2) {
        List<Integer> o1Scores = new ArrayList<Integer>();
        List<Integer> o2Scores = new ArrayList<Integer>();
        int o1ScoreSum = 0;
        int o2ScoreSum = 0;
        int o1MedalRaceScore = 0;
        int o2MedalRaceScore = 0;
        for (RaceColumn raceColumn : getLeaderboard().getRaceColumns()) {
            int preemptiveColumnResult = 0;
            final int o1Score = totalPointsCache.get(new Pair<Competitor, RaceColumn>(o1, raceColumn));
            if (o1Score != 0) {
                o1Scores.add(o1Score);
                o1ScoreSum += o1Score;
            }
            final int o2Score = totalPointsCache.get(new Pair<Competitor, RaceColumn>(o2, raceColumn));
            if (o2Score != 0) {
                o2Scores.add(o2Score);
                o2ScoreSum += o2Score;
            }
            if (raceColumn.isMedalRace()) {
                o1MedalRaceScore = o1Score;
                o2MedalRaceScore = o2Score;
                // similar to compareByFleet, however, tracking is not required; having medal race column points
                // (tracked or manual) is sufficient
                preemptiveColumnResult = compareByMedalRaceParticipation(o1Score, o2Score);
            }
            if (preemptiveColumnResult == 0) {
                preemptiveColumnResult = compareByFleet(raceColumn, o1, o2);
            }
            if (preemptiveColumnResult != 0) {
                return preemptiveColumnResult;
            }
        }
        // now count the races in which they scored; if they scored in a different number of races, prefer the
        // competitor
        // who scored more often; otherwise, prefer the competitor who has a better score sum; if score sums are equal,
        // break
        // tie by sorting scores and looking for the first score difference.
        int result = compareByNumberOfRacesScored(o1Scores.size(), o2Scores.size());
        if (result == 0) {
            result = compareByScoreSum(o1ScoreSum, o2ScoreSum);
            if (result == 0) {
                result = compareByMedalRaceScore(o1MedalRaceScore, o2MedalRaceScore);
                if (result == 0) {
                    result = compareByBetterScore(o1Scores, o2Scores);
                }
            }
        }
        return result;
    }

    /**
     * Precondition: either both scored in medal race or both didn't. If both scored, the better score wins.
     * This is to be applied only if the total score of both competitors are equal to each other.
     */
    private int compareByMedalRaceScore(int o1MedalRaceScore, int o2MedalRaceScore) {
        assert o1MedalRaceScore != 0 || o2MedalRaceScore == 0;
        if (o1MedalRaceScore != 0) {
            return getScoreComparator().compare(o1MedalRaceScore, o2MedalRaceScore);
        } else {
            return 0;
        }
    }

    private int compareByFleet(RaceColumn raceColumn, Competitor o1, Competitor o2) {
        Fleet o1f = raceColumn.getFleetOfCompetitor(o1);
        Fleet o2f = raceColumn.getFleetOfCompetitor(o2);
        // if the fleet for both was identified because both were tracked in this column, then if the fleets
        // don't compare equal, return the fleet comparison as result immediately. Example: o1 competed in Gold fleet,
        // o2 in Silver fleet; Gold compares better to Silver, so o1 is compared better to o2.
        int result = 0;
        if (o1f != null) {
            if (o2f != null) {
                if (o1f.compareTo(o2f) != 0) {
                    result = o1f.compareTo(o2f);
                }
            } else {
                // check if o1's fleet is best or worst in column; in that case, o1's membership in this fleet and the fact
                // that o2 is not part of that fleet determines the result
                result = extremeFleetComparison(raceColumn, o1f);
            }
        } else if (o2f != null) {
            // check if o1's fleet is best or worst in column; in that case, o1's membership in this fleet and the fact
            // that o2 is not part of that fleet determines the result
            result = -extremeFleetComparison(raceColumn, o2f);
        }
        return result;
    }

    /**
     * If the race column only has one fleet, no decision is made and 0 is returned. Otherwise, if <code>fleet</code> is the
     * best fleet with others in the column being worse, return "better" (lesser; -1). If <code>fleet</code> is the worst fleet
     * with others in the column being better, return "worse" (greater; 1). Otherwise, return 0.
     */
    private int extremeFleetComparison(RaceColumn raceColumn, Fleet fleet) {
        boolean allOthersAreGreater = true;
        boolean allOthersAreLess = true;
        boolean othersExist = false;
        for (Fleet f : raceColumn.getFleets()) {
            if (f != fleet) {
                othersExist = true;
                allOthersAreGreater = allOthersAreGreater && f.compareTo(fleet) > 0;
                allOthersAreLess = allOthersAreLess && f.compareTo(fleet) < 0;
            }
        }
        int result = 0;
        if (othersExist) {
            assert !(allOthersAreGreater && allOthersAreLess);
            if (allOthersAreGreater) {
                result = -1;
            } else if (allOthersAreLess) {
                result = 1;
            }
        }
        return result;
    }

    private int compareByMedalRaceParticipation(int o1Score, int o2Score) {
        // if only one scored in medal race, this decides the order and returns immediately
        if (o1Score == 0) {
            if (o2Score != 0) {
                return 1;
            }
        } else {
            if (o2Score == 0) {
                return -1;
            } else {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Assuming both competitors scored in the same number of races, compares the sorted scores.
     * @param o1Scores
     * @param o2Scores
     * @return
     */
    protected int compareByBetterScore(List<Integer> o1Scores, List<Integer> o2Scores) {
        assert o1Scores.size() == o2Scores.size();
        Comparator<Integer> scoreComparator = getScoreComparator();
        Collections.sort(o1Scores, scoreComparator);
        Collections.sort(o2Scores, scoreComparator);
        // now both lists are sorted from best to worst score
        Iterator<Integer> o1Iter = o1Scores.iterator();
        Iterator<Integer> o2Iter = o2Scores.iterator();
        int result = 0;
        while (result == 0 && o1Iter.hasNext() && o2Iter.hasNext()) {
            result = scoreComparator.compare(o1Iter.next(), o2Iter.next());
        }
        return result;
    }
    
    /**
     * Returns a comparator for comparing individual scores. This implementation returns a comparator for the usual ISAF
     * scheme where lesser scores compare "better" which again means "lesser." Therefore, the comparator retunred compares
     * the integer numbers by their natural ordering.
     */
    protected Comparator<Integer> getScoreComparator() {
        return scoreComparator;
    }

    /**
     * This implementation ranks a competitor better (lesser) if it has the lower score sum
     */
    protected int compareByScoreSum(int o1ScoreSum, int o2ScoreSum) {
        return getScoreComparator().compare(o1ScoreSum, o2ScoreSum);
    }

    /**
     * Compares a competitor better (lesser) if it has the greater number of races scored
     */
    protected int compareByNumberOfRacesScored(int o1NumberOfRacesScored, int o2NumberOfRacesScored) {
        return o2NumberOfRacesScored - o1NumberOfRacesScored;
    }
}
