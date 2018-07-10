package com.sap.sailing.gwt.home.communication.fakeseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO.EventSeriesState;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
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

    private UUID seriesUUIDOrNull;
    private UUID leaderboardGroupUUIDOrNull;

    @SuppressWarnings("unused")
    private GetEventSeriesViewAction() {
    }

    /**
     * Creates a {@link GetEventSeriesViewAction} instance for the given series-id.
     * 
     * @param id
     *            {@link UUID} of the series to load data for
     */
    public GetEventSeriesViewAction(UUID leaderboardGroupUUID) {
        super();
        if(leaderboardGroupUUID == null) {
            throw new RuntimeException("leaderboardGroupUUID cannot be null in this context");
        }
        this.leaderboardGroupUUIDOrNull = leaderboardGroupUUID;
    }
    
    /**
     * Creates a {@link GetEventSeriesViewAction} instance for the given series-id.
     * 
     * @param id
     *            {@link UUID} of the series to load data for
     */
    public GetEventSeriesViewAction(UUID seriesUUIDOrNull, UUID leaderboardGroupUUIDOrNull) {
        super();
        this.seriesUUIDOrNull = seriesUUIDOrNull;
        this.leaderboardGroupUUIDOrNull = leaderboardGroupUUIDOrNull;
    }

    @Override
    @GwtIncompatible
    public EventSeriesViewDTO execute(SailingDispatchContext ctx) throws DispatchException {
        LeaderboardGroup leaderBoardGroup;
        Event o;
        if (leaderboardGroupUUIDOrNull != null) {
            leaderBoardGroup = ctx.getRacingEventService().getLeaderboardGroupByID(leaderboardGroupUUIDOrNull);
            if (leaderBoardGroup == null) {
                throw new RuntimeException("LeaderboardGroup not found");
            }
            o = determineBestMatchingEvent(ctx, leaderBoardGroup);
        } else {
            // legacy code for old links
            o = ctx.getRacingEventService().getEvent(seriesUUIDOrNull);
            if (o == null) {
                throw new RuntimeException("Series not found");
            }
            if (Util.size(o.getLeaderboardGroups()) != 1) {
                throw new RuntimeException("Could not map event to LeaderboardGroup");
            }
            leaderBoardGroup = o.getLeaderboardGroups().iterator().next();
        }

        if (!leaderBoardGroup.hasOverallLeaderboard()) {
            throw new RuntimeException("Is not overall leaderboard");
        }

        EventSeriesViewDTO dto = new EventSeriesViewDTO();
        dto.setLeaderboardGroupUUID(leaderBoardGroup.getId());
        dto.setId(o.getId());
        ImageDescriptor logoImage = o.findImageWithTag(MediaTagConstants.LOGO);
        dto.setLogoImage(logoImage != null ? HomeServiceUtil.convertToImageDTO(logoImage) : null);
        // TODO implement correctly. We currently do not show media for series.
        dto.setHasMedia(false);

        boolean oneEventStarted = false;
        boolean oneEventLive = false;
        boolean allFinished = true;
        dto.setDisplayName(leaderBoardGroup.getDisplayName() != null ? leaderBoardGroup.getDisplayName()
                : leaderBoardGroup.getName());

        if (leaderBoardGroup.getOverallLeaderboard() != null) {
            dto.setLeaderboardId(leaderBoardGroup.getOverallLeaderboard().getName());
        }

        for (Event eventInSeries : HomeServiceUtil.getEventsForSeriesOrdered(leaderBoardGroup,
                ctx.getRacingEventService())) {
            EventMetadataDTO eventOfSeries = HomeServiceUtil.convertToMetadataDTO(eventInSeries,
                    ctx.getRacingEventService());
            dto.addEvent(eventOfSeries);

            oneEventStarted |= eventOfSeries.isStarted();
            oneEventLive |= (eventOfSeries.isStarted() && !eventOfSeries.isFinished());
            allFinished &= eventOfSeries.isFinished();
        }
        if (oneEventLive) {
            dto.setState(EventSeriesState.RUNNING);
        } else if (!oneEventStarted) {
            dto.setState(EventSeriesState.UPCOMING);
        } else if (allFinished) {
            dto.setState(EventSeriesState.FINISHED);
        } else {
            dto.setState(EventSeriesState.IN_PROGRESS);
        }

        dto.setHasAnalytics(oneEventStarted);
        return dto;
    }

    @GwtIncompatible
    private Event determineBestMatchingEvent(SailingDispatchContext ctx, LeaderboardGroup leaderBoardGroup) {
        List<Event> events = new ArrayList<>(
                HomeServiceUtil.getEventsForSeriesOrdered(leaderBoardGroup, ctx.getRacingEventService()));
        Collections.sort(events, new Comparator<Event>() {

            @Override
            public int compare(Event o1, Event o2) {
                boolean o1GroupPerfectMatch = Util.size(o1.getLeaderboardGroups()) == 1;
                boolean o2GroupPerfectMatch = Util.size(o2.getLeaderboardGroups()) == 1;
                int result = Boolean.compare(o1GroupPerfectMatch, o2GroupPerfectMatch);
                if (result == 0) {
                    TimePoint o1Start = o1.getStartDate();
                    if (o1Start == null) {
                        o1Start = TimePoint.BeginningOfTime;
                    }
                    TimePoint o2Start = o2.getStartDate();
                    if (o2Start == null) {
                        o2Start = TimePoint.BeginningOfTime;
                    }
                    result = o1Start.compareTo(o2Start);
                }
                return result;
            }
        });
        return events.get(0);
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(leaderboardGroupUUIDOrNull);
        key.append(seriesUUIDOrNull);
    }

}
