package com.sap.sailing.server.api;

public interface RaceFetcher {
    /**
     * Not for execution on the client; on the server, returns a <code>RaceDefinition</code> object.
     */
    Object getRace(EventNameAndRaceName eventNameAndRaceName);

    /**
     * Not for execution on the client; on the server, returns a <code>TrackedRace</code> object.
     */
    Object getTrackedRace(EventNameAndRaceName eventNameAndRaceName);

    Object getExistingTrackedRace(EventNameAndRaceName eventNameAndRaceName);
}
