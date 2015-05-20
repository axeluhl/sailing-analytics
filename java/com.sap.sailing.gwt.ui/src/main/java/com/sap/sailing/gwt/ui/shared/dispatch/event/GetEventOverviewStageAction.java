package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.gwt.home.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewVideoStageDTO.Type;
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
        
        // TODO get correct message
        return new ResultWithTTL<>(ttl, new EventOverviewStageDTO(null, getStageContent(event, state, now)));
    }

    @GwtIncompatible
    public EventOverviewStageContentDTO getStageContent(Event event, EventState state, MillisecondsTimePoint now) {
        String stageImageUrl = HomeServiceUtil.getStageImageURLAsString(event);
//        if(state == EventState.RUNNING && TODO live video) {
//          return new EventOverviewVideoStageDTO(Type.LIVESTREAM);
//      }
        
//        if(TODO highlight video) {
//          return new EventOverviewVideoStageDTO(Type.HIGHLIGHTS);
//      }
        
        if(state == EventState.RUNNING) {
//            TODO next race countdown if available
            // TODO Proper Implementation (Type race)
//            return new EventOverviewRaceTickerStageDTO(new RegattaNameAndRaceName(
//                    "Regatta XY", "Race 1"), "Race 1 - Gold Fleet", new Date(new Date().getTime() + 5000), stageImageUrl);
            
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
                                .asDate(), stageImageUrl);
            }
        }
        
        if(state == EventState.UPCOMING || state == EventState.PLANNED) {
            return new EventOverviewTickerStageDTO(event.getStartDate()
                    .asDate(), event.getName(), stageImageUrl);
        }
        
        List<URL> photoGalleryImageURLs = HomeServiceUtil.getPhotoGalleryImageURLs(event);
        if(photoGalleryImageURLs.size() >= 3) {
            // TODO image gallery
        }
        
        if(HomeServiceUtil.hasVideos(event)) {
            // TODO first video
//          return new EventOverviewVideoStageDTO(Type.MEDIA);
        }
        
        return null;
    }
}
