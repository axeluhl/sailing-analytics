package com.sap.sailing.server.util;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;

import com.sap.sailing.domain.base.EventBase;

public interface EventUtil {
        
    public static Year getYearOfEvent(EventBase event) {
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(event.getStartDate().asMillis(), 0, ZoneOffset.UTC);
        return Year.of(dateTime.getYear());
    }
}
