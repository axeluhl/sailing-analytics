package com.sap.sailing.domain.leaderboard;

import java.util.Collection;
import java.util.UUID;

import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sse.common.WithID;

/**
 * A leaderboard group is used to group one or more {@link Leaderboard}s. It can be used to represent all or part of the
 * regattas of an event or to establish a collection of regattas or leaderboards otherwise belonging together according
 * to some context. This could, for example, be a leaderboard group of all the interesting races in a particular boat
 * class or races at a particular event over time.<p>
 * 
 * The leaderboards grouped by this object have an iteration order that equals their insertion order. We have seen
 * cases where a user interface shall display the leaderboards in reverse insertion order. This behavior is described
 * by {@link #isDisplayGroupsInReverseOrder()}.<p>
 * 
 * Optionally, a leaderboard group can specify an "Overall Leaderboard". The typical use case for this is a regatta series
 * such as the Extreme Sailing Series or the German Sailing League ("Bundesliga") where several regatta results are
 * combined into a series score. The overall leaderboard, if defined, has its own scoring scheme and obtains its
 * "tracked results" from the results of the respective leaderboard. Each leaderboard in this group then represents
 * one race column in the overall leaderboard.
 * 
 * @author Frank Mittag (c5163874)
 */
public interface LeaderboardGroup extends LeaderboardGroupBase {
    void addLeaderboardGroupListener(LeaderboardGroupListener listener);
    void removeLeaderboardGroupListener(LeaderboardGroupListener listener);
    boolean isDisplayGroupsInReverseOrder();
    
    /**
     * @return a non-live copy of the leaderboard list as it looked when the method was called
     */
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
    
    /**
     * Specializes the {@link WithID#getId()} method regarding its return type.
     */
    UUID getId();
}
