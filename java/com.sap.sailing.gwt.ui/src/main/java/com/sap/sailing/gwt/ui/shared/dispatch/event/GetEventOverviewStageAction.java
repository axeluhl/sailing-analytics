package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
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
        Event event = context.getRacingEventService().getEvent(eventId);
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        
        EventState state = HomeServiceUtil.calculateEventState(event);
        if(state == EventState.UPCOMING || state == EventState.PLANNED) {
            // TODO Type Event
            return new ResultWithTTL<EventOverviewStageDTO>(5000, new EventOverviewTickerStageDTO());
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
                 // TODO Type regatta
                return new ResultWithTTL<EventOverviewStageDTO>(5000, new EventOverviewTickerStageDTO());
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
                // TODO Type race
                return new ResultWithTTL<EventOverviewStageDTO>(5000, new EventOverviewTickerStageDTO());
            }
            // TODO clever fallback for live event/regatta?
//            }
        }
        // finished
//        if(TODO highlights video) {
        return new ResultWithTTL<EventOverviewStageDTO>(5000, new EventOverviewVideoStageDTO());
//        } else {
//        }
    }
}
