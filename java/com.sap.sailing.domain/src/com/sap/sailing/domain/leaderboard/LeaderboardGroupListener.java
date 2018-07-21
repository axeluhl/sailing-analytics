package com.sap.sailing.domain.leaderboard;

import java.io.Serializable;

public interface LeaderboardGroupListener extends Serializable {
    void leaderboardAdded(LeaderboardGroup group, Leaderboard leaderboard);
    void leaderboardRemoved(LeaderboardGroup group, Leaderboard leaderboard);
}
