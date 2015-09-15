package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;

/**
 * Implements an elimination scheme as used by surfing championships, based on a general low point scoring scheme. A
 * regatta is divided into "eliminations" where each elimination consists of a sequence of "rounds." Each round is in
 * turn divided into a number of "heats" (races). The final round in an elimination consists of a "final" race and a
 * "losers final" race. They constitute, in our terminology, one series with contiguous scoring. The winner of the
 * "losers final" gets one point more than the competitor ranking last in the "final" race. The winner of the final
 * race obtains 0.7 points. All other ranks are assigned points equal to the rank or the average of the ranks attained
 * by competitors ranking equal.
 * <p>
 * 
 * The semi-final has two heats, and the better half of the competitors in those heats get promoted to the "final," the
 * competitors ranking worse will participate in the "losers final." Points are usually not awarded in the semi-final
 * (exceptions for races not sailed see below).
 * <p>
 * 
 * In the quarter-final there are four heats. The competitors ranking in the top half of their heat are promoted to the
 * semi-final and are not awarded points for the quarter-final round (exceptions for races not sailed see below). Those
 * ranking in the bottom half of their heat are not promoted to the next round and are instead awarded points for the
 * elimination based on their rank in their heat. Across all quarter-final heats, competitors not promoted and having
 * the same rank obtain equal points regardless their heat. The number of points is calculated as the average of their
 * rank in the elimination. Quarter-final participants rank better than those eliminated in earlier rounds. Eliminated
 * competitors rank better in the elimination if they obtained a better rank in their heat than other competitors
 * eliminated in the same round. For example, if each of the four quarter-final heat has eight competitors, the
 * four competitors ranking last (8th) in their heat get the average points for ranks 29-32 (30.5 points), whereas
 * the four competitors ranking 7th in their heat get the average points for ranks 25-28 (26.5 points), and so on.
 * <p>
 * 
 * Other rounds preceding the quarter-final work by the same principle as the quarter-final.
 * <p>
 * 
 * Should a planned heat not be sailed then all competitors assigned to that heat will obtain equal points based on the
 * average of the ranks for which they would have sailed. For example, if the final heat cannot be sailed, all competitors
 * who qualified for the final race obtain the average of 0.7, 2, 3, 4, 5, 6, 7 and 8 points (4.46 points).
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LowPointWithEliminationsAndRoundsWinnerGets07 extends LowPoint {
    private static final long serialVersionUID = -2318652113347853873L;

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_WITH_ELIMINATIONS_AND_ROUNDS_WINNER_GETS_07;
    }
}
