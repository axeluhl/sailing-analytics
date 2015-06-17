package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetEventStatisticsAction implements Action<ResultWithTTL<EventStatisticsDTO>> {
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetEventStatisticsAction() {
    }

    public GetEventStatisticsAction(UUID eventId) {
        this.eventId = eventId;
    }
    
    @GwtIncompatible
    public ResultWithTTL<EventStatisticsDTO> execute(DispatchContext context) {
        Event event = context.getRacingEventService().getEvent(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }
        
        int competitors = 0;
        int races = 0;
        int trackedRaces = 0;
        int regattas = 0;
        
        for(LeaderboardGroup lg: event.getLeaderboardGroups()) {
            for(Leaderboard leaderboard : lg.getLeaderboards()) {
                competitors += HomeServiceUtil.calculateCompetitorsCount(leaderboard);
                races += HomeServiceUtil.calculateRaceCount(leaderboard);
                trackedRaces += HomeServiceUtil.calculateTrackedRaceCount(leaderboard);
                regattas++;
            }
        }
        
        EventStatisticsDTO statisticsDTO = new EventStatisticsDTO(regattas, competitors, races, trackedRaces);
        // TODO: add more stats to DTO

        return new ResultWithTTL<EventStatisticsDTO>(1000 * 60 * 5, statisticsDTO);
    }
}
