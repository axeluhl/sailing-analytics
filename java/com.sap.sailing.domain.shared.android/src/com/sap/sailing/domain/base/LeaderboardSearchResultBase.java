package com.sap.sailing.domain.base;

import com.sap.sse.common.search.Hit;

/**
 * A generalization of <code>LeaderboardSearchResult</code> which has references to full-blown leaderboards, a regatta,
 * full leaderboard groups and the entire event; this form requires only the respective <code>...Base</code> flavors of
 * events, leaderboards and leaderboard groups which allows for the creation of a copy in a server that has been
 * receiving a search result from some other (remote) server, e.g., through JSON deserialization.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface LeaderboardSearchResultBase extends Hit {
    /**
     * The leaderboard that matches the search query.
     */
    LeaderboardBase getLeaderboard();

    String getRegattaName();
    
    String getBoatClassName();
    
    /**
     * If the leaderboard is part of one or more leaderboard groups, the leaderboard groups; an empty but
     * valid (non-<code>null</code>) collection otherwise.
     */
    Iterable<? extends LeaderboardGroupBase> getLeaderboardGroups();
    
    /**
     * If there is a connection between an {@link EventBase} and the {@link #getLeaderboard() leaderboard}, e.g.,
     * because the {@link EventBase} has a {@link EventBase#getLeaderboardGroups() leaderboard group} that is part of
     * {@link #getLeaderboardGroups()} or the event has a course area attached to its {@link EventBase#getVenue() venue}
     * that is referenced by the leaderboard as its {@link Leaderboard#getDefaultCourseArea() default course area}, the
     * event is returned; <code>null</code> otherwise.
     */
    Iterable<? extends EventBase> getEvents();
}
