package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.VideoDescriptor;

public class GetEventOverviewStageAction implements Action<ResultWithTTL<EventOverviewStageDTO>> {
    private static final Collection<String> rankedTags = Arrays.asList(MediaTagConstants.LIVESTREAM, MediaTagConstants.FEATURED, MediaTagConstants.HIGHLIGHT);
    private static final Collection<String> rankedTagsFinished = Arrays.asList(MediaTagConstants.FEATURED, MediaTagConstants.HIGHLIGHT);
    
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetEventOverviewStageAction() {
    }

    public GetEventOverviewStageAction(UUID eventId) {
        this.eventId = eventId;
    }
    
    @Override
    @GwtIncompatible
    public ResultWithTTL<EventOverviewStageDTO> execute(DispatchContext context) {
        TimePoint now = MillisecondsTimePoint.now();
        Event event = context.getRacingEventService().getEvent(eventId);
        EventState state = HomeServiceUtil.calculateEventState(event);
        long ttl = Duration.ONE_MINUTE.times(5).asMillis();
        if(state == EventState.RUNNING) {
            ttl = Duration.ONE_MINUTE.times(2).asMillis();
        }
        if(state == EventState.UPCOMING || state == EventState.PLANNED) {
            ttl = Math.min(ttl, now.until(event.getStartDate()).asMillis());
        }
        
        // TODO get correct message
        EventOverviewStageDTO stage = new EventOverviewStageDTO(null, getStageContent(context, event, state, now));
        return new ResultWithTTL<>(new MillisecondsDurationImpl(ttl), stage);
    }

    @GwtIncompatible
    public EventOverviewStageContentDTO getStageContent(DispatchContext context, Event event, EventState state, TimePoint now) {
        // Simple solution:
        // P1: Show the best matching video if available
        // P2: Show Countdown for upcoming events
        // P3: Show Stage image without Countdown
        
        Collection<String> tags = state == EventState.FINISHED ? rankedTagsFinished : rankedTags;
        VideoDescriptor stageVideo = HomeServiceUtil.getStageVideo(event, context.getClientLocale(), tags, true);
        if(stageVideo != null) {
            return new EventOverviewVideoStageDTO(EventOverviewVideoStageDTO.Type.MEDIA, HomeServiceUtil.toVideoDTO(stageVideo));
        }
        String stageImageUrl = HomeServiceUtil.getStageImageURLAsString(event);
        if(state == EventState.UPCOMING || state == EventState.PLANNED) {
            return new EventOverviewTickerStageDTO(event.getStartDate().asDate(), event.getName(), stageImageUrl);
        }
        return new EventOverviewTickerStageDTO(null, null, stageImageUrl);
        
        
        
        // TODO do the full implementation
        
//        String stageImageUrl = HomeServiceUtil.getStageImageURLAsString(event);
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
//        
//        if(state == EventState.UPCOMING || state == EventState.PLANNED) {
//            return new EventOverviewTickerStageDTO(event.getStartDate()
//                    .asDate(), event.getName(), stageImageUrl);
//        }
//        
//        List<URL> photoGalleryImageURLs = HomeServiceUtil.getPhotoGalleryImageURLs(event);
//        if(photoGalleryImageURLs.size() >= 3) {
//            // TODO image gallery
//        }
//        
//        return new EventOverviewTickerStageDTO(null, null, stageImageUrl);
    }
}
