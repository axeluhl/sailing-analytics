package com.sap.sailing.server.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Iterator;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.common.dto.EventType;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sse.common.Util;

public interface EventUtil {

    /**
     * Obtains the year of the provided {@link EventBase event}'s start time point mapped to UTC, or {@code null} if the
     * event's {@link EventBase#getStartDate()} is {@code null}.
     * 
     * @param event
     *            the {@link EventBase event} to get the year for
     */
    public static Integer getYearOfEvent(EventBase event) {
        return event.getStartDate() == null ? null : Instant.ofEpochMilli(event.getStartDate().asMillis()).atOffset(ZoneOffset.UTC).getYear();
    }
    
    /**
     * Returns {@code true} if the given event is part of an event series.
     */
    public static boolean isFakeSeries(EventBase event) {
        final Iterator<? extends LeaderboardGroupBase> lgIter = event.getLeaderboardGroups().iterator();
        if (!lgIter.hasNext()) {
            return false;
        }
        final LeaderboardGroupBase lg = lgIter.next();
        if (lgIter.hasNext()) {
            return false;
        }
        return lg.hasOverallLeaderboard();
    }
    
    public static EventType getEventType(Event event) {
        final EventType eventType;
        if (isFakeSeries(event)) {
            eventType = EventType.SERIES;
        } else {
            final Iterator<? extends LeaderboardGroup> leaderboardGroups = event.getLeaderboardGroups().iterator();
            if (leaderboardGroups.hasNext()) {
                final LeaderboardGroup leaderboardGroup = leaderboardGroups.next();
                if (!leaderboardGroups.hasNext() && Util.size(leaderboardGroup.getLeaderboards()) == 1) {
                    eventType = EventType.SINGLE_REGATTA;
                } else {
                    eventType = EventType.MULTI_REGATTA;
                }
            } else {
                eventType = EventType.MULTI_REGATTA;
            }
        }
        return eventType;
    }
}
