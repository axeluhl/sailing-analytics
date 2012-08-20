package com.sap.sailing.domain.leaderboard;

import java.util.Collection;

import com.sap.sailing.domain.common.Named;

public interface LeaderboardGroup extends Named {
    void addLeaderboardGroupListener(LeaderboardGroupListener listener);
    void removeLeaderboardGroupListener(LeaderboardGroupListener listener);
    
    String getDescription();
    void setDescriptiom(String description);
    void setName(String newName);

    Iterable<Leaderboard> getLeaderboards();
    int getIndexOf(Leaderboard leaderboard);
    void addLeaderboard(Leaderboard leaderboard);
    void addLeaderboardAt(Leaderboard leaderboard, int index);
    void addAllLeaderboards(Collection<Leaderboard> leaderboards);
    void removeLeaderboard(Leaderboard leaderboard);
    void removeAllLeaderboards(Collection<Leaderboard> leaderboards);
    void clearLeaderboards();
    Leaderboard getOverallLeaderboard();
    void setOverallLeaderboard(Leaderboard leaderboard);
}
