package com.sap.sailing.domain.base;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;

/**
 * This is what you get when you search a <code>RacingEventService</code> with its <code>search(...)</code> method. It
 * delivers the "real things," meaning that the leaderboards and events etc. are those objects currently living in the
 * server instance. This is richer than the de-serialized {@link LeaderboardSearchResultBase} objects you get when
 * fetching search results from remote instances, such as those described by a {@link RemoteSailingServerReference}.
 * However, for putting them into a DTO to transfer them to a web client, both are fine.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface LeaderboardSearchResult extends LeaderboardSearchResultBase {
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
    Iterable<LeaderboardGroup> getLeaderboardGroups();
    
    /**
     * If there is a connection between an {@link Event} and the {@link #getLeaderboard() leaderboard}, e.g., because
     * the {@link Event} has a {@link Event#getLeaderboardGroups() leaderboard group} that is part of {@link #getLeaderboardGroups()} or
     * the event has a course area attached to its {@link Event#getVenue() venue} that is referenced by the leaderboard as
     * its {@link Leaderboard#getCourseAreas() default course area}, the event is part of the resulting iterable.
     */
    Iterable<Event> getEvents();
}
