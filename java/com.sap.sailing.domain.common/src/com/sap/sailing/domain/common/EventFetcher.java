package com.sap.sailing.domain.common;

public interface EventFetcher {
    /**
     * Not to be executed on the client; when executed on the server, returns an object of type <code>Event</code>
     */
    Object getEvent(EventName eventIdentifier);

}
