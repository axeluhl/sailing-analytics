package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GetLiveRacesAction implements Action<ResultWithTTL<LiveRacesDTO>> {
    
    private UUID eventId;
    
    public GetLiveRacesAction() {
    }

    public GetLiveRacesAction(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<LiveRacesDTO> execute(DispatchContext context) {
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        LiveRacesDTO result = new LiveRacesDTO();

        Event event = context.getRacingEventService().getEvent(getEventId());
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for (Leaderboard lb : lg.getLeaderboards()) {
                // Regatta regatta = getService().getRegattaByName(lb.getName());
                for (TrackedRace trackedRace : lb.getTrackedRaces()) {
                    // trackedRace.getMarks()
                    if (trackedRace.getStartOfRace().before(now) && trackedRace.getEndOfRace().after(now)) {
                        result.addRace(new LiveRaceDTO(trackedRace.getRace().getName()));
                    }
                }
                // for (RaceDefinition rd : regatta.getAllRaces()) {
                // rd.
                // }
            }
        }
        return new ResultWithTTL<LiveRacesDTO>(5000, result);
    }
}
