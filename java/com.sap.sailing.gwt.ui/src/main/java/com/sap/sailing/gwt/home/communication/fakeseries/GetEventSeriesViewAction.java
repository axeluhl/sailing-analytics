package com.sap.sailing.gwt.home.communication.fakeseries;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO.EventSeriesState;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.server.util.EventUtil;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;
import com.sap.sse.shared.media.ImageDescriptor;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in the series list overview for the
 * {@link #GetEventSeriesViewAction(UUID) given series-id}, preparing the appropriate data structure.
 * </p>
 */
public class GetEventSeriesViewAction implements SailingAction<EventSeriesViewDTO>, IsClientCacheable {
    
    private UUID id;
    
    @SuppressWarnings("unused")
    private GetEventSeriesViewAction() {
    }
    
    /**
     * Creates a {@link GetEventSeriesViewAction} instance for the given series-id.
     * 
     * @param id
     *            {@link UUID} of the series to load data for
     */
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
        if (EventUtil.isFakeSeries(o)) {
            LeaderboardGroup overallLeaderboardGroup = o.getLeaderboardGroups().iterator().next();
            dto.setDisplayName(overallLeaderboardGroup.getDisplayName() != null ? overallLeaderboardGroup.getDisplayName() : overallLeaderboardGroup.getName());

            if (overallLeaderboardGroup.getOverallLeaderboard() != null) {
                dto.setLeaderboardId(overallLeaderboardGroup.getOverallLeaderboard().getName());
            }

            for (Event eventInSeries : HomeServiceUtil.getEventsForSeriesOrdered(overallLeaderboardGroup,
                    ctx.getRacingEventService())) {
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
