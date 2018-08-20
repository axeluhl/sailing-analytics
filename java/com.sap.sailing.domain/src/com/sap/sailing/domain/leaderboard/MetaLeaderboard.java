package com.sap.sailing.domain.leaderboard;

/**
 * A leaderboard that accumulates the result of other (normal) leaderboards.
 * 
 * @author Frank Mittag (C5163874)
 */

public interface MetaLeaderboard extends Leaderboard {
    Iterable<Leaderboard> getLeaderboards();
}
