package com.sap.sailing.domain.racelog.tracking;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.impl.RaceLogRaceTracker;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.server.RacingEventService;

public interface RaceLogTrackingAdapter {
	/**
	 * Can be called either to track a new race, or optionally to load a race
	 * that is already in the {@link RaceLogTrackingState#TRACKING} state, but
	 * was closed.
	 */
	RacesHandle addRace(RacingEventService service,
			RegattaIdentifier regattaToAddTo, Leaderboard leaderboard,
			RaceColumn raceColumn, Fleet fleet, RaceLogStore raceLogStore,
			WindStore windStore, long timeoutInMilliseconds)
			throws MalformedURLException, FileNotFoundException,
			URISyntaxException, Exception;

	/**
	 * List RaceLog-tracked races (such RaceLogs, in which a
	 * {@link DenoteForTrackingEvent} exists. Will only return such elements
	 * that do not already have an attached {@link TrackedRace}, and for which no
	 * {@link RaceLogRaceTracker} is registered.
	 */
	Map<RaceColumn, Collection<Fleet>> listLoadableStoredRaceLogTrackedRaces(RacingEventService service,
			Leaderboard leaderboard);

	/**
	 * Denotes the {@link RaceLog} for racelog-tracking, by inserting a
	 * {@link DenoteForTrackingEvent}.
	 * 
	 * @throws NotDenotableForTrackingException
	 *             Fails, if no {@link RaceLog}, or a non-empty {@link RaceLog}, or one with attached
	 *             {@link TrackedRace} is found already in place.
	 */
	void denoteForRaceLogTracking(RacingEventService service, Leaderboard leaderboard,
			RaceColumn raceColumn, Fleet fleet, String raceName)
			throws NotDenotableForTrackingException;
}
