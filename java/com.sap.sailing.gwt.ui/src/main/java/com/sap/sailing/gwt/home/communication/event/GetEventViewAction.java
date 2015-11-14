package com.sap.sailing.gwt.home.communication.event;

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
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.LeaderboardContext;
import com.sap.sailing.gwt.home.server.EventActionUtil.LeaderboardCallback;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.MediaTagConstants;

public class GetEventViewAction implements SailingAction<EventViewDTO>, IsClientCacheable {
    private static final Logger logger = Logger.getLogger(GetEventViewAction.class.getName());

    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetEventViewAction() {
    }

    public GetEventViewAction(UUID eventId) {
        this.eventId = eventId;
    }
    
    @GwtIncompatible
    public EventViewDTO execute(SailingDispatchContext context) {
        final Event event = context.getRacingEventService().getEvent(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }

        final EventViewDTO dto = new EventViewDTO();
        HomeServiceUtil.mapToMetadataDTO(event, dto, context.getRacingEventService());
        
        ImageDescriptor logoImage = event.findImageWithTag(MediaTagConstants.LOGO);
        dto.setLogoImage(logoImage != null ? HomeServiceUtil.convertToImageDTO(logoImage) : null);
        dto.setOfficialWebsiteURL(event.getOfficialWebsiteURL() == null ? null : event.getOfficialWebsiteURL().toString());
        dto.setSailorsInfoWebsiteURL(event.getSailorsInfoWebsiteURL() == null ? null : event.getSailorsInfoWebsiteURL().toString());

        dto.setHasMedia(HomeServiceUtil.hasMedia(event));
        dto.setState(HomeServiceUtil.calculateEventState(event));
        // bug2982: always show leaderboard and competitor analytics 
        dto.setHasAnalytics(true);

        final boolean isFakeSeries = HomeServiceUtil.isFakeSeries(event);
        
        EventActionUtil.forLeaderboardsOfEvent(context, event, new LeaderboardCallback() {
            @Override
            public void doForLeaderboard(LeaderboardContext context) {
                try {
                    if(isFakeSeries && !context.isPartOfEvent()) {
                        return;
                    }
                    RegattaMetadataDTO regattaDTO = context.asRegattaMetadataDTO();
                    dto.getRegattas().add(regattaDTO);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Catched exception while reading data for leaderboard " + context.getLeaderboardName(), e);
                }
            }
        });
        
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
        
        // Special solution for localization of SailorsInfo URL
        if(dto.getSailorsInfoWebsiteURL() != null && !Locale.GERMAN.equals(context.getClientLocale())) {
            String localizedURL = dto.getSailorsInfoWebsiteURL();
            if(!localizedURL.endsWith("/")) {
                localizedURL += "/";
            }
            localizedURL += "en";
            dto.setSailorsInfoWebsiteURL(localizedURL);
        }
        return dto;
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
    }
}
