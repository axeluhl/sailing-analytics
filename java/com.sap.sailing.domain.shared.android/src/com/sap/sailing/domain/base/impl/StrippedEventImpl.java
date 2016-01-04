package com.sap.sailing.domain.base.impl;

import java.util.UUID;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sse.common.TimePoint;

/**
 * A simplified implementation of the {@link EventBase} interface which maintains an immutable collection of
 * {@link LeaderboardGroupBase} objects to implement the {@link #getLeaderboardGroups()} method. A local image
 * size cache can be maintained using the {@link #setImageSize} method.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class StrippedEventImpl extends EventBaseImpl {
    private static final long serialVersionUID = 5608501747499933988L;
    private final Iterable<LeaderboardGroupBase> leaderboardGroups;
    
    public StrippedEventImpl(String name, TimePoint startDate, TimePoint endDate, String venueName,
            boolean isPublic, UUID id, Iterable<LeaderboardGroupBase> leaderboardGroups) {
        this(name, startDate, endDate, new VenueImpl(venueName), isPublic, id, leaderboardGroups);
    }

    public StrippedEventImpl(String name, TimePoint startDate, TimePoint endDate, Venue venue,
            boolean isPublic, UUID id, Iterable<LeaderboardGroupBase> leaderboardGroups) {
        super(name, startDate, endDate, venue, isPublic, id);
        this.leaderboardGroups = leaderboardGroups;
    }

    @Override
    public Iterable<LeaderboardGroupBase> getLeaderboardGroups() {
        return leaderboardGroups;
    }
}
