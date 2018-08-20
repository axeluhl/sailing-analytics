package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Event;

/**
 * A leaderboard group which is connected to a sailing event
 * @author Frank Mittag (c5163874)
 */
public interface EventLeaderboardGroup extends LeaderboardGroup {
    Event getEvent();
}
