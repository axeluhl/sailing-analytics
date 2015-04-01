package com.sap.sailing.gwt.ui.server.dispatch.handlers;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.ui.server.dispatch.AbstractSailingHandler;
import com.sap.sailing.gwt.ui.server.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GetLiveRacesActionHandler extends AbstractSailingHandler<ResultWithTTL<LiveRacesDTO>, GetLiveRacesAction> {

    public GetLiveRacesActionHandler(ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker) {
        super(racingEventServiceTracker);
    }

    @Override
    public ResultWithTTL<LiveRacesDTO> execute(GetLiveRacesAction action, DispatchContext context) {
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        LiveRacesDTO result = new LiveRacesDTO();
        
        Event event = getService().getEvent(action.getEventId());
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for (Leaderboard lb : lg.getLeaderboards()) {
//                Regatta regatta = getService().getRegattaByName(lb.getName());
                for (TrackedRace trackedRace : lb.getTrackedRaces()) {
//                    trackedRace.getMarks()
                    if(trackedRace.getStartOfRace().before(now) && trackedRace.getEndOfRace().after(now)) {
                        result.addRace(new LiveRaceDTO(trackedRace.getRace().getName()));
                    }
                }
//                for (RaceDefinition rd : regatta.getAllRaces()) {
//                    rd.
//                }
            }
        }
        return new ResultWithTTL<LiveRacesDTO>(5000, result);
    }

}
