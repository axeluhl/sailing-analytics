package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.news.InfoNewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.news.LeaderboardNewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.news.RaceCompetitorNewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.gwt.ui.shared.media.MediaUtils;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.media.MimeType;

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
        long ttl = 1000 * 60 * 5;
        if(state == EventState.RUNNING) {
            ttl = 1000 * 60 * 2;
        }
        if(state == EventState.UPCOMING || state == EventState.PLANNED) {
            ttl = Math.min(ttl, now.until(event.getStartDate()).asMillis());
        }
        
        // TODO get correct message
        EventOverviewStageDTO stage = new EventOverviewStageDTO(null, getStageContent(context, event, state, now));
        addNews(stage);
        return new ResultWithTTL<>(ttl, stage);
    }

    @GwtIncompatible
    public EventOverviewStageContentDTO getStageContent(DispatchContext context, Event event, EventState state, MillisecondsTimePoint now) {
        // Simple solution:
        // P1: Show the last video if available
        // P2: Show Countdown for upcoming events
        // P3: Show Stage image without Countdown
        
        Iterable<URL> videoURLs = event.getVideoURLs();
        for (int i = Util.size(videoURLs) - 1; i >= 0; i--) {
            // TODO fmittag/pgt: implement locale specific live stream using context.getClientLocaleName() or context.getClientLocale()
            String videoUrl = Util.get(videoURLs, i).toString();
            MimeType type = MediaUtils.detectMimeTypeFromUrl(videoUrl);
            if (type == MimeType.youtube || type == MimeType.vimeo || type == MimeType.mp4) {
                return new EventOverviewVideoStageDTO(EventOverviewVideoStageDTO.Type.MEDIA, type, videoUrl);
            }
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

    @GwtIncompatible
    private void addNews(EventOverviewStageDTO stage) {
        stage.addNews(new InfoNewsEntryDTO("Abendbespaﬂung", "Es gibt Freibier und Wurst!!!", new Date()));
        stage.addNews(new LeaderboardNewsEntryDTO("Test LB", "505 m", BoatClassMasterdata._5O5.getDisplayName(), new Date(new Date().getTime() - 120000), LeaderboardNewsEntryDTO.Type.NEW_RESULTS));
        
        LeaderboardNewsEntryDTO lbEntryWithExternalLink = new LeaderboardNewsEntryDTO("Test LB", "Some Regatta", BoatClassMasterdata.LASER_RADIAL.getDisplayName(), new Date(new Date().getTime() - 270000), LeaderboardNewsEntryDTO.Type.NEW_RESULTS);
        lbEntryWithExternalLink.setExternalURL("http://www.sap.com/");
        stage.addNews(lbEntryWithExternalLink);
        
        CompetitorDTOImpl testCompetitor = new CompetitorDTOImpl("Peter Mayer", null, null, null, null, null, null, null, null, null, null);
        stage.addNews(new RaceCompetitorNewsEntryDTO("Some regatta", "R7", "R7 - Gold", BoatClassMasterdata.PIRATE.getDisplayName(), new Date(new Date().getTime() - 1337000), testCompetitor, RaceCompetitorNewsEntryDTO.Type.WINNER));
        stage.addNews(new RaceCompetitorNewsEntryDTO("Some regatta", "R6", "R6 - Gold", BoatClassMasterdata.PIRATE.getDisplayName(), new Date(new Date().getTime() - 1888000), testCompetitor, RaceCompetitorNewsEntryDTO.Type.CRASH));
    }
}
