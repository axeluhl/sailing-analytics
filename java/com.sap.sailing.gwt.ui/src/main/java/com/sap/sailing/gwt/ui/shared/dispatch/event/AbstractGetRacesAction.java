package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;

public abstract class AbstractGetRacesAction<R extends Result> implements Action<R> {
    
    protected interface RaceCallback {
        void doForRace(LeaderboardGroup lg, Leaderboard lb, RaceColumn raceColumn, String regattaName, Fleet fleet);
    }

    @GwtIncompatible
    protected void forRacesOfEvent(DispatchContext context, UUID eventId, RaceCallback callback) {
        Event event = context.getRacingEventService().getEvent(eventId);
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for (Leaderboard lb : lg.getLeaderboards()) {
                String regattaName = null;
                if(lb instanceof RegattaLeaderboard) {
                    regattaName = ((RegattaLeaderboard) lb).getRegatta().getName();
                }
                for(RaceColumn raceColumn : lb.getRaceColumns()) {
                    for(Fleet fleet : raceColumn.getFleets()) {
                        callback.doForRace(lg, lb, raceColumn, regattaName, fleet);
                    }
                }
            }
        }
    }
}
