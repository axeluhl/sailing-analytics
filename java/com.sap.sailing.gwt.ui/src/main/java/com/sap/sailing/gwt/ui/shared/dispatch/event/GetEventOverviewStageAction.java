package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Date;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.gwt.home.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GetEventOverviewStageAction implements Action<ResultWithTTL<EventOverviewStageDTO>> {
    private UUID eventId;
    
    public GetEventOverviewStageAction() {
    }

    public GetEventOverviewStageAction(UUID eventId) {
        this.eventId = eventId;
    }
    
    @Override
    @GwtIncompatible
    public ResultWithTTL<EventOverviewStageDTO> execute(DispatchContext context) {
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        Event event = context.getRacingEventService().getEvent(eventId);
        EventState state = HomeServiceUtil.calculateEventState(event);
        int ttl = 1000 * 60 * 5;
        if(state == EventState.RUNNING) {
            ttl = 1000 * 60 * 2;
        }
        if(state == EventState.UPCOMING || state == EventState.PLANNED) {
            ttl = Math.max(ttl, (int) now.until(event.getStartDate()).asMillis());
        }
        return new ResultWithTTL<>(ttl, new EventOverviewStageDTO(null, getStageContent(event, state, now)));
    }

    @GwtIncompatible
    public EventOverviewStageContentDTO getStageContent(Event event, EventState state, MillisecondsTimePoint now) {
        
        if(state == EventState.UPCOMING || state == EventState.PLANNED) {
            return new EventOverviewTickerStageDTO(event.getStartDate()
                    .asDate(), event.getName());
        }
        
        if(state == EventState.RUNNING) {
//            if(TODO live video) {
//                return new ResultWithTTL<EventOverviewStageDTO>(5000, new EventOverviewVideoStageDTO());
//            } else {
            
            Regatta nextRegatta = null;
            for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
                for (Leaderboard lb : lg.getLeaderboards()) {
                    if(lb instanceof RegattaLeaderboard) {
                        Regatta regatta = ((RegattaLeaderboard) lb).getRegatta();
                        if (regatta.getStartDate() != null
                                && now.before(regatta.getStartDate())
                                && (nextRegatta == null || nextRegatta.getStartDate().after(regatta.getStartDate()))) {
                            nextRegatta = regatta;
                        }
                    }
                }
            }
            if(!HomeServiceUtil.isSingleRegatta(event) && nextRegatta != null) {
                return new EventOverviewRegattaTickerStageDTO(
                        new RegattaName(nextRegatta.getName()), nextRegatta.getName(), nextRegatta.getStartDate()
                                .asDate());
            } else {
                // TODO find next race or live race
//                for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
//                    for (Leaderboard lb : lg.getLeaderboards()) {
//                        for(RaceColumn raceColumn : lb.getRaceColumns()) {
//                            for(Fleet fleet : raceColumn.getFleets()) {
//                                
//                            }
//                        }
//                    }
//                }
                // TODO Proper Implementation (Type race)
                EventOverviewTickerStageDTO ticker = new EventOverviewRaceTickerStageDTO(new RegattaNameAndRaceName(
                        "Regatta XY", "Race 1"), "Race 1 - Gold Fleet", new Date(new Date().getTime() + 5000));
                return ticker;
            }
            // TODO clever fallback for live event/regatta?
//            }
        }
        // finished
//        if(TODO highlights video) {
        return new EventOverviewVideoStageDTO();
//        } else {
//        }
    }
}
