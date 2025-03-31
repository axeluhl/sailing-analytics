package com.sap.sailing.domain.common;

public interface RaceFetcher {
    /**
     * Not for execution on the client; on the server, returns a <code>RaceDefinition</code> object.
     */
    Object getRace(RegattaAndRaceIdentifier regattaNameAndRaceName);

    /**
     * Not for execution on the client; on the server, returns a <code>TrackedRace</code> object.
     * The method will not block or wait for the tracked race to appear. If it isn't found (see also
     * bug 5982) then {@code null} is returned.
     */
    Object getTrackedRace(RegattaAndRaceIdentifier regattaNameAndRaceName);

    Object getExistingTrackedRace(RegattaAndRaceIdentifier regattaNameAndRaceName);
}
