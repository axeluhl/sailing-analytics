package com.sap.sailing.domain.leaderboard;

import java.util.Collection;

import com.sap.sailing.domain.common.Named;

public interface LeaderboardGroup extends Named {
    
    String getDescription();
    void setDescriptiom(String description);
    void setName(String newName);

    Iterable<Leaderboard> getLeaderboards();
    void addLeaderboard(Leaderboard leaderboard);
    void addAllLeaderboards(Collection<Leaderboard> leaderboards);
    void removeLeaderboard(Leaderboard leaderboard);
    void removeAllLeaderboards(Collection<Leaderboard> leaderboards);
    void clearLeaderboards();
    
}
