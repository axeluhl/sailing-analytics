package com.sap.sailing.gwt.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.home.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.home.communication.event.EventReferenceDTO;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO.EventSeriesState;
import com.sap.sailing.gwt.home.communication.media.MediaConstants;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.communication.media.SailingVideoDTO;
import com.sap.sailing.gwt.ui.client.HomeService;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.common.media.VideoDescriptor;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * The server side implementation of the RPC service.
 */
public class HomeServiceImpl extends ProxiedRemoteServiceServlet implements HomeService {
    private static final long serialVersionUID = 3947782997746039939L;
    
    public static final int MAX_STAGE_EVENTS = 5;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    public HomeServiceImpl() {
        BundleContext context = Activator.getDefault();
        
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
    }

    protected RacingEventService getService() {
        return racingEventServiceTracker.getService(); 
    }
    
    @Override
    public EventSeriesViewDTO getEventSeriesViewById(UUID id) {
        Event o = getService().getEvent(id);
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
            for (Event event : getService().getAllEvents()) {
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
                EventMetadataDTO eventOfSeries = HomeServiceUtil.convertToMetadataDTO(eventInSeries, getService());
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
    public MediaDTO getMediaForEvent(UUID eventId) {
        Event event = getService().getEvent(eventId);
        EventReferenceDTO eventRef = new EventReferenceDTO(event);

        String eventName = event.getName();
        MediaDTO media = new MediaDTO();
        for(ImageDescriptor image : HomeServiceUtil.getPhotoGalleryImages(event)) {
            SailingImageDTO imageDTO = new SailingImageDTO(eventRef, image.getURL().toString(), image.getCreatedAtDate().asDate());
            imageDTO.setSizeInPx(image.getWidthInPx(), image.getHeightInPx());
            imageDTO.setTitle(image.getTitle() != null ? image.getTitle(): eventName);
            imageDTO.setSubtitle(image.getSubtitle());
            imageDTO.setTags(image.getTags());
            imageDTO.setCopyright(image.getCopyright());
            imageDTO.setLocale(image.getLocale() != null ? image.getLocale().toString() : null);
            media.addPhoto(imageDTO);
        }
        for(VideoDescriptor video : event.getVideos()) {
            MimeType type = video.getMimeType();
            if (MediaConstants.SUPPORTED_VIDEO_TYPES.contains(type)) {
                SailingVideoDTO videoDTO = HomeServiceUtil.toSailingVideoDTO(eventRef, video);
                media.addVideo(videoDTO);
            }
        }
        return media;
    }

    @Override
    public MediaDTO getMediaForEventSeries(UUID seriesId) {
        // TODO implement correctly. We currently do not show media for series.
        return getMediaForEvent(seriesId);
    }
    
    // @Override
    // public EventListViewDTO getEventListView() throws MalformedURLException {
    // EventListDataCalculator eventListDataCalculator = new EventListDataCalculator(getService());
    // HomeServiceUtil.forAllPublicEvents(getService(), getThreadLocalRequest(), eventListDataCalculator);
    // return eventListDataCalculator.getResult();
    // }
}
