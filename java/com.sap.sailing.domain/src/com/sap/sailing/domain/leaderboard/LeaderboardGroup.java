package com.sap.sailing.domain.leaderboard;

import java.util.Collection;

import com.sap.sailing.domain.common.Renamable;

/**
 * A leaderboard group is used to group one or more {@link Leaderboard}s.
 * @author Frank Mittag (c5163874)
 */
public interface LeaderboardGroup extends Renamable {
    void addLeaderboardGroupListener(LeaderboardGroupListener listener);
    void removeLeaderboardGroupListener(LeaderboardGroupListener listener);
    
    String getDescription();
    void setDescriptiom(String description);

    boolean isDisplayGroupsInReverseOrder();
    
    Iterable<Leaderboard> getLeaderboards();
    int getIndexOf(Leaderboard leaderboard);
    void addLeaderboard(Leaderboard leaderboard);
    void addLeaderboardAt(Leaderboard leaderboard, int index);
    void addAllLeaderboards(Collection<Leaderboard> leaderboards);
    void removeLeaderboard(Leaderboard leaderboard);
    void removeAllLeaderboards(Collection<Leaderboard> leaderboards);
    void clearLeaderboards();
    /**
     * @return The overall leaderboard of this group or null if there is none.
     */
    Leaderboard getOverallLeaderboard();
    void setOverallLeaderboard(Leaderboard leaderboard);
}
