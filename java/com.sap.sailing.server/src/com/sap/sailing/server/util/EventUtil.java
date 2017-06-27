package com.sap.sailing.server.util;

import java.time.Instant;
import java.time.ZoneOffset;

import com.sap.sailing.domain.base.EventBase;

public interface EventUtil {

    /**
     * Obtains the year of the provided {@link EventBase event}'s start date.
     * 
     * @param event
     *            the {@link EventBase event} to get the year for
     * @return the year of the {@link EventBase event}'s start date
     */
    public static Integer getYearOfEvent(EventBase event) {
        Instant instant = Instant.ofEpochMilli(event.getStartDate().asMillis());
        return instant.atOffset(ZoneOffset.UTC).getYear();
    }
}
