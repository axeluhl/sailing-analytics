package com.sap.sailing.server.notification;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

public class EmptySailingNotificationService implements SailingNotificationService {

    @Override
    public void stop() {
    }

    @Override
    public void notifyUserOnBoatClassRaceChangesStateToFinished(BoatClass boatClass, TrackedRace trackedRace,
            Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet) {
    }

    @Override
    public void notifyUserOnBoatClassWhenScoreCorrectionsAreAvailable(BoatClass boatClass, Leaderboard leaderboard) {
    }

    @Override
    public void notifyUserOnBoatClassUpcomingRace(BoatClass boatClass, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet, TimePoint when) {
    }

    @Override
    public void notifyUserOnCompetitorPassesFinish(Competitor competitor, TrackedRace trackedRace,
            Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet) {
    }

    @Override
    public void notifyUserOnCompetitorScoreCorrections(Competitor competitor, Leaderboard leaderboard) {
    }
}
