package com.sap.sailing.domain.leaderboard;

/**
 * A regatta leaderboard that may have to resort to the competitor standings in another leaderboard
 * for breaking specific ties. This {@link #getOtherTieBreakingLeaderboard() other leaderboard} can,
 * e.g., be obtained by a {@link ScoringScheme} which then looks up scores or ranks in that leaderboard
 * for tie-breaking purposes.<p>
 * 
 * Changes in the tie-breaking leaderboard may lead to tie-breaking changes in this leaderboard. However,
 * this leaderboard assumes that by and large the tie-breaking leaderboard is complete and has no running
 * races anymore. An exception to this assumption are score corrections applied to the tie-breaking
 * leaderboard, e.g., by importing official results after the races in this leaderboard have started
 * already. Therefore, this leaderboard acts as a {@link ScoreCorrectionListener} on the
 * tie-breaking leaderboard and will forward those changes.
 *  
 * @author Axel Uhl (d043530)
 *
 */
public interface RegattaLeaderboardWithOtherTieBreakingLeaderboard extends RegattaLeaderboard {
    RegattaLeaderboard getOtherTieBreakingLeaderboard();
}
