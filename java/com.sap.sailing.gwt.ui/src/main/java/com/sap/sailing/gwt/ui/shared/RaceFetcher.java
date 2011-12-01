package com.sap.sailing.gwt.ui.shared;

public interface RaceFetcher {
    /**
     * Not for execution on the client; on the server, returns a <code>RaceDefinition</code> object.
     */
    Object getRace(EventNameAndRaceName eventNameAndRaceName);

    /**
     * Not for execution on the client; on the server, returns a <code>TrackedRace</code> object.
     */
    Object getTrackedRace(EventNameAndRaceName eventNameAndRaceName);

    Object getRace(LeaderboardNameAndRaceColumnName leaderboardNameAndRaceColumnName);

    Object getTrackedRace(LeaderboardNameAndRaceColumnName leaderboardNameAndRaceColumnName);

    Object getExistingTrackedRace(LeaderboardNameAndRaceColumnName leaderboardNameAndRaceColumnName);

    Object getExistingTrackedRace(EventNameAndRaceName eventNameAndRaceName);
}
