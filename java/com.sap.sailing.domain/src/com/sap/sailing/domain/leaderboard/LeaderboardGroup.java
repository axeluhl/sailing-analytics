package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.common.Named;

public interface LeaderboardGroup extends Named {

    Iterable<Leaderboard> getLeaderboards();
    void addLeaderboard(Leaderboard leaderboard);
    void removeLeaderboard(Leaderboard leaderboard);
    
    String getDescription();
    void setDescriptiom(String description);
    void setName(String newName);
    
}
