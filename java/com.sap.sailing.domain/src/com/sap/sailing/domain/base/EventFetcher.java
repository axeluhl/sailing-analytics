package com.sap.sailing.domain.base;

public interface EventFetcher {
    Iterable<Event> getAllEvents();
}
