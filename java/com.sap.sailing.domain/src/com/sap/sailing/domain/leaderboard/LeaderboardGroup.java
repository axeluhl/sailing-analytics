package com.sap.sailing.domain.leaderboard;

import java.util.List;

import com.sap.sailing.domain.common.Named;

public interface LeaderboardGroup extends Named {

    List<Leaderboard> getLeaderboards();
    void addLeaderboard(Leaderboard leaderboard);
    void removeLeaderboard(Leaderboard leaderboard);
    
    String getDescription();
    void setDescriptiom(String description);
    
}
