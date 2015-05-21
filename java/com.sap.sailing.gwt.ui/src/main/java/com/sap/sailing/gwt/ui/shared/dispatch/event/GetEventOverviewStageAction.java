package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.media.MimeType;
import com.sap.sailing.gwt.home.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.gwt.ui.shared.media.MediaUtils;
import com.sap.sse.common.Util;
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
            ttl = Math.min(ttl, (int) now.until(event.getStartDate()).asMillis());
        }
        
        // TODO get correct message
        return new ResultWithTTL<>(ttl, new EventOverviewStageDTO(null, getStageContent(context, event, state, now)));
    }

    @GwtIncompatible
    public EventOverviewStageContentDTO getStageContent(DispatchContext context, Event event, EventState state, MillisecondsTimePoint now) {
        String stageImageUrl = HomeServiceUtil.getStageImageURLAsString(event);
//        if(state == EventState.RUNNING && TODO live video) {
//          return new EventOverviewVideoStageDTO(Type.LIVESTREAM);
//      }
        
//        if(TODO highlight video) {
//          return new EventOverviewVideoStageDTO(Type.HIGHLIGHTS);
//      }
        
//        if(state == EventState.RUNNING) {
//            NextRaceFinder nextRaceFinder = new NextRaceFinder();
//            RacesActionUtil.forRacesOfEvent(context, eventId, nextRaceFinder);
//            
//            RaceContext nextRace = nextRaceFinder .getNextRace();
//            if(nextRace != null) {
//                return new EventOverviewRaceTickerStageDTO(nextRace.getRaceIdentifier(), nextRace.getStageText(), nextRaceFinder.getNextStartTime().asDate(), stageImageUrl);
//            }
//            
//            // TODO This is not a good idea due to Axel and Frank as regatta start times aren't correctly set.
//            Regatta nextRegatta = null;
//            for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
//                for (Leaderboard lb : lg.getLeaderboards()) {
//                    if(lb instanceof RegattaLeaderboard) {
//                        Regatta regatta = ((RegattaLeaderboard) lb).getRegatta();
//                        if (regatta.getStartDate() != null
//                                && now.before(regatta.getStartDate())
//                                && (nextRegatta == null || nextRegatta.getStartDate().after(regatta.getStartDate()))) {
//                            nextRegatta = regatta;
//                        }
//                    }
//                }
//            }
//            if(!HomeServiceUtil.isSingleRegatta(event) && nextRegatta != null) {
//                return new EventOverviewRegattaTickerStageDTO(
//                        new RegattaName(nextRegatta.getName()), nextRegatta.getName(), nextRegatta.getStartDate()
//                                .asDate(), stageImageUrl);
//            }
//        }
        
        if(state == EventState.UPCOMING || state == EventState.PLANNED) {
            return new EventOverviewTickerStageDTO(event.getStartDate()
                    .asDate(), event.getName(), stageImageUrl);
        }
        
        List<URL> photoGalleryImageURLs = HomeServiceUtil.getPhotoGalleryImageURLs(event);
        if(photoGalleryImageURLs.size() >= 3) {
            // TODO image gallery
        }
        
//            Iterable<URL> videoURLs = event.getVideoURLs();
//            for (int i = Util.size(videoURLs) - 1; i >= 0; i--) {
//                URL videoUrl = Util
//            }
//            MimeType type = MediaUtils.detectMimeTypeFromUrl(url.toString());
            // TODO first video
          return new EventOverviewVideoStageDTO(EventOverviewVideoStageDTO.Type.MEDIA, null, null);
        
//        return new EventOverviewTickerStageDTO(null, null, stageImageUrl);
    }
}
