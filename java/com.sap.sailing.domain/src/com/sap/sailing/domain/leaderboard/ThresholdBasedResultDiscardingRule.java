package com.sap.sailing.domain.leaderboard;

/**
 * The strategy for discarding results implemented by this rule is to compare the number
 * of races that have been started so far to a given series of thresholds <code>t[i]</code>.
 * Let <code>s</code> be the number of races started so far, then i scores of non-medal
 * races can be discarded if and only if <code>t[i] &lt;= s && (t.length==i-1 || t[i+1] &gt; s)</code>.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ThresholdBasedResultDiscardingRule extends ResultDiscardingRule {
    int[] getDiscardIndexResultsStartingWithHowManyRaces();
}
