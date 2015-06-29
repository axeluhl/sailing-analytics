package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.MediaTagConstants;

public class GetEventViewAction implements Action<EventViewDTO> {
    private static final Logger logger = Logger.getLogger(GetEventViewAction.class.getName());

    private UUID eventId;
    
    public GetEventViewAction() {
    }

    public GetEventViewAction(UUID eventId) {
        this.eventId = eventId;
    }
    
    @GwtIncompatible
    public EventViewDTO execute(DispatchContext context) {
        Event event = context.getRacingEventService().getEvent(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }

        EventViewDTO dto = new EventViewDTO();
        HomeServiceUtil.mapToMetadataDTO(event, dto, context.getRacingEventService());
        
        ImageDescriptor logoImage = event.findImageWithTag(MediaTagConstants.LOGO);
        dto.setLogoImage(logoImage != null ? HomeServiceUtil.convertToImageDTO(logoImage) : null);
        dto.setOfficialWebsiteURL(event.getOfficialWebsiteURL() == null ? null : event.getOfficialWebsiteURL().toString());

        dto.setHasMedia(HomeServiceUtil.hasMedia(event));
        dto.setState(HomeServiceUtil.calculateEventState(event));
        dto.setHasAnalytics(EventState.RUNNING.compareTo(dto.getState()) <= 0);

        boolean isFakeSeries = HomeServiceUtil.isFakeSeries(event);
        
        for (LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                try {
                    if(leaderboard instanceof RegattaLeaderboard) {
                        Regatta regatta = context.getRacingEventService().getRegattaByName(leaderboard.getName());
                        if(isFakeSeries && !HomeServiceUtil.isPartOfEvent(event, regatta)) {
                            continue;
                        }
                        
                        RegattaMetadataDTO regattaDTO = createRegattaMetadataDTO(leaderboardGroup, leaderboard);
                        regattaDTO.setStartDate(regatta.getStartDate() != null ? regatta.getStartDate().asDate() : null);
                        regattaDTO.setEndDate(regatta.getEndDate() != null ? regatta.getEndDate().asDate() : null);
                        regattaDTO.setState(HomeServiceUtil.calculateRegattaState(regattaDTO));
                        dto.getRegattas().add(regattaDTO);
                        
                    } else if(leaderboard instanceof FlexibleLeaderboard) {
                        RegattaMetadataDTO regattaDTO = createRegattaMetadataDTO(leaderboardGroup, leaderboard);
                        
                        regattaDTO.setStartDate(null);
                        regattaDTO.setEndDate(null);
                        regattaDTO.setState(HomeServiceUtil.calculateRegattaState(regattaDTO));
                        dto.getRegattas().add(regattaDTO);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Catched exception while reading data for leaderboard " + leaderboard.getName(), e);
                }
            }
        }
        
        if (isFakeSeries) {
            dto.setType(EventType.SERIES_EVENT);
            
            LeaderboardGroup overallLeaderboardGroup = event.getLeaderboardGroups().iterator().next();
            dto.setSeriesName(HomeServiceUtil.getLeaderboardDisplayName(overallLeaderboardGroup));
            List<Event> fakeSeriesEvents = new ArrayList<Event>();
            
            for (Event eventOfSeries : context.getRacingEventService().getAllEvents()) {
                for (LeaderboardGroup leaderboardGroup : eventOfSeries.getLeaderboardGroups()) {
                    if (overallLeaderboardGroup.equals(leaderboardGroup)) {
                        fakeSeriesEvents.add(eventOfSeries);
                    }
                }
            }
            Collections.sort(fakeSeriesEvents, new Comparator<Event>() {
                public int compare(Event e1, Event e2) {
                    return e1.getStartDate().compareTo(e2.getEndDate());
                }
            });
            for(Event eventInSeries: fakeSeriesEvents) {
                String displayName = HomeServiceUtil.getLocation(eventInSeries, context.getRacingEventService());
                if(displayName == null) {
                    displayName = eventInSeries.getName();
                }
                dto.getEventsOfSeries().add(new EventReferenceDTO(eventInSeries.getId(), displayName));
            }
        } else {
            dto.setType(dto.getRegattas().size() == 1 ? EventType.SINGLE_REGATTA: EventType.MULTI_REGATTA);
        }
        
        // Special solution for Kieler Woche 2015
        if("a9d6c5d5-cac3-47f2-9b5c-506e441819a1".equals(event.getId().toString())) {
            dto.setSailorsInfoURL(Locale.GERMAN.equals(context.getClientLocale()) ? "http://sailorsinfo.kieler-woche.de/" : "http://sailorsinfo.kieler-woche.de/en");
        }

        return dto;
    }

    @GwtIncompatible
    private RegattaMetadataDTO createRegattaMetadataDTO(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard) {
        RegattaMetadataDTO regattaDTO = new RegattaMetadataDTO(leaderboard.getName(), leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : leaderboard.getName());
        regattaDTO.setBoatCategory(leaderboardGroup.getDisplayName() != null ? leaderboardGroup.getDisplayName() : leaderboardGroup.getName());
        regattaDTO.setCompetitorsCount(HomeServiceUtil.calculateCompetitorsCount(leaderboard));
        regattaDTO.setRaceCount(HomeServiceUtil.calculateRaceCount(leaderboard));
        regattaDTO.setTrackedRacesCount(HomeServiceUtil.calculateTrackedRaceCount(leaderboard));
        regattaDTO.setBoatClass(HomeServiceUtil.calculateBoatClass(leaderboard));
        
        return regattaDTO;
    }
}
