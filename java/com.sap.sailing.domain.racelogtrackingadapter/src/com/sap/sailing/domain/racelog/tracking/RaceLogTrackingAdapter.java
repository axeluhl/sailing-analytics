package com.sap.sailing.domain.racelog.tracking;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotableForTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.tracking.impl.RaceLogRaceTracker;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public interface RaceLogTrackingAdapter {
    /**
     * Can be called either to track a new race, or optionally to load a race that is already in the
     * {@link RaceLogTrackingState#TRACKING} state, but was closed.
     */
    RacesHandle addRace(RacingEventService service, RegattaIdentifier regattaToAddTo, Leaderboard leaderboard,
            RaceColumn raceColumn, Fleet fleet, long timeoutInMilliseconds) throws MalformedURLException,
            FileNotFoundException, URISyntaxException, Exception;

    /**
     * Lists all races for this leaderboards according to the rules in
     * {@link #canRaceBeAdded(RacingEventService, Leaderboard, RaceColumn, Fleet)}.
     */
    Map<RaceColumn, Collection<Fleet>> listRaceLogTrackersThatCanBeAdded(RacingEventService service, Leaderboard leaderboard);

    /**
     * Returns {@code true}, if a {@link DenoteForTrackingEvent} exists within the {@link RaceLog}, and if it does not
     * not already have an attached {@link TrackedRace}, and if there is no {@link RaceLogRaceTracker} registered for it
     * already.
     */
    boolean isDenotedForRaceLogTracking(RacingEventService service, RaceColumn raceColumn, Fleet fleet);

    /**
     * Returns {@code true}, if a {@link DenoteForTrackingEvent} exists within the {@link RaceLog}, and if it does not
     * not already have an attached {@link TrackedRace}, and if there is no {@link RaceLogRaceTracker} registered for it
     * already.
     */
    boolean canRaceLogTrackerBeAdded(RacingEventService service, RaceColumn raceColumn, Fleet fleet);

    /**
     * Denotes the {@link RaceLog} for racelog-tracking, by inserting a {@link DenoteForTrackingEvent}.
     * 
     * @throws NotDenotableForTrackingException
     *             Fails, if no {@link RaceLog}, or a non-empty {@link RaceLog}, or one with attached
     *             {@link TrackedRace} is found already in place.
     *             Also fails, if the {@code leaderboard} is not a {@link RegattaLeaderboard}.
     */
    void denoteForRaceLogTracking(RacingEventService service, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet, String raceName) throws NotDenotableForTrackingException;

    /**
     * Denotes the entire {@link Leaderboard} for racelog-tracking, by calling the
     * {@link #denoteForRaceLogTracking(RacingEventService, Leaderboard, RaceColumn, Fleet, String)} method for each
     * {@link RaceLog}.
     * 
     * Also, a listener is registered, that denotes every {@link RaceLog} that is added at a later time as well.
     */
    void denoteForRaceLogTracking(RacingEventService service, Leaderboard leaderboard)
            throws NotDenotableForTrackingException;
}
