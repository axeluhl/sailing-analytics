package com.sap.sailing.gwt.home.communication.fakeseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO.EventSeriesState;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.shared.media.ImageDescriptor;

public class GetEventSeriesViewAction implements SailingAction<EventSeriesViewDTO>, IsClientCacheable {
    
    private UUID id;
    
    @SuppressWarnings("unused")
    private GetEventSeriesViewAction() {
    }
    
    public GetEventSeriesViewAction(UUID id) {
        super();
        this.id = id;
    }

    @Override
    @GwtIncompatible
    public EventSeriesViewDTO execute(SailingDispatchContext ctx) throws DispatchException {
        Event o = ctx.getRacingEventService().getEvent(id);
        if (o == null) {
            throw new RuntimeException("Series not found");
        }
        
        EventSeriesViewDTO dto = new EventSeriesViewDTO();
        dto.setId(id);
        ImageDescriptor logoImage = o.findImageWithTag(MediaTagConstants.LOGO);
        dto.setLogoImage(logoImage != null ? HomeServiceUtil.convertToImageDTO(logoImage) : null);
        // TODO implement correctly. We currently do not show media for series.
        dto.setHasMedia(false);
        
        boolean oneEventStarted = false;
        boolean oneEventLive = false;
        boolean allFinished = true;
        if (HomeServiceUtil.isFakeSeries(o)) {
            LeaderboardGroup overallLeaderboardGroup = o.getLeaderboardGroups().iterator().next();
            dto.setDisplayName(overallLeaderboardGroup.getDisplayName() != null ? overallLeaderboardGroup.getDisplayName() : overallLeaderboardGroup.getName());

            if (overallLeaderboardGroup.getOverallLeaderboard() != null) {
                dto.setLeaderboardId(overallLeaderboardGroup.getOverallLeaderboard().getName());
            }

            List<Event> fakeSeriesEvents = new ArrayList<Event>();
            for (Event event : ctx.getRacingEventService().getAllEvents()) {
                for (LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
                    if (overallLeaderboardGroup.equals(leaderboardGroup)) {
                        fakeSeriesEvents.add(event);
                    }
                }
            }
            Collections.sort(fakeSeriesEvents, new Comparator<Event>() {
                public int compare(Event e1, Event e2) {
                    return e1.getStartDate().compareTo(e2.getEndDate());
                }
            });
            for(Event eventInSeries: fakeSeriesEvents) {
                EventMetadataDTO eventOfSeries = HomeServiceUtil.convertToMetadataDTO(eventInSeries, ctx.getRacingEventService());
                dto.addEvent(eventOfSeries);
                
                oneEventStarted |= eventOfSeries.isStarted();
                oneEventLive |= (eventOfSeries.isStarted() && !eventOfSeries.isFinished());
                allFinished &= eventOfSeries.isFinished();
            }
        }
        if(oneEventLive) {
            dto.setState(EventSeriesState.RUNNING);
        } else if(!oneEventStarted) {
            dto.setState(EventSeriesState.UPCOMING);
        } else if(allFinished) {
            dto.setState(EventSeriesState.FINISHED);
        } else {
            dto.setState(EventSeriesState.IN_PROGRESS);
        }
        
        dto.setHasAnalytics(oneEventStarted);
        return dto;
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(id);
    }

}
