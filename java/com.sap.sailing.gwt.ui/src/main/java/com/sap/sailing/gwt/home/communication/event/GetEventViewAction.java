package com.sap.sailing.gwt.home.communication.event;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.common.client.EventWindFinderUtil;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.communication.eventview.SeriesReferenceWithEventsDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.EventActionUtil.LeaderboardCallback;
import com.sap.sailing.gwt.home.server.LeaderboardContext;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sse.common.Util;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.shared.media.ImageDescriptor;

/**
 * <p>
 * {@link SailingAction} implementation to load the basic logo, name, state, date and navigation information for a
 * {@link #GetEventViewAction(UUID) given event-id} to be shown on several pages of this event.
 * </p>
 */
public class GetEventViewAction implements SailingAction<EventViewDTO>, IsClientCacheable {
    private static final Logger logger = Logger.getLogger(GetEventViewAction.class.getName());

    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetEventViewAction() {
    }

    /**
     * Creates a {@link GetEventViewAction} instance for the given event-id.
     * 
     * @param eventId
     *            {@link UUID} of the {@link Event} to load data for
     */
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
        URL sailorsInfoWebsiteURL = event.getSailorsInfoWebsiteURLOrFallback(context.getClientLocale());
        dto.setSailorsInfoWebsiteURL(sailorsInfoWebsiteURL == null ? null : sailorsInfoWebsiteURL.toString());
        if (context.getWindFinderTrackerFactory() != null) {
            dto.setAllWindFinderSpotsUsedByEvent(new EventWindFinderUtil().getWindFinderSpotsToConsider(event,
                    context.getWindFinderTrackerFactory(), /* useCachedSpotsForTrackedRaces */ true));
        }
        dto.setHasMedia(HomeServiceUtil.hasMedia(event));
        dto.setState(HomeServiceUtil.calculateEventState(event));
        // bug2982: always show leaderboard and competitor analytics 
        dto.setHasAnalytics(true);
        
        String description = event.getDescription();
        if (description == null || description.trim().isEmpty() || event.getName().equalsIgnoreCase(description)) {
            // If a description isn't useful, it should not be shown in the UI
            description = null;
        }
        dto.setDescription(description);

        final HashSet<RegattaMetadataDTO> regattasOfEvent = new HashSet<>();
        final HashSet<LeaderboardGroup> relevantLeaderboardGroupsOfEvent = new HashSet<>();
        
        EventActionUtil.forLeaderboardsOfEvent(context, event, new LeaderboardCallback() {
            @Override
            public void doForLeaderboard(LeaderboardContext context) {
                try {
                    final Iterable<LeaderboardGroup> leaderboardGroupsForRegatta = context.getLeaderboardGroups();
                    Util.addAll(leaderboardGroupsForRegatta, relevantLeaderboardGroupsOfEvent);
                    if (Util.size(leaderboardGroupsForRegatta) == 1) {
                        final LeaderboardGroup singleLeaderboardGroup = leaderboardGroupsForRegatta.iterator().next();
                        if (singleLeaderboardGroup.hasOverallLeaderboard() && !context.isPartOfEvent()) {
                            // Regatta is associated to LeaderboardGroup that forms a series.
                            // In this case we only assume the Regatta to be part of the current event if the Regatta references the Event through the associated CourseArea.
                            return;
                        }
                    }
                    RegattaMetadataDTO regattaDTO = context.asRegattaMetadataDTO();
                    regattasOfEvent.add(regattaDTO);
                    dto.getRegattas().add(regattaDTO);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Catched exception while reading data for leaderboard " + context.getLeaderboardName(), e);
                }
            }
        });
        dto.setMultiRegatta(regattasOfEvent.size() != 1);
        
        if (relevantLeaderboardGroupsOfEvent.size() == 1) {
            final LeaderboardGroup singleLeaderboardGroup = relevantLeaderboardGroupsOfEvent.iterator().next();
            if (singleLeaderboardGroup.hasOverallLeaderboard()) {
                // The event is part of one series and does not host any further Regattas
                dto.setSeriesName(HomeServiceUtil.getLeaderboardDisplayName(singleLeaderboardGroup));
                
                final ArrayList<EventReferenceWithStateDTO> eventsOfSeries = new ArrayList<>();
                for (Event eventInSeries : HomeServiceUtil.getEventsForSeriesInDescendingOrder(singleLeaderboardGroup,
                        context.getRacingEventService())) {
                    // TODO include regatta name if more than one regatta is part of the event and add values for every regatta
                    String displayName = HomeServiceUtil.getLocation(eventInSeries, context.getRacingEventService());
                    if(displayName == null) {
                        displayName = eventInSeries.getName();
                    }
                    EventState eventState = HomeServiceUtil.calculateEventState(eventInSeries);
                    eventsOfSeries.add(new EventReferenceWithStateDTO(eventInSeries.getId(), displayName, eventState));
                }
                dto.setSeriesData(new SeriesReferenceWithEventsDTO(
                        singleLeaderboardGroup.getDisplayName() != null ? singleLeaderboardGroup.getDisplayName()
                                : singleLeaderboardGroup.getName(),
                        singleLeaderboardGroup.getName(), eventsOfSeries));
            }
        }
        return dto;
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
    }
}
