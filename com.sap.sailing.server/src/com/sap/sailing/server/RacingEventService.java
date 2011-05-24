package com.sap.sailing.server;

import com.sap.sailing.domain.base.Event;

public interface RacingEventService {
    Iterable<Event> getAllEvents();
    Event getEventByName(String name);
}
