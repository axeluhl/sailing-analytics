package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;

@GwtIncompatible
public final class RacesActionUtil {
    private RacesActionUtil() {
    }
    
    protected interface RaceCallback {
        void doForRace(RaceContext context);
    }

    public static void forRacesOfEvent(DispatchContext context, UUID eventId, RaceCallback callback) {
        Event event = context.getRacingEventService().getEvent(eventId);
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for (Leaderboard lb : lg.getLeaderboards()) {
                for(RaceColumn raceColumn : lb.getRaceColumns()) {
                    for(Fleet fleet : raceColumn.getFleets()) {
                        callback.doForRace(new RaceContext(lg, lb, raceColumn, fleet));
                    }
                }
            }
        }
    }
}
