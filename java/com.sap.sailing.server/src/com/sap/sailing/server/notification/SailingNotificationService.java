package com.sap.sailing.server.notification;

import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Stoppable;
import com.sap.sse.common.TimePoint;

public interface SailingNotificationService extends Stoppable {

    /**
     * Called when {@link RaceState} changes state to finished.
     */
    void notifyUserOnBoatClassRaceChangesStateToFinished(BoatClass boatClass, TrackedRace trackedRace, Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet);
    
    /**
     * Called on score correction for a race with BoatClass if the last score correction was more than one minute.
     */
    void notifyUserOnBoatClassWhenScoreCorrectionsAreAvailable(BoatClass boatClass, Leaderboard leaderboard);
    
    /**
     * Called whenever a start time is set for a race with {@link BoatClass} through the race's {@link RaceState}.
     */
    void notifyUserOnBoatClassUpcomingRace(BoatClass boatClass, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet, TimePoint when);

    /**
     * Triggered when a competitor passes the finishing waypoint of a race.
     */
    void notifyUserOnCompetitorPassesFinish(Competitor competitor, TrackedRace trackedRace, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet);

    /**
     * Triggered when a score correction is set for competitor on any leaderboard.
     */
    void notifyUserOnCompetitorScoreCorrections(Competitor competitor, Leaderboard leaderboard);

}