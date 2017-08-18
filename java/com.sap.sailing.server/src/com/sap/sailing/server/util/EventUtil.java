package com.sap.sailing.server.util;

import java.time.Instant;
import java.time.ZoneOffset;

import com.sap.sailing.domain.base.EventBase;

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
}
