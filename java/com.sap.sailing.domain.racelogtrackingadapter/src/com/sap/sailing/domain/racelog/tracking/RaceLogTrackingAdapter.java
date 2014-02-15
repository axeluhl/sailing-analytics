package com.sap.sailing.domain.racelog.tracking;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackerManager;
import com.sap.sailing.domain.tracking.WindStore;

public interface RaceLogTrackingAdapter {
	/**
	 * Can be called either to track a new race, or optionally to load a race
	 * that is already in the {@link RaceLogTrackingState#TRACKING} state,
	 * but was closed.
	 */
	RacesHandle addRace(TrackerManager trackerManager, RegattaIdentifier regattaToAddTo,
			Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet, RaceLogStore raceLogStore,
			WindStore windStore, long timeoutInMilliseconds) throws RaceNotCreatedException;
    
    /**
     * List such RaceLog-tracked races, for which the {@link RaceLogTrackingState#AWAITING_RACE_DEFINITION} phase
     * has ended (that is to say, that are in the {@link RaceLogTrackingState#TRACKING} phase. Such a race
     * could be loaded (e.g., the race can be created and all related {@link GPSFix}es from the database are
     * recorded.
     * Will only return such {@link RaceColumn}s that do not already have an attached {@link TrackedRace}.
     * @param leaderboard
     * @return
     */
    Map<RaceColumn, Collection<Fleet>> listLoadableStoredRaceLogTrackedRaces(Leaderboard leaderboard);
    
    /**
     * List such RaceLog-tracked races, which are still in the {@link RaceLogTrackingState#AWAITING_RACE_DEFINITION}
     * phase.
     * @param leaderboard
     * @return
     */
    Map<RaceColumn, Collection<Fleet>> listRaceLogTrackedRacesAwaitingRaceDefinition(Leaderboard leaderboard);

    /**
     * Moves a RaceLog-tracked race from the {@link RaceLogTrackingState#AWAITING_RACE_DEFINITION} phase into
     * the {@link RaceLogTrackingState#TRACKING} phase. The actual {@link TrackedRace} is created, and fixes
     * that have arrived earlier, or will arrive in the future and fit to the {@link DeviceMapping}s specified
     * in this race's {@link RaceLog} will be forwarded to that {@link TrackedRace}, as well as being stored
     * in the database.
     */
    RacesHandle startTrackingRaceLogTrackedRace(Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet,
            RegattaIdentifier regatta, BoatClass boatClass, WindStore windStore) throws RaceNotCreatedException;
}
