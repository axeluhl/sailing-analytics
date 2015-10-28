package com.sap.sailing.gwt.server;

import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.home.communication.event.EventReferenceDTO;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.communication.media.SailingVideoDTO;
import com.sap.sailing.gwt.ui.client.HomeService;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.gwt.ui.shared.media.MediaConstants;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.common.media.VideoDescriptor;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * The server side implementation of the RPC service.
 */
public class HomeServiceImpl extends ProxiedRemoteServiceServlet implements HomeService {
    private static final long serialVersionUID = 3947782997746039939L;
    
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    public HomeServiceImpl() {
        BundleContext context = Activator.getDefault();
        
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
    }

    protected RacingEventService getService() {
        return racingEventServiceTracker.getService(); 
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
