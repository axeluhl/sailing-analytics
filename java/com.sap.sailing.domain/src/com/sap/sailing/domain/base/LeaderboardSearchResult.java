package com.sap.sailing.domain.base;

import java.util.Set;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sse.common.search.Hit;

public interface LeaderboardSearchResult extends Hit {
    /**
     * The leaderboard that matches the search query.
     */
    Leaderboard getLeaderboard();

    /**
     * Returns the {@link RegattaLeaderboard#getRegatta() regatta leaderboard's regatta} if the
     * {@link #getLeaderboard() leaderboard} is a regatta leaderboard, or <code>null</code> otherwise.
     */
    Regatta getRegatta();
    
    /**
     * If the leaderboard is part of one or more leaderboard groups, the leaderboard groups; an empty but
     * valid (non-<code>null</code>) collection otherwise.
     */
    Set<LeaderboardGroup> getLeaderboardGroups();
    
    /**
     * If there is a connection between an {@link Event} and the {@link #getLeaderboard() leaderboard}, e.g., because
     * the {@link Event} has a {@link Event#getLeaderboardGroups() leaderboard group} that is part of {@link #getLeaderboardGroups()} or
     * the event has a course area attached to its {@link Event#getVenue() venue} that is referenced by the leaderboard as
     * its {@link Leaderboard#getDefaultCourseArea() default course area}, the event is returned; <code>null</code> otherwise.
     */
    EventBase getEvent();
}
