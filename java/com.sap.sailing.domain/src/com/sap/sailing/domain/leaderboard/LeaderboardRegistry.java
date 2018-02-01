package com.sap.sailing.domain.leaderboard;


public interface LeaderboardRegistry {
    Leaderboard getLeaderboardByName(String leaderboardName);
    
    void addLeaderboard(Leaderboard leaderboard);
}
