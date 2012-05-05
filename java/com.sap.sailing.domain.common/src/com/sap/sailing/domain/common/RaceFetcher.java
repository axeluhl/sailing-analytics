package com.sap.sailing.domain.common;

public interface RaceFetcher {
    /**
     * Not for execution on the client; on the server, returns a <code>RaceDefinition</code> object.
     */
    Object getRace(EventAndRaceIdentifier eventNameAndRaceName);

    /**
     * Not for execution on the client; on the server, returns a <code>TrackedRace</code> object.
     */
    Object getTrackedRace(EventAndRaceIdentifier eventNameAndRaceName);

    Object getExistingTrackedRace(RaceIdentifier eventNameAndRaceName);
}
